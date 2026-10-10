package raf.dsw.classycraft.app.gui.swing.main.pkgview;

import raf.dsw.classycraft.app.controller.events.NodeChildrenClassyEvent;
import raf.dsw.classycraft.app.controller.events.PaintClassyEvent;
import raf.dsw.classycraft.app.core.msggen.IPublisher;
import raf.dsw.classycraft.app.core.msggen.ISubscriber;
import raf.dsw.classycraft.app.core.treemodel.nodes.abstractNodes.ClassyNode;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.DiagramElement;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.connections.Connection;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.entities.Interclass;
import raf.dsw.classycraft.app.core.treemodel.nodes.projNodes.Diagram;
import raf.dsw.classycraft.app.gui.swing.main.CommandManager;
import raf.dsw.classycraft.app.gui.swing.main.ConnectionAddCommand;
import raf.dsw.classycraft.app.gui.swing.painters.ConnectionPainter;
import raf.dsw.classycraft.app.gui.swing.painters.ElementPainter;
import raf.dsw.classycraft.app.gui.swing.painters.InterClassPainter;
import raf.dsw.classycraft.app.gui.swing.painters.factory.PainterSimpleFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

public class DiagramViewModel implements IPublisher, ISubscriber
{
    Diagram diag;
    List<ElementPainter> painters;
    List<ElementPainter> selPainters;
    Rectangle curSel;
    ConnectionPainter curConn;
    AffineTransform trans;
    CommandManager manager;
    MoveCommand activeMove;



    public void AddPainter(Interclass cls){
        AddPainter(PainterSimpleFactory.makePainter(cls));
    }
    public DiagramViewModel(Diagram d){
        diag=d;
        //d.addSubscriber(this);
        painters=new ArrayList<>();
        selPainters=new ArrayList<>();
        curSel=null;
        curConn=null;
        trans=new AffineTransform();
        manager=new CommandManager();
        initialize();

    }
    public void repaint(Rectangle r){
        notifySubscribers(new PaintClassyEvent(this,null));
    }
    public void initialize(){
        for (var e:diag.getChildren()) {
            AddPainter(PainterSimpleFactory.makePainter((DiagramElement) e));
        }
    }
    public Diagram getDiag(){
        return diag;
    }

    public void setCurConn(ConnectionPainter curConn) {
        this.curConn = curConn;
        var r= curConn!=null?curConn.getBounds():null;
        repaint(r);
    }

    public void AddPainter(ElementPainter cls){
        if(cls instanceof InterClassPainter i) {
            Point pos=i.getElement().getPos();
            pos= tWorldCoords(pos);
            i.getElement().setPos(pos);
        }
        painters.add(cls);
        repaint(cls.getBounds());
    }
    /** Adds already positioned (world coordinates) elements: one model event, one repaint. Entities are painted below connections. */
    public void addElements(List<DiagramElement> els){
        diag.AddChildren(new ArrayList<>(els));
        for (var e:els) {
            if(e instanceof Interclass)
                painters.add(PainterSimpleFactory.makePainter(e));
        }
        for (var e:els) {
            if(e instanceof Connection)
                painters.add(PainterSimpleFactory.makePainter(e));
        }
        repaint(null);
    }
    public void removeElements(List<DiagramElement> els){
        Set<ClassyNode> gone=Collections.newSetFromMap(new IdentityHashMap<>()); //equals() is name based
        gone.addAll(els);
        painters.removeIf(p->gone.contains(p.getElement()));
        selPainters.removeIf(p->gone.contains(p.getElement()));
        diag.removeChildren(new ArrayList<>(els));
        repaint(null);
    }
    private Point tWorldCoords(Point pos){
        Point2D p= null;
        try {
            p = trans.inverseTransform(pos,null);
        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException(e);
        }
        return new Point((int)p.getX(),(int)p.getY());
    }
    public void RemovePainter(ElementPainter p){
        RemoveAffilConn(p);
        painters.remove(p);
        selPainters.remove(p);
        p.getElement().removeSelf();
        repaint(p.getBounds());
    }
    public ElementPainter findPainter(ClassyNode node){
        for (var p:painters) {
            if(p.getElement().equals(node)){
                return p;
            }
        }
        return null;
    }
    private void RemoveAffilConn(ElementPainter p){
        for (ElementPainter painter : painters) {
            if (painter instanceof ConnectionPainter cp) {
                if (cp.getElement().getSource() == p.getElement() || cp.getElement().getDestination() == p.getElement()) {
                    RemovePainter(cp);
                    repaint(cp.getBounds());
                }
            }
        }
    }
    public void RemovePainters(List<ElementPainter> l){
        while (!l.isEmpty()){
            RemovePainter(l.get(0));;
        }
    }

    public void DeleteSelected() {
        RemovePainters(selPainters);
    }


    public void SelectElement(ElementPainter e){
        if(selPainters.contains(e))return;
        e.setSelected(true);
        selPainters.add(e);
        repaint(e.getBounds());
    }
    public void DeselectElement(ElementPainter e){
        e.setSelected(false);
        selPainters.remove(e);
        repaint(e.getBounds());
    }
    public void resetSelection(){
        while(!selPainters.isEmpty()) {
            DeselectElement(selPainters.get(0));
        }
    }

    public boolean SingleSel(){
        return selPainters.size()==1;
    }
    public ElementPainter getSelected(){
        return selPainters.get(0);
    }

    public ElementPainter underPointer(Point p){
        p=tWorldCoords(p);
        for (int i=painters.size()-1;i>=0;i--) {
            var pp= painters.get(i);
            if(pp.elementAt(p)){
                return pp;
            }
        }
        return null;
    }

