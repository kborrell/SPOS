package com.udl.tfg.sposapp.utils;


import com.udl.tfg.sposapp.models.DataFile;
import com.udl.tfg.sposapp.models.Session;
import com.udl.tfg.sposapp.repositories.SessionRepository;

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
            CleanSessionPath();
            ocaManager.WaitUntilCreated(vmIP);
            session.setIP(vmIP);
            sessionRepository.save(session);
            SendFiles(session);
            executionManager.LaunchExecution(session);
        } catch (Exception e) {
            sessionRepository.delete(session);
        }
    }

    private void CleanSessionPath() {
//        Path storagePath = Paths.get(localStorageFolder, String.valueOf(session.getId()));
//        try {
//            FileUtils.cleanDirectory(storagePath.toFile());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        com.jcraft.jsch.Session sshSession = null;
        try {
             sshSession = sshManager.OpenSession("192.168.101.113", 22, "root");
            sshManager.CleanPath(sshSession, sshStorageFolder + "/" + String.valueOf(session.getId()));
            sshSession.disconnect();
        } catch (Exception e) {
            if (sshSession!= null) sshSession.disconnect();
            e.printStackTrace();
        }
    }

    private void SendFiles(Session session) throws Exception {
        com.jcraft.jsch.Session sshSession = null;
        try {
            sshSession = sshManager.OpenSession(session.getIP(), 22, "root");
            sshManager.CleanPath(sshSession, sshStorageFolder + "/" + String.valueOf(session.getId()));
            for (int i=0; i < session.getInfo().getFiles().size(); i++){
                File file = saveFile(session, session.getInfo().getFiles().get(i));
                sshManager.SendFile(sshSession, session.getId(), file);
            }
            sshSession.disconnect();

            sshSession = sshManager.OpenSession("192.168.101.113", 22, "root");
            sshManager.CleanPath(sshSession, sshStorageFolder + "/" + String.valueOf(session.getId()));
            for (int i=0; i < session.getInfo().getFiles().size(); i++){
                File file = saveFile(session, session.getInfo().getFiles().get(i));
                sshManager.SendFile(sshSession, session.getId(), file);
            }
            sshSession.disconnect();
        } catch (Exception e) {
            if (sshSession != null) sshSession.disconnect();
            throw new Exception(e);
        }
    }

    private File saveFile(com.udl.tfg.sposapp.models.Session session, DataFile dataFile) throws IOException {
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
        return infoFile;
    }

}
