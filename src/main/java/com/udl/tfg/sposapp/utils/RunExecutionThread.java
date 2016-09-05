package com.udl.tfg.sposapp.utils;


import com.udl.tfg.sposapp.models.DataFile;
import com.udl.tfg.sposapp.models.Session;
import com.udl.tfg.sposapp.repositories.SessionRepository;
import org.apache.tomcat.util.http.fileupload.FileUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RunExecutionThread extends Thread {

    private SessionRepository sessionRepository;
    private ExecutionManager executionManager;
    private OCAManager ocaManager;
    private SSHManager sshManager;
    private Session session;

    private String localStorageFolder;
    private String sshStorageFolder;

    private String vmIP;

    public RunExecutionThread(Session session, SessionRepository sessionRepository,
                              ExecutionManager executionManager, OCAManager ocaManager,
                              SSHManager sshManager, String localStorageFolder, String sshStorageFolder, String vmIP) {
        this.sessionRepository = sessionRepository;
        this.executionManager = executionManager;
        this.ocaManager = ocaManager;
        this.sshManager = sshManager;
        this.session = session;
        this.localStorageFolder = localStorageFolder;
        this.sshStorageFolder = sshStorageFolder;
        this.vmIP = vmIP;
    }

    public void run() {
        try {
            System.out.println("Starting execution thread");
            System.out.println("1");
            CleanSessionPath();
            System.out.println("5");
            System.out.println("Cleaned");
            ocaManager.WaitUntilCreated(vmIP);
            System.out.println("Created");
            session.setIP(vmIP);
            sessionRepository.save(session);
            System.out.println("Saved IP");
            SendFiles(session);
            System.out.println("Sent files");
            executionManager.LaunchExecution(session);
        } catch (Exception e) {
            System.out.println("Error launching execution: " + e.getMessage());
            sessionRepository.delete(session);
        }
    }

    private void CleanSessionPath() {
        Path storagePath = Paths.get(localStorageFolder, String.valueOf(session.getId()));
        System.out.println("2");
        try {
            System.out.println("3");
            if (Files.exists(storagePath)){
                FileUtils.cleanDirectory(storagePath.toFile());
            }
            System.out.println("4");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void SendFiles(Session session) throws Exception {
        com.jcraft.jsch.Session sshSession = null;
        try {
            sshSession = sshManager.OpenSession(session.getIP(), 22, "root");
            sshManager.CleanPath(sshSession, sshStorageFolder + "/" + String.valueOf(session.getId()));
            System.out.println("Cleaned remote path");
            for (int i=0; i < session.getInfo().getFiles().size(); i++){
                File file = saveFile(session, session.getInfo().getFiles().get(i));
                System.out.println("File locally saved");
                sshManager.SendFile(sshSession, session.getId(), file);
                System.out.println("File remotely saved");
            }
            sshSession.disconnect();
        } catch (Exception e) {
            System.out.println("ERROR SENDING FILES " + e.getMessage());
            if (sshSession != null) sshSession.disconnect();
            throw new Exception(e);
        }
    }

    private File saveFile(com.udl.tfg.sposapp.models.Session session, DataFile dataFile) throws IOException {
        System.out.print("Saving file locally...");
        Path storagePath = Paths.get(localStorageFolder, String.valueOf(session.getId()), dataFile.getName());

        if (!Files.exists(storagePath.getParent())) {
            Files.createDirectories(storagePath.getParent());
        }

        File infoFile = storagePath.toFile();
        if (!infoFile.exists()) {
            infoFile.createNewFile();
        }

        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(infoFile));
        bufferedOutputStream.write(dataFile.getContent());
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
        System.out.println(" SAVED");
        return infoFile;
    }

}
