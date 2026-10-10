package raf.dsw.classycraft.app.gui.swing.main;

import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.DiagramElement;
import raf.dsw.classycraft.app.gui.swing.main.pkgview.DiagramViewModel;

import java.util.List;

public class InsertTemplateCommand extends AbstractCommand{

    DiagramViewModel diagviewmodel;
    List<DiagramElement> elements;

    public InsertTemplateCommand(DiagramViewModel diagviewmodel, List<DiagramElement> elements) {
        this.diagviewmodel = diagviewmodel;
        this.elements = elements;
    }

    @Override
    public void doCommand() {
        diagviewmodel.addElements(elements);
    }

    @Override
    public void undoCommand() {
        diagviewmodel.removeElements(elements);
    }
}
