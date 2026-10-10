package raf.dsw.classycraft.app.core.state.states;

import raf.dsw.classycraft.app.core.state.State;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.entities.Interclass;
import raf.dsw.classycraft.app.gui.swing.main.MainFrame;
import raf.dsw.classycraft.app.gui.swing.main.pkgview.DiagramView;
import raf.dsw.classycraft.app.gui.swing.popups.EditEntDialog;

import java.awt.event.MouseEvent;

public class EditContentState implements State {

    @Override
    public void mouseDown(MouseEvent pos, DiagramView dviewv) {
        var dview=dviewv.getModel();
        //dview.resetSelection();
        var e=dview.underPointer(pos.getPoint());
        if(e==null)return;
        Interclass ic= ((Interclass) e.getElement());
        EditEntDialog d=EditEntDialog.getDialog(ic);
        d.ShowDialog(MainFrame.getInstance());
        dview.relayout();
    }

    @Override
    public void mouseUp(MouseEvent pos, DiagramView dview) {

    }

    @Override
    public void mouseDrag(MouseEvent pos, DiagramView dview) {

    }

    @Override
    public void mouseWheeled(MouseEvent e, DiagramView source) {

    }
}
