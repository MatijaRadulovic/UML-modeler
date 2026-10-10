package raf.dsw.classycraft.app.gui.swing.painters;

import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.entities.Interclass;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

public class InterClassPainter extends ElementPainter{

    @Override
    public Interclass getElement() {
        return (Interclass) element;
    }

    public Point getCentar(){
        return ((Interclass) element).getPos();
    }
    public Dimension getDim(){
        return ((Interclass) element).getDim();
    }

    public Point getTopLeft(){
        return   new Point(getCentar().x-getDim().width/2,getCentar().y-getDim().height/2);
    }
    public InterClassPainter(Interclass cls){
        this.element= cls;
        layout();
    }
    private RoundRectangle2D makeShape(){ //always built from the element, so it follows moves and resizes
        return new RoundRectangle2D.Float(getTopLeft().x,getTopLeft().y,getDim().width,getDim().height,10,10);
    }
    @Override
    public Rectangle getBounds() {
        return makeShape().getBounds();
    }
    @Override
    public boolean elementAt(Point pos) {
        return makeShape().contains(pos);
    }
    @Override
    public void paint(Graphics2D g) {
        Color c=element.ColGet();
        if(isSelected)
            c=c.darker();

        RoundRectangle2D shape=makeShape();
        float ox=3*contPadd+getTopLeft().x,oy=2*contPadd+getTopLeft().y;
        float w=getDim().width,h=getDim().height;
        float cx=getTopLeft().x+w/2;

        g.setColor(c);
        g.fill(shape);

        g.setColor(textCol);
        g.setFont(font);

        String s=element.toString();
        Dimension d=getStringDim(g,s);
        oy+=d.height;
        g.drawString(s,cx-d.width/2f,oy);
        oy+= contPadd*2;

        for (var ce: getElement().getElements()) {
            oy+=d.height;
            g.drawString(ce.toString(),ox,oy);
            oy+=contPadd;
        }

        g.setColor(c.darker());
        g.setStroke(stroke);
        g.draw(shape);

        if(isSelected)
            drawSelRect(g,shape.getBounds2D());

    }

    private Dimension getStringDim(Graphics2D g, String s){
        if(s==null) return new Dimension();
        FontMetrics fm=g.getFontMetrics(font);
        return new Dimension(fm.stringWidth(s),fm.getHeight());
    }
    static int contPadd=2;
    static Color textCol=Color.BLACK;
    static Stroke stroke=new BasicStroke(3);
    static Font font=new Font(Font.MONOSPACED,Font.PLAIN,10);

    /** Sizes the element to its content. Call when the content changes, not while painting. */
    public void layout(){
        int w=metrics.stringWidth(element.toString());
        if(getElement().getElements()!=null)
            for (var c: getElement().getElements()) {
                w=Math.max(w,metrics.stringWidth(c.toString()));
            }
        w+=6*contPadd;
        int h=(metrics.getHeight()+contPadd)*(getElement().getElements().size()+1)+6*contPadd;
        Dimension dim=new Dimension(w,h);
        if(!dim.equals(getDim()))
            getElement().setDim(dim);
    }

    //measured on an offscreen image, so the size does not depend on the zoom of the view that paints it
    private static final FontMetrics metrics=createMetrics();
    private static FontMetrics createMetrics(){
        Graphics2D g=new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB).createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        return g.getFontMetrics(font);
    }

}
