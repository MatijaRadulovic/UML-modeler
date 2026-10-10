package raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.content;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.connections.Aggregation;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.connections.Composition;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.connections.Dependency;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.connections.Generalization;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.entities.*;
import raf.dsw.classycraft.app.core.treemodel.nodes.projNodes.Diagram;
import raf.dsw.classycraft.app.core.treemodel.nodes.projNodes.Project;

import java.util.Objects;

@Getter
@Setter
public abstract class ClassContent {
    public ClassContent() {
    }

    protected String name;
    protected Visibility v;
    @JsonProperty("dataType") //"type" is taken by the type-id property
    protected String type;

    public ClassContent(String name, Visibility v, String type) {
        this.name = name;
        this.v = v;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassContent that = (ClassContent) o;
        return Objects.equals(name, that.name) && v == that.v && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, v, type);
    }
}
