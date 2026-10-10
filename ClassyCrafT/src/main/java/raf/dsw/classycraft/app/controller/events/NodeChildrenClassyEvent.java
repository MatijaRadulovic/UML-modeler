package raf.dsw.classycraft.app.controller.events;

import raf.dsw.classycraft.app.core.treemodel.nodes.abstractNodes.ClassyNode;
import raf.dsw.classycraft.app.core.treemodel.nodes.abstractNodes.ClassyNodeComposite;

import java.util.*;

public class NodeChildrenClassyEvent extends ClassyEvent{
    private final List<ClassyNode> added;
    private final List<ClassyNode> removed;

    public boolean isRemoved(){
        return !removed.isEmpty();
    }
    public boolean isAdded(){
        return !added.isEmpty();
    }
    //plain lists, so every subscriber sees all of them
    public List<ClassyNode> getAdded(){
        return added;
    }
    public List<ClassyNode> getRemoved(){
        return removed;
    }
    public ClassyNodeComposite getParent(){
        return ((ClassyNodeComposite) source);
    }
    public NodeChildrenClassyEvent(List<ClassyNode> a,List<ClassyNode> r){
        added=Collections.unmodifiableList(new ArrayList<>(a));
        removed=Collections.unmodifiableList(new ArrayList<>(r));
    }
    public static ClassyEvent childrenAddedEvent(List<ClassyNode> c){
        return new NodeChildrenClassyEvent(c,new ArrayList<ClassyNode>());
    }
    public static ClassyEvent childAddedEvent(ClassyNode c){
        return childrenAddedEvent(Collections.singletonList(c));
    }
    public static ClassyEvent childRemovedEvent(ClassyNode c){
        return childrenRemovedEvent(Collections.singletonList(c));
    }
    public static ClassyEvent childrenRemovedEvent(List<ClassyNode> c){
        return new NodeChildrenClassyEvent(new ArrayList<ClassyNode>(),c);
    }
}
