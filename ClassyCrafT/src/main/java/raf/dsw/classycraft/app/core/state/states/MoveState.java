package raf.dsw.classycraft.app.core.state.states;

import raf.dsw.classycraft.app.core.state.State;
import raf.dsw.classycraft.app.gui.swing.main.pkgview.DiagramView;
import raf.dsw.classycraft.app.gui.swing.painters.ElementPainter;

import java.awt.*;
import java.awt.event.MouseEvent;

public class MoveState implements State {
    Point ppoint;
    boolean movEl =false;
    boolean movSel=false;
    ElementPainter movPe;
    @Override
    public void mouseDown(MouseEvent pos, DiagramView dview) {

    }

    @Override
    public void mouseUp(MouseEvent pos, DiagramView dview) {
        dview.getModel().endMove();
        ppoint=null;
        movEl =false;
        movSel=false;
    }

    @Override
    public void mouseDrag(MouseEvent epos, DiagramView dviewv) {
        var dview=dviewv.getModel();
        Point pos=epos.getPoint();

        if(ppoint==null)
        {
            ppoint=pos;
            var s=dview.underPointer(pos);
            if(s!=null){
                movEl=true;
                if(s.isSelected())
                {
                    movSel= true;
                }
                else {
                    movSel=false;
                    movPe=s;
                }
            }
            else
                movEl=false;
        }
        int dx=pos.x-ppoint.x;
        int dy=pos.y-ppoint.y;
        if(!movEl) {
            dview.transView(dx,dy);
        }
        if(movEl){

            if (movSel)
                dview.transSel(dx,dy);
            else
            {
                dview.transEl(movPe,dx,dy);
            }
        }
        ppoint=pos;

    }

    @Override
    public void mouseWheeled(MouseEvent e, DiagramView source) {

    }
}
