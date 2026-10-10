package raf.dsw.classycraft.app.controller;

import raf.dsw.classycraft.app.core.ApplicationFramework;
import raf.dsw.classycraft.app.core.serializer.JacksonUtil;
import raf.dsw.classycraft.app.core.treemodel.nodes.projNodes.Diagram;
import raf.dsw.classycraft.app.gui.swing.main.MainFrame;
import raf.dsw.classycraft.app.gui.swing.treeview.model.ClassyTreeItem;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

public class SaveDiagram extends AbstractClassyAction{
    public SaveDiagram() {
        Icon icon=this.loadIcon("/images/savd.png");
        putValue(SMALL_ICON, icon);
        putValue(NAME, "Save as template");
        putValue(SHORT_DESCRIPTION, "Save template");
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        ClassyTreeItem selitem= MainFrame.getInstance().getTree().getSelectedNode();
        if(selitem!=null && selitem.getNode() instanceof Diagram d){

            JFileChooser fileChooser = new JFileChooser();
            File fil=null;
            fileChooser.setCurrentDirectory(JacksonUtil.templateDir());
            if (fileChooser.showSaveDialog(MainFrame.getInstance()) == JFileChooser.APPROVE_OPTION) {
                fil = fileChooser.getSelectedFile();
            }
            if(fil==null)return;
            try {
                JacksonUtil.saveDiagram(fil,d);
            } catch (IOException ex) {
                ApplicationFramework.getInstance().getMsgGen().generateErrorMessage("Greška prilikom čuvanja!");
                System.err.println(ex.toString());
            }


        }
        else {
            ApplicationFramework.getInstance().getMsgGen().generateErrorMessage("Greška prilikom čuvanja stavke!");
        }
    }
}
