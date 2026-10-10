package raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.content;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeName("ite")
public class EnumItem extends ClassContent {
    public EnumItem() {
    }

    public EnumItem(String n) {
        super(n,null,null);
        name = n;
    }

    @Override
    public String toString() {
        return name.toUpperCase();
    }
}
