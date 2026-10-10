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
import java.util.ArrayList;
import java.util.List;

public class ClassyTreeI implements ClassyTree, IPublisher, ISubscriber {
    private DefaultTreeModel treeModel;
    private ClassyTreeView treeView;
    public ClassyTreeView getView(){return treeView;}
    public DefaultTreeModel getTreeModel() {
        return treeModel;
    }

    ClassyTreeItem openedItem;
    public void setOpenedItem(ClassyTreeItem sel){
        openedItem =sel;
        notifySubscribers( new SelectionClassyEvent(this,sel));
    }
    @Override
    public ClassyTreeView generateTree(ProjectExplorer pe) {
        ClassyTreeItem root= new ClassyTreeItem(pe);
        treeModel=new DefaultTreeModel(root);

        treeView= new ClassyTreeView(treeModel);
        treeView.setSelectionPath(new TreePath(root));
        treeView.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        return treeView;
    }

    public void AddChildItem(ClassyTreeItem parent, ClassyNode node){ //main add
        if(node instanceof Diagram d){ //caution!
            d.addSubscriber(this);
        }

        ClassyTreeItem newItem=new ClassyTreeItem(node);
        parent.add(newItem);
        treeView.expandPath(new TreePath(parent.getPath()) );
        SwingUtilities.updateComponentTreeUI(treeView);

    }
    public void Update(ClassyTreeItem parent){
        parent.removeAllChildren();
        for (var c: ((ClassyNodeComposite) parent.getNode()).getChildren()) {
            AddChildItem(parent,c);
        }
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
        AddChildItem(parentItem,newNode);

        if(newNode instanceof Diagram diz)
            diz.addSubscriber(this);
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
            SwingUtilities.updateComponentTreeUI(treeView);
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

        ((DefaultTreeModel)treeView.getModel()).removeNodeFromParent(item);
        treeView.expandPath(treeView.getSelectionPath());
        SwingUtilities.updateComponentTreeUI(treeView);
        return true;
    }
    private void RemoveTreeItem(ClassyTreeItem item){
        ((DefaultTreeModel)treeView.getModel()).removeNodeFromParent(item);
        SwingUtilities.updateComponentTreeUI(treeView);

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
            while (ech.hasNextAdded()){
                ClassyNode c=ech.getNextAdded();//diagram element
                AddChildSel(c);
            }
            while(ech.hasNextRemoved()){
                ClassyNode c=ech.getNextRemoved();
                RemoveChildSel(c);
            }
            SwingUtilities.updateComponentTreeUI(treeView);

        }
        else if(notification instanceof NodeChangedClassyEvent nch){
            ClassyNode c=nch.getNode();
            var p=c.getParent();
            var x=find(getRoot(),c);
            //x.setName2(nch.getNode().getName());
            SwingUtilities.updateComponentTreeUI(treeView);


        }
    }
    public ClassyTreeItem find(ClassyTreeItem anc,ClassyNode node){
        var it=anc.depthFirstEnumeration();
        while (it.hasMoreElements()){
            ClassyTreeItem n= ((ClassyTreeItem) it.nextElement());
            if(n.getNode()==node){
                return n;
            }
        }
        return null;
    }

    public ClassyTreeItem getRoot(){
        return ((ClassyTreeItem) this.getTreeModel().getRoot());
    }

    public void RemoveNode(ClassyNode child){
        child.getParent();
        var x=find(getRoot(),child);
        RemoveNode(x);
    }
    public void RemoveChildSel(ClassyNode child){
        child.getParent();
        var x=find(openedItem,child);
        RemoveTreeItem(x);
    }
    public void AddChildSel(ClassyNode child){

        var x=find(getRoot(),child.getParent());//diagram
        AddChildItem(x,child);
    }
    public void AddNode(ClassyNode child){
        child.getParent();
        var x=find(getRoot(),child.getParent());
        AddChildItem(x,child);
    }
    public void AddNodeR(ClassyNode child){
        AddNode(child);
        if(child instanceof ClassyNodeComposite comp){
            for (var c:comp.getChildren()) {
                AddNodeR(c);
            }
        }

    }




    public void loadProj(Project p){
        p.setParent(((ClassyNodeComposite) getRoot().getNode()));
        p.parenthesise();
        AddNodeR(p);
    }

    public void loadDiagram(Diagram d){
        d.parenthesise();
        if(getSelectedNode().getNode() instanceof Diagram dor){
            d.setParent(((ClassyNodeComposite) dor.getParent()));
            for (var c:d.getChildren()) {
                c.setParent(dor);
                dor.AddChild(c);
            }

            MainFrame.getInstance().getPkgpane().getActiveDiagramP().reset();
        }


    }
}
