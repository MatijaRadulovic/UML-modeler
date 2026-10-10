package raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.entities;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.DiagramElement;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.connections.Aggregation;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.connections.Composition;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.connections.Dependency;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.connections.Generalization;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.content.Attribute;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.content.ClassContent;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.content.EnumItem;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.content.Method;
import raf.dsw.classycraft.app.core.treemodel.nodes.projNodes.Diagram;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
//connections reference their ends by id, so a loaded connection points at the diagram's own element, not a copy
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "id")
public abstract class Interclass extends DiagramElement implements Cloneable {

    Visibility vis;
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type")
    @JsonSubTypes({

            @JsonSubTypes.Type(value = Attribute.class, name = "atr"),
            @JsonSubTypes.Type(value = EnumItem.class, name = "ite"),
            @JsonSubTypes.Type(value = Method.class, name = "met")

    })
    List<ClassContent> elements=new ArrayList<>();
    Point pos;
    Dimension dim;

    protected Interclass() {
        pos=new Point();
        dim=new Dimension();
        vis=Visibility.PUBLIC;
    }

    public Point getPos() {
        return pos;
    }

    public Dimension getDim() {
        return dim;
    }

    public List<ClassContent> getElements() {
        return elements;
    }

    public void setElements(List<ClassContent> elements) {
        this.elements = elements;
        IChanged();
    }
    public void IChanged(){}
    public Interclass(String n, Visibility vis, Point pos, Dimension dim, Diagram d) {
        super(n,d);
        this.vis = vis;
        this.pos = pos;
        this.dim = dim;
    }

    public String maxStringGet(){
        return elements.stream().max(Comparator.comparingInt(x -> x.toString().length())).map(Object::toString).orElse(null);
    }

    public void setDim(Dimension dim) {
        this.dim = dim;
        IChanged();
    }
    public Interclass(Point pos, Diagram d){
        this("", Visibility.DEFAULT,pos,new Dimension(50,30),d);
    }


    public void setPos(Point pos) {
        this.pos = pos;
        IChanged();
    }
    public void trans(int dx,int dy){
        setPos(new Point(pos.x+dx,pos.y+dy) );
    }
    public abstract Interclass CopyGet();

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    //public abstract String title();
    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Interclass that = (Interclass) o;
        if(that.elements.size()!=this.elements.size()) return false;
        for (int i=0;i<elements.size();i++) {
            if(!elements.get(i).equals(that.elements.get(i))){
                return false;
            }
        }
        return true;
    }


}

