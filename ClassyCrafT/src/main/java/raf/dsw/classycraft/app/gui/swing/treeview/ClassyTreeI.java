package raf.dsw.classycraft.app.gui.swing.treeview;

import raf.dsw.classycraft.app.controller.events.NodeChangedClassyEvent;
import raf.dsw.classycraft.app.controller.events.NodeChildrenClassyEvent;
import raf.dsw.classycraft.app.controller.events.SelectionClassyEvent;
import raf.dsw.classycraft.app.core.ApplicationFramework;
import raf.dsw.classycraft.app.core.msggen.IPublisher;
import raf.dsw.classycraft.app.core.msggen.ISubscriber;
import raf.dsw.classycraft.app.core.treemodel.ClassyRepository;
import raf.dsw.classycraft.app.core.treemodel.factory.AbstractNodeFactory;
import raf.dsw.classycraft.app.core.treemodel.nodes.abstractNodes.ClassyNode;
import raf.dsw.classycraft.app.core.treemodel.nodes.abstractNodes.ClassyNodeComposite;
import raf.dsw.classycraft.app.core.treemodel.nodes.abstractNodes.ClassyNodeLeaf;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.entities.Interclass;
import raf.dsw.classycraft.app.core.treemodel.nodes.projNodes.Diagram;
import raf.dsw.classycraft.app.core.treemodel.nodes.projNodes.Package;
import raf.dsw.classycraft.app.core.treemodel.nodes.projNodes.Project;
import raf.dsw.classycraft.app.core.treemodel.nodes.projNodes.ProjectExplorer;
import raf.dsw.classycraft.app.gui.swing.main.MainFrame;
import raf.dsw.classycraft.app.gui.swing.painters.ConnectionPainter;
import raf.dsw.classycraft.app.gui.swing.treeview.model.ClassyTreeItem;
import raf.dsw.classycraft.app.gui.swing.treeview.view.ClassyTreeView;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.util.*;

