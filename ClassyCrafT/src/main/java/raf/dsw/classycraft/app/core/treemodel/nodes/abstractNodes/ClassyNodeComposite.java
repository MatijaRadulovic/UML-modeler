package raf.dsw.classycraft.app.core.treemodel.nodes.abstractNodes;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.connections.Aggregation;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.connections.Composition;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.connections.Dependency;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.connections.Generalization;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.entities.ClassyClass;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.entities.ClassyEnum;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.entities.ClassyInterface;
import raf.dsw.classycraft.app.core.treemodel.nodes.projNodes.Diagram;
import raf.dsw.classycraft.app.core.treemodel.nodes.projNodes.Package;
import raf.dsw.classycraft.app.core.treemodel.nodes.projNodes.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
//@JsonTypeName("ncomp")
public abstract class ClassyNodeComposite extends ClassyNode{

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Project.class, name = "proj"),
            @JsonSubTypes.Type(value = Diagram.class, name = "diag"),
            @JsonSubTypes.Type(value = Package.class, name = "pack"),

            @JsonSubTypes.Type(value = ClassyClass.class, name = "cls"),
            @JsonSubTypes.Type(value = ClassyInterface.class, name = "inf"),
            @JsonSubTypes.Type(value = ClassyEnum.class, name = "enu"),

            @JsonSubTypes.Type(value = Aggregation.class, name = "agg"),
            @JsonSubTypes.Type(value = Generalization.class, name = "gen"),
            @JsonSubTypes.Type(value = Composition.class, name = "comp"),
            @JsonSubTypes.Type(value = Dependency.class, name = "dep"),



    })
    protected List<ClassyNode> children=new ArrayList<>();

    public ClassyNodeComposite(String name, ClassyNodeComposite parent) {
        super(name, parent);
    }

    public ClassyNodeComposite() {
    }

    public void AddChild(ClassyNode child){
        children.add((ClassyNode) child);
    }
    public void removeChild(ClassyNode child){
        children.remove(child);
    }

    public List<ClassyNode> getChildren() {
        return children;
    }

    public boolean isUnique(ClassyNode node){
        if(node.parent!=this)
            return true;
        for (ClassyNode nod:children) {
            if(nod!=node&&nod.getClass()==node.getClass() && nod.name.equals(node.name))
                return false;
        }
        return true;
    }
    public void NameChild(ClassyNode node){
        int num=1;
        do {
            node.setName(node.getClass().getSimpleName()+num++);
        }while (!isUnique(node));
    }

    public void parenthesise(){
        for (var c:children) {
            c.setParent(this);
            if(c instanceof ClassyNodeComposite comp){
                comp.parenthesise();
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ClassyNodeComposite that = (ClassyNodeComposite) o;
        if(that.children.size()!=this.children.size())
            return false;
        for (int i=0;i<that.children.size();i++) {
            if(! children.get(i).equals(that.children.get(i))){
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), children);
    }
}