    public Rectangle getCurSel() {
        return curSel;
    }
    public void setSelectRect(Rectangle r){
        var psel=curSel;
        curSel=r;
        Shape rt=null;
        if(psel!=null)
        {
            repaint(psel);
        }
        if(r!=null){

            try {
                rt= trans.createInverse().createTransformedShape(r);
            } catch (NoninvertibleTransformException e) {
                throw new RuntimeException(e);
            }

            for (ElementPainter e : painters) {
                if (rt.intersects(e.getBounds())) {
                    //if(e.isSelected())
                    //  DeselectElement(e);
                    //else
                    SelectElement(e);
                } //else if (e.isSelected()&&!additve) {
                // DeselectElement(e);
                //}
            }
            repaint(r);
        }

    }


    public void Fit(int w, int h) {
        Rectangle r=new Rectangle(0,0,w,h);
        FitToRect(r,getBoundRect());
        repaint(addRect(r,getBoundRect()));
    }
    public void FitToRect(Rectangle r1,Rectangle r){
        if(r==null)return;
        double scw=(double)r1.width/r.width;
        double sch=(double)r1.height/r.height;
        double scfact = Math.min(scw,sch);
        scfact=Math.max(scfact,0.2);
        scfact=Math.min(scfact,5);
        int dx1=(int)r.getCenterX();
        int dy1=(int)r.getCenterY();
        int dx2=r1.width/2+r1.x;
        int dy2=r1.height/2+r1.y;

        AffineTransform ntrans=new AffineTransform();
        ntrans.translate(+dx2,+dy2);
        ntrans.scale(scfact,scfact);
        ntrans.translate(-dx1,-dy1);
        trans=ntrans;


    }
    public Rectangle getBoundRect(){
        if(painters.isEmpty()) return null;
        Rectangle r= new Rectangle(painters.get(0).getBounds());
        for (var e:painters) {
            r=addRect(r,e.getBounds());
        }
        return r;
    }
    private Rectangle addRect(Rectangle r1,Rectangle r2){
        int minx,miny,maxx,maxy;
        minx=Math.min(r1.x,r2.x);
        miny=Math.min(r1.y,r2.y);
        maxx= (int)Math.max(r1.getMaxX(),r2.getMaxX());
        maxy= (int)Math.max(r1.getMaxY(),r2.getMaxY());

        int w=maxx-minx;
        int h=maxy-miny;
        return new Rectangle(minx,miny,w,h);
    }



    public void transView(int dx,int dy){
        trans.translate(dx,dy);
        repaint(null);
    }
    public void zoomView(double factor, Point pos){
        double currscale=trans.getScaleX();
        if(currscale>4&&factor>1||currscale<0.5&&factor<1) return;
        pos=tWorldCoords(pos);
        var newt=new AffineTransform();
        newt.translate((pos).x, (pos).y);
        newt.scale(factor, factor);
        newt.translate(-(pos).x,-(pos).y);
        trans.concatenate(newt);
        repaint(null);
    }

    public void transEl(ElementPainter e, int dx, int dy){
        transElements(List.of(e), dx, dy);
    }
    public void transElements(List<ElementPainter> le,int dx,int dy){
        if (activeMove != null) {
            activeMove.extend(dx, dy);
            return;
        }
        var movable = new ArrayList<ElementPainter>();
        for (var e:le) {
            if(e instanceof InterClassPainter)
                movable.add(e);
        }
        activeMove = new MoveCommand(this, movable, dx, dy, trans);
        manager.addCommand(activeMove);
    }
    public void transSel(int dx,int dy){
        transElements(selPainters,dx,dy);
    }
    // called on mouse release, next drag starts a new undo step
    public void endMove(){
        activeMove = null;
    }



    public void setConEnd(Point pos){
        pos=tWorldCoords(pos);
        curConn.setEndPoint(pos);
        var r= curConn!=null?curConn.getBounds():null;
        repaint(r);//?
    }
    public void letGo(Point pos) {
        var x=this.underPointer(pos);
        if(x!=null && x.getElement()!= curConn.getElement().getSource()) {

            curConn.getElement().setDestination(((Interclass) x.getElement()));
            manager.addCommand(new ConnectionAddCommand(curConn, this));
        }
        this.setCurConn(null);
    }


    public void Touch(Point pos){
        pos=tWorldCoords(pos);
        var s=underPointer(pos);
        if(s!=null){
            if(!s.isSelected()){
                resetSelection();
                SelectElement(s);
            }
        }
        else
            resetSelection();

    }
    public boolean isUnder(Point p){
        return underPointer(p)!=null;
    }


    public Point toViewCoords(Point pos){
        Point2D p= trans.transform(pos,null);
        return new Point((int)p.getX(),(int)p.getY());
    }



    List<ISubscriber> subs=new ArrayList<>();
    @Override
    public void addSubscriber(ISubscriber subscr) {
        subs.add(subscr);
    }

    @Override
    public void removeSubscriber(ISubscriber subscr) {
        subs.remove(subscr);
    }

    @Override
    public void notifySubscribers(Object notification) {
        for (var s:subs) {
            s.react(notification);
        }
    }

    @Override
    public void react(Object notification) {
//        if(notification instanceof NodeChildrenClassyEvent ech){
//            ech.reset();
//            while (ech.hasNextAdded()){
//                ClassyNode c=ech.getNextAdded();//diagram element
//                AddPainter(PainterSimpleFactory.makePainter(((DiagramElement) c)));
//            }
//            while(ech.hasNextRemoved()){
//                ClassyNode c=ech.getNextRemoved();
//                RemovePainter(findPainter(c));
//            }
//            repaint(null);
//
//        }
    }

    public CommandManager ManagerGet() {
        return manager;
    }
}
