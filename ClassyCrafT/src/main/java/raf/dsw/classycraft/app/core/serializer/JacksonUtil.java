package raf.dsw.classycraft.app.core.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import raf.dsw.classycraft.app.core.treemodel.nodes.abstractNodes.ClassyNode;
import raf.dsw.classycraft.app.core.treemodel.nodes.abstractNodes.ClassyNodeComposite;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.connections.Connection;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.entities.Interclass;
import raf.dsw.classycraft.app.core.treemodel.nodes.projNodes.Diagram;
import raf.dsw.classycraft.app.core.treemodel.nodes.projNodes.Project;

import java.io.File;
import java.io.IOException;

public class JacksonUtil {
    /** Folder the open/save template dialogs start in; created when missing. */
    public static File templateDir() {
        File dir = new File("templates");
        if (!dir.isDirectory() && !dir.mkdirs())
            return new File(".");
        return dir;
    }

    public static Project loadProject(File file) throws IOException {
        ObjectMapper objmapper=new ObjectMapper();
        Project p=objmapper.readValue(file, Project.class);
        postLoad(p);
        return p;
    }
    public static void saveProject(File file, Project proj) throws IOException {
        ObjectMapper objmapper=new ObjectMapper();
        connectionsLast(proj);
        objmapper.writeValue(file,proj);
    }
    public static Diagram loadDiagram(File file) throws IOException {

        ObjectMapper objmapper=new ObjectMapper();
        Diagram d=objmapper.readValue(file, Diagram.class);
        postLoad(d);
        return d;
    }
    public static void saveDiagram(File file, Diagram diag) throws IOException {
        ObjectMapper objmapper=new ObjectMapper();
        connectionsLast(diag);
        objmapper.writeValue(file,diag);
    }

    /**
     * Connections refer to their ends by id, and the id must be defined before it is referenced.
     * Undo/redo can re-add an element after the connection that uses it, so put connections last (stable sort).
     */
    private static void connectionsLast(ClassyNodeComposite node) {
        node.getChildren().sort(java.util.Comparator.comparing(c -> c instanceof Connection));
        for (ClassyNode c : node.getChildren()) {
            if (c instanceof ClassyNodeComposite comp)
                connectionsLast(comp);
        }
    }

    /** Sets parents and makes sure every connection ends at an element of its own diagram. */
    private static void postLoad(ClassyNodeComposite root) throws IOException {
        root.parenthesise();
        checkConnections(root);
    }

    private static void checkConnections(ClassyNodeComposite node) throws IOException {
        for (ClassyNode c : node.getChildren()) {
            if (c instanceof Connection con) {
                checkEnd(node, con, con.getSource());
                checkEnd(node, con, con.getDestination());
            } else if (c instanceof ClassyNodeComposite comp) {
                checkConnections(comp);
            }
        }
    }

    /** Every connection must end at an element of its own diagram (compared by identity). */
    private static void checkEnd(ClassyNodeComposite diagram, Connection con, Interclass end) throws IOException {
        if (end == null)
            throw new IOException("Veza '" + con.getName() + "' nema oba kraja!");
        for (ClassyNode c : diagram.getChildren()) {
            if (c == end)
                return;
        }
        throw new IOException("Veza '" + con.getName() + "' pokazuje na element koji nije u dijagramu!");
    }

}
