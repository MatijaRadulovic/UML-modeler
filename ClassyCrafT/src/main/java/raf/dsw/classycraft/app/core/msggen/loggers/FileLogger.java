package raf.dsw.classycraft.app.core.msggen.loggers;

import raf.dsw.classycraft.app.core.ApplicationFramework;
import raf.dsw.classycraft.app.core.msggen.Message;

import java.io.*;

public class FileLogger implements Logger{
    File logfile;

    public FileLogger() {
        ApplicationFramework.getInstance().getMsgGen().addSubscriber(this);

        logfile = new File("logs/log.txt");
        File dir = logfile.getParentFile();
        if (dir != null && !dir.exists() && !dir.mkdirs())
            System.err.println("Could not create log directory: " + dir.getAbsolutePath());
    }

    @Override
    public void react(Object notification) {
        log((Message) notification);
    }

    @Override
    public void log(Message msg) {

        try(FileWriter fw = new FileWriter(logfile, true); BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(msg.toString());
            bw.newLine();
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
        }

    }
}
