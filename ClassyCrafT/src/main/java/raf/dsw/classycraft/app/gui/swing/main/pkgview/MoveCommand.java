package raf.dsw.classycraft.app.gui.swing.main.pkgview;

import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.entities.Interclass;
import raf.dsw.classycraft.app.gui.swing.main.AbstractCommand;
import raf.dsw.classycraft.app.gui.swing.painters.ElementPainter;

import java.awt.geom.AffineTransform;
import java.util.List;

/**
 * One whole drag gesture. Every mouse-drag step extends the same command,
 * so undo/redo moves the elements by the total distance in one go.
 */
public class MoveCommand extends AbstractCommand {

    DiagramViewModel diagviewmodel;
    List<ElementPainter> elements;
    AffineTransform trans;
    // accumulated movement in world coords
    int dx;
    int dy;

    public MoveCommand(DiagramViewModel diagviewmodel, List<ElementPainter> elements, int dx, int dy, AffineTransform trans) {
        this.diagviewmodel = diagviewmodel;
        this.elements = elements;
        this.trans = trans;
        this.dx = toWorld(dx);
        this.dy = toWorld(dy);
    }

    private int toWorld(int d) {
        return (int) (d / trans.getScaleX());
    }

    // add one more drag step (screen coords) and apply it right away
    public void extend(int sdx, int sdy) {
        int wx = toWorld(sdx);
        int wy = toWorld(sdy);
        dx += wx;
        dy += wy;
        translate(wx, wy);
    }

    private void translate(int wx, int wy) {
        for (var e : elements)
            ((Interclass) e.getElement()).trans(wx, wy);
        diagviewmodel.repaint(null);
    }

    @Override
    public void doCommand() {
        translate(dx, dy);
    }

    @Override
    public void undoCommand() {
        translate(-dx, -dy);
    }
}
