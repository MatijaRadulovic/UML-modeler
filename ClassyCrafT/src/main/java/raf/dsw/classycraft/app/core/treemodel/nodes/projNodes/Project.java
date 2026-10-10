package raf.dsw.classycraft.app.core.treemodel.nodes.projNodes;

import com.fasterxml.jackson.annotation.JsonTypeName;
import raf.dsw.classycraft.app.controller.events.NodeChangedClassyEvent;
import raf.dsw.classycraft.app.core.msggen.IPublisher;
import raf.dsw.classycraft.app.core.msggen.ISubscriber;
import raf.dsw.classycraft.app.core.treemodel.nodes.abstractNodes.ClassyNodeComposite;

import java.util.ArrayList;
import java.util.List;

@JsonTypeName("proj")
public class Project extends ClassyNodeComposite implements IPublisher {
    String author;

    public void setAuthor(String author) {
        this.author = author;
        notifySubscribers(new NodeChangedClassyEvent(this)); //proj info pane
    }
    public String getAuthor(){
        return author;
    }
    String path;
    transient Boolean changed=true;

    public void Unsaved(){
        changed=true;
    }
    public boolean SavedIs(){
        return  !changed &&path!=null;
    }
    public void Saved(){
        //changed=false;
    }

    public Project(String name, String author, String path, ClassyNodeComposite parent) {
        super(name, parent);
        this.author=author;
        this.path=path;
    }

    public Project() {
        author="nepoznat";
    }

    public Project(String name, ClassyNodeComposite parent) {
        super(name,parent);
        this.author="nepoznat";
        this.path="nepoznat";
    }

    public String PathGet(){
        return  path;
    }
    @Override
    public void setName(String name) {
        super.setName(name);
        notifySubscribers(new NodeChangedClassyEvent(this)); //proj info pane
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
    public void notifySubscribers(Object notification) { //notifies proj info pane to display modified project info
        for (ISubscriber sub:subscribers) {
            sub.react(notification);
        }
    }
}
