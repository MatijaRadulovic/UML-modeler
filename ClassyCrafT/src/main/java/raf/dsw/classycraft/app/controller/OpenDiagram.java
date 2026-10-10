package raf.dsw.classycraft.app.controller;

import raf.dsw.classycraft.app.core.ApplicationFramework;
import raf.dsw.classycraft.app.core.serializer.JacksonUtil;
import raf.dsw.classycraft.app.core.treemodel.TemplateInserter;
import raf.dsw.classycraft.app.core.treemodel.nodes.diagramNodes.DiagramElement;
import raf.dsw.classycraft.app.core.treemodel.nodes.projNodes.Diagram;
import raf.dsw.classycraft.app.gui.swing.main.InsertTemplateCommand;
import raf.dsw.classycraft.app.gui.swing.main.MainFrame;
import raf.dsw.classycraft.app.gui.swing.main.pkgview.DiagramView;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class OpenDiagram extends AbstractClassyAction{

    public OpenDiagram() {
        Icon icon=this.loadIcon("/images/oped.png");
        putValue(SMALL_ICON, icon);
        putValue(NAME, "Load template");
        putValue(SHORT_DESCRIPTION, "Load template");
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        DiagramView view=MainFrame.getInstance().getPkgpane().getActiveDiagramP(); //the diagram shown in the open tab
        if(view==null)
        {
            ApplicationFramework.getInstance().getMsgGen().generateErrorMessage("Šablon se dodaje u postojeći dijagram!");
            return;
        }

        JFileChooser fc=new JFileChooser();
        fc.setCurrentDirectory(JacksonUtil.templateDir());
        int result=fc.showOpenDialog(MainFrame.getInstance());
        if(result!=JFileChooser.APPROVE_OPTION)
            return;

        File selfile = fc.getSelectedFile();
        try {
            Diagram template= JacksonUtil.loadDiagram(selfile);
            var model=view.getModel();
            List<DiagramElement> elements=TemplateInserter.prepare(model.getDiag(),template);
            model.ManagerGet().addCommand(new InsertTemplateCommand(model,elements)); //undoable
        } catch (IOException ex) {
            String msg=String.valueOf(ex.getMessage()).split("\n")[0];
            ApplicationFramework.getInstance().getMsgGen().generateErrorMessage("Greška prilikom učitavanja fajla! "+msg);
            System.err.println(ex.toString());
        }
    }

}