public class ClassyTreeI implements ClassyTree, IPublisher, ISubscriber {
    private DefaultTreeModel treeModel;
    private ClassyTreeView treeView;
    public ClassyTreeView getView(){return treeView;}
    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }

    //node -> its tree item, so items are found in O(1) instead of searching the whole tree
    private final Map<ClassyNode, ClassyTreeItem> items=new IdentityHashMap<>();

    ClassyTreeItem openedItem;
    public void setOpenedItem(ClassyTreeItem sel){
        openedItem =sel;
        notifySubscribers( new SelectionClassyEvent(this,sel));
    }
    @Override
    public ClassyTreeView generateTree(ProjectExplorer pe) {
        ClassyTreeItem root= new ClassyTreeItem(pe);
        items.put(pe,root);
        treeModel=new DefaultTreeModel(root);

        treeView= new ClassyTreeView(treeModel);
        treeView.setSelectionPath(new TreePath(root));
        treeView.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        return treeView;
    }

    private ClassyTreeItem itemOf(ClassyNode node){
        return items.get(node);
    }
    private ClassyTreeItem newItem(ClassyNode node){ //creates and registers the item, does not touch the view
        if(node instanceof Diagram d){ //caution!
            d.addSubscriber(this);
        }
        ClassyTreeItem item=new ClassyTreeItem(node);
        items.put(node,item);
        return item;
    }
    private ClassyTreeItem buildItems(ClassyNode node){ //item together with the items of all descendants
        ClassyTreeItem item=newItem(node);
        if(node instanceof ClassyNodeComposite comp){
            for (var c:comp.getChildren()) {
                item.add(buildItems(c));
            }
        }
        return item;
    }
    private void forget(ClassyTreeItem item){ //unregisters item and its descendants
        var it=item.depthFirstEnumeration();
        while (it.hasMoreElements()){
            items.remove(((ClassyTreeItem) it.nextElement()).getNode());
        }
    }

    public void AddChildItem(ClassyTreeItem parent, ClassyNode node){ //main add
        treeModel.insertNodeInto(newItem(node),parent,parent.getChildCount());
        treeView.expandPath(new TreePath(parent.getPath()) );
    }
    private void AddChildItems(ClassyTreeItem parent, List<ClassyNode> nodes){ //one view update for the whole batch
        if(nodes.isEmpty()) return;
        int[] indices=new int[nodes.size()];
        int first=parent.getChildCount();
        for (int i=0;i<indices.length;i++) {
            parent.add(newItem(nodes.get(i)));
            indices[i]=first+i;
        }
        treeModel.nodesWereInserted(parent,indices);
        treeView.expandPath(new TreePath(parent.getPath()) );
    }
    private void addItems(List<ClassyNode> nodes){ //nodes already added to the model, grouped by parent
        Map<ClassyTreeItem,List<ClassyNode>> byParent=new LinkedHashMap<>();
        for (ClassyNode c:nodes) {
            ClassyTreeItem p=itemOf(c.getParent());
            if(p!=null)
                byParent.computeIfAbsent(p,k->new ArrayList<>()).add(c);
        }
        byParent.forEach(this::AddChildItems);
    }
    private void removeItems(List<ClassyNode> nodes){ //nodes already removed from the model, grouped by parent
        Map<ClassyTreeItem,List<ClassyTreeItem>> byParent=new LinkedHashMap<>();
        for (ClassyNode c:nodes) {
            ClassyTreeItem item=itemOf(c);
            if(item!=null && item.getParent()!=null)
                byParent.computeIfAbsent((ClassyTreeItem) item.getParent(),k->new ArrayList<>()).add(item);
        }
        byParent.forEach((parent,gone)->{
            gone.sort(Comparator.comparingInt(parent::getIndex));
            int[] indices=new int[gone.size()];
            Object[] removed=new Object[gone.size()];
            for (int i=0;i<indices.length;i++) {
                indices[i]=parent.getIndex(gone.get(i));
                removed[i]=gone.get(i);
            }
            for (ClassyTreeItem item:gone) {
                parent.remove(item);
                forget(item);
            }
            treeModel.nodesWereRemoved(parent,indices,removed);
        });
    }
    public void Update(ClassyTreeItem parent){
        List<ClassyNode> old=new ArrayList<>();
        for (int i=0;i<parent.getChildCount();i++) {
            old.add(((ClassyTreeItem) parent.getChildAt(i)).getNode());
        }
        removeItems(old);
        AddChildItems(parent,new ArrayList<>(((ClassyNodeComposite) parent.getNode()).getChildren()));
    }
    public boolean AddChild(ClassyTreeItem parentItem, String type) {
        if(! (parentItem.getNode() instanceof ClassyNodeComposite parentNode) ){
                return false;
        }
        ClassyNode newNode= CreateNode(parentNode,type);
        if(newNode==null){
            return false;
        }

        parentNode.AddChild(newNode);
        AddChildItem(parentItem,newNode); //subscribes to new diagrams
        return true;
    }

    @Override
    public ClassyNode CreateNode(ClassyNodeComposite parent,String type) {
        AbstractNodeFactory factory= ClassyRepository.getFactoryOf(parent.getClass().getSimpleName());
        ClassyNode res= factory.createNode(type);
        if(res==null)
            return null;
        res.setParent(parent);
        parent.NameChild(res);
        return res;
    }

    private boolean RenameItem( ClassyTreeItem item,String newName) {
        if(item.getNode() instanceof ClassyNodeLeaf) return false;
        String oldName = item.toString();
        item.setName(newName);
        item.getNode().setName(newName);
        ClassyNodeComposite node=((ClassyNodeComposite) item.getNode().getParent());
        if (node.isUnique(item.getNode())){
            treeModel.nodeChanged(item);
            return true;
        }
        item.setName(oldName);
        item.getNode().setName(oldName);
        return false;
    }
    public boolean RenameSel(String name){
        if(getSelectedNode()!=null)
            return RenameItem(getSelectedNode(),name);
        return false;
    }
    public ClassyTreeItem getSelectedNode() {
        return (ClassyTreeItem) treeView.getLastSelectedPathComponent();
    }


    private boolean RemoveNode(ClassyTreeItem item) {//main remove
        if(item==treeModel.getRoot())
            return false;
        if(item== openedItem || item.isNodeDescendant(openedItem))
            setOpenedItem(null);

        if(item.getNode() instanceof ClassyNodeLeaf) return false;  //generate error or implement delete notify...
        if(item.getNode() instanceof Diagram d){
            d.removeSubscriber(this);
        }


        treeView.setSelectionPath(treeView.getSelectionPath().getParentPath());

        ClassyNode node= item.getNode();
        ((ClassyNodeComposite) node.getParent()).removeChild(node);

        treeModel.removeNodeFromParent(item);
        forget(item);
        treeView.expandPath(treeView.getSelectionPath());
        return true;
    }
    @Override
    public boolean AppendChild(String type) {
        if(getSelectedNode()!=null)
            return AddChild(getSelectedNode(),type);
        return false;
    }

    @Override
    public boolean RemoveSel() {

        if(getSelectedNode()!=null)
            return RemoveNode(getSelectedNode());
        return false;
    }




    public boolean isPackSel(){
        return getSelectedNode().getNode() instanceof Package;
    }
    public boolean isExpSel(){
        return getSelectedNode().getNode() instanceof ProjectExplorer;
    }
    public  boolean isSel(){return getSelectedNode()!=null;}

    List<ISubscriber> subscribers=new ArrayList<>();
    @Override
    public void addSubscriber(ISubscriber subscr) {
        subscribers.add(subscr);
    }

    @Override
    public void removeSubscriber(ISubscriber subscr) {
        subscribers.remove(subscr);
    }

    @Override
    public void notifySubscribers(Object notification) { //notifies package view after selection change
        for (ISubscriber sub:subscribers) {
            sub.react(notification);
        }
    }





    @Override
    public void react(Object notification) {  //diagrams belonging to curently selected package has elements added/removed, diagram elemnet name chaged...
        if(notification instanceof NodeChildrenClassyEvent ech){
            addItems(ech.getAdded()); //diagram elements
            removeItems(ech.getRemoved());
        }
        else if(notification instanceof NodeChangedClassyEvent nch){
            ClassyTreeItem item=itemOf(nch.getNode());
            if(item!=null)
                treeModel.nodeChanged(item);
        }
    }

    public ClassyTreeItem getRoot(){
        return ((ClassyTreeItem) this.getTreeModel().getRoot());
    }

    public void RemoveNode(ClassyNode child){
        var x=itemOf(child);
        if(x!=null)
            RemoveNode(x);
    }
    public void AddNode(ClassyNode child){
        AddChildItem(itemOf(child.getParent()),child);
    }
    public void AddNodeR(ClassyNode child){ //child with all descendants, inserted into the view once
        ClassyTreeItem parent=itemOf(child.getParent());
        ClassyTreeItem item=buildItems(child);
        treeModel.insertNodeInto(item,parent,parent.getChildCount());
        treeView.expandPath(new TreePath(parent.getPath()));
        var it=item.depthFirstEnumeration();
        while (it.hasMoreElements()){
            ClassyTreeItem n=(ClassyTreeItem) it.nextElement();
            if(n.getChildCount()>0)
                treeView.expandPath(new TreePath(n.getPath()));
        }
    }




    public void loadProj(Project p){
        p.setParent(((ClassyNodeComposite) getRoot().getNode()));
        p.parenthesise();
        AddNodeR(p);
    }
}
