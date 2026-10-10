package raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.connections;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.entities.Interclass;
import raf.dsw.classycraft.app.core.treemodel.nodes.projNodes.Diagram;
@Getter
@Setter
@JsonTypeName("agg")
public class Aggregation extends Connection{
    public Aggregation(Interclass source, String name, Diagram d) {
        super(source, name, d);
    }
    public Aggregation(){

    }


}
