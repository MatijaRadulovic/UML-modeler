package raf.dsw.classycraft.app.core.treemodel;

import raf.dsw.classycraft.app.core.treemodel.nodes.abstractNodes.ClassyNode;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.DiagramElement;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.connections.Connection;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.entities.Interclass;
import raf.dsw.classycraft.app.core.treemodel.nodes.projNodes.Diagram;

import java.awt.*;
import java.util.*;
import java.util.List;

/** Prepares the elements of a loaded template for insertion into an existing diagram. */
public final class TemplateInserter {
    private static final int GAP = 40;

    private TemplateInserter() {
    }

    /**
     * Re-parents the template's elements to {@code target}, renames those whose name is already taken
     * and moves them next to the existing content. Nothing is added to {@code target} yet.
     *
     * @return the elements to add, entities first and connections after them
     */
    public static List<DiagramElement> prepare(Diagram target, Diagram template) {
        List<Interclass> entities = new ArrayList<>();
        List<Connection> connections = new ArrayList<>();
        for (ClassyNode c : template.getChildren()) {
            if (c instanceof Interclass i)
                entities.add(i);
            else if (c instanceof Connection con)
                connections.add(con);
        }

        rename(target, entities, connections);
        move(target, entities);

        for (DiagramElement e : entities)
            e.setParent(target);
        for (DiagramElement e : connections)
            e.setParent(target);

        List<DiagramElement> res = new ArrayList<>(entities);
        res.addAll(connections);
        return res;
    }

    private static String key(ClassyNode node, String name) {
        return node.getClass().getName() + "/" + name;
    }

    private static void rename(Diagram target, List<Interclass> entities, List<Connection> connections) {
        Set<String> taken = new HashSet<>();
        for (ClassyNode c : target.getChildren())
            taken.add(key(c, c.getName()));

        Map<Interclass, String> oldNames = new IdentityHashMap<>();
        for (Interclass i : entities) {
            String old = i.getName() == null ? "" : i.getName();
            oldNames.put(i, old);
            String name = old;
            String base = old.replaceAll("\\d+$", "");
            for (int n = 1; taken.contains(key(i, name)); n++)
                name = base + n;
            i.setName(name);
            taken.add(key(i, name));
        }

        for (Connection con : connections) { //generated names ("A->B") follow the renamed ends
            String generated = oldNames.get(con.getSource()) + "->" + oldNames.get(con.getDestination());
            if (generated.equals(con.getName()))
                con.setName(con.getSource().getName() + "->" + con.getDestination().getName());
        }
    }

    /** Puts the template to the right of what is already in the diagram, top edges aligned. */
    private static void move(Diagram target, List<Interclass> entities) {
        List<Interclass> existing = new ArrayList<>();
        for (ClassyNode c : target.getChildren()) {
            if (c instanceof Interclass i)
                existing.add(i);
        }
        Rectangle old = bounds(existing);
        Rectangle tpl = bounds(entities);
        if (old == null || tpl == null)
            return;

        int dx = old.x + old.width + GAP - tpl.x;
        int dy = old.y - tpl.y;
        for (Interclass i : entities)
            i.setPos(new Point(i.getPos().x + dx, i.getPos().y + dy));
    }

    private static Rectangle bounds(List<Interclass> list) {
        Rectangle r = null;
        for (Interclass i : list) { //pos is the center
            Rectangle b = new Rectangle(i.getPos().x - i.getDim().width / 2, i.getPos().y - i.getDim().height / 2,
                    i.getDim().width, i.getDim().height);
            if (r == null)
                r = b;
            else
                r.add(b);
        }
        return r;
    }
}
