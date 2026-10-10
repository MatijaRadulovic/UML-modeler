package raf.dsw.classycraft.app.core.treemodel.nodes.projNodes;

import com.fasterxml.jackson.annotation.JsonTypeName;
import raf.dsw.classycraft.app.controller.events.NodeChildrenClassyEvent;
import raf.dsw.classycraft.app.controller.events.NodeChangedClassyEvent;
import raf.dsw.classycraft.app.core.msggen.IPublisher;
import raf.dsw.classycraft.app.core.msggen.ISubscriber;
import raf.dsw.classycraft.app.core.treemodel.nodes.abstractNodes.ClassyNode;
import raf.dsw.classycraft.app.core.treemodel.nodes.abstractNodes.ClassyNodeComposite;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.connections.Connection;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.entities.Interclass;
import raf.dsw.classycraft.app.gui.swing.painters.ConnectionPainter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

@JsonTypeName("diag")
public class Diagram extends ClassyNodeComposite implements IPublisher {
    public Diagram(String name, ClassyNodeComposite parent) {
        super(name, parent);
    }

    public Diagram() {
    }
    transient List<ISubscriber> subscribers=new ArrayList<>();
    @Override
    public void addSubscriber(ISubscriber subscr) {
        subscribers.add(subscr);
    }

    @Override
    public void removeSubscriber(ISubscriber subscr) {
        subscribers.remove(subscr);
    }

    @Override
    public void setName(String name) {
        super.setName(name);
        notifySubscribers(new NodeChangedClassyEvent(this));
    }

    @Override
    public void notifySubscribers(Object notification) {  //package tabbed pane after name change for tab title change, if child removed/added from diagram view notify jtree for update
        for (ISubscriber sub:subscribers) { //notify diagram view to repaint? x
            sub.react(notification);
        }
    }
    @Override
    public void AddChild(ClassyNode child) {
        super.AddChild(child);
        notifySubscribers(NodeChildrenClassyEvent.childAddedEvent(child));
    }
    public void AddChildren(List<ClassyNode> added) { //one event for the whole batch
        if(added.isEmpty()) return;
        children.addAll(added);
        notifySubscribers(NodeChildrenClassyEvent.childrenAddedEvent(added));
    }
    public void removeChildren(List<ClassyNode> removed) {
        if(removed.isEmpty()) return;
        Set<ClassyNode> gone=Collections.newSetFromMap(new IdentityHashMap<>()); //equals() is name based, compare identity
        gone.addAll(removed);
        children.removeIf(gone::contains);
        notifySubscribers(NodeChildrenClassyEvent.childrenRemovedEvent(removed));
    }
    @Override
    public void removeChild(ClassyNode child) {
        super.removeChild(child);
        notifySubscribers(NodeChildrenClassyEvent.childRemovedEvent(child));
    }
}
