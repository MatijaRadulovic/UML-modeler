package raf.dsw.classycraft.app.core.treemodel.nodes.abstractNodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.connections.Aggregation;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.connections.Composition;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.connections.Dependency;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.connections.Generalization;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.content.Attribute;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.content.EnumItem;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.content.Method;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.entities.ClassyClass;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.entities.ClassyEnum;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.entities.ClassyInterface;
import raf.dsw.classycraft.app.core.treemodel.nodes.projNodes.Diagram;
import raf.dsw.classycraft.app.core.treemodel.nodes.projNodes.Project;

import java.util.Objects;
@Getter
@Setter
public  abstract class ClassyNode implements Cloneable {



    protected String name;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name=name;
    }

    protected transient ClassyNodeComposite parent;

    public void setParent(ClassyNodeComposite parent) {
        this.parent=parent;
    }

    @JsonIgnore
    public ClassyNode getParent() {
        return parent;
    }

    public ClassyNode(String name, ClassyNodeComposite parent) {
        this.name = name;
        this.parent = parent;
    }

    public ClassyNode() {
    }

    public String FullNameGet() {
        return parent!=null? parent.FullNameGet()+name+"/" : "/";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassyNode that = (ClassyNode) o;
        return this.FullNameGet().equals(that.FullNameGet());
    }
    public void removeSelf(){
        parent.removeChild(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getClass().getName(), FullNameGet()); //same fields as equals(), parent/children are not hashed (cycle)
    }
}
