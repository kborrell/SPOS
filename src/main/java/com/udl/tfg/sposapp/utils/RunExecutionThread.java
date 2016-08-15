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

    public RunExecutionThread(Session session, SessionRepository sessionRepository,
                              ExecutionManager executionManager, OCAManager ocaManager,
                              SSHManager sshManager, String localStorageFolder, String sshStorageFolder) {
        this.sessionRepository = sessionRepository;
        this.executionManager = executionManager;
        this.ocaManager = ocaManager;
        this.sshManager = sshManager;
        this.session = session;
        this.localStorageFolder = localStorageFolder;
        this.sshStorageFolder = sshStorageFolder;
    }

    public void run() {
        try {
            session.setIP(GetVMIp());
            sessionRepository.save(session);
            SendFiles(session);
            executionManager.LaunchExecution(session);
        } catch (Exception e) {
            sessionRepository.delete(session);
        }
    }

    private String GetVMIp() {
        if (session.getIP() != null)
            return session.getIP();

        if (session.getVmConfig().getId() < 4)
            return getPredefinedVMIp();

        try {
            return ocaManager.createNewVM(session.getVmConfig());
        } catch (Exception e) {
            return null;
        }
    }

    private String getPredefinedVMIp() {
        switch ((int)session.getVmConfig().getId()) {
            case 0:
                return ocaManager.GetPredefinedVMIP(OCAManager.PredefinedVM.LOW);
            case 1:
                return ocaManager.GetPredefinedVMIP(OCAManager.PredefinedVM.MEDIUM);
            case 2:
                return ocaManager.GetPredefinedVMIP(OCAManager.PredefinedVM.HIGH);
            default:
                return null;
        }
    }

    private void SendFiles(Session session) throws Exception {
        sshManager.CloseSession();
        sshManager.OpenSession(session.getIP(), 22, "root");
        sshManager.CleanPath(sshStorageFolder + "/" + String.valueOf(session.getId()));
        for (int i=0; i < session.getInfo().getFiles().size(); i++){
            File file = saveFile(session, session.getInfo().getFiles().get(i));
            sshManager.SendFile(session.getId(), file);
        }
        sshManager.CloseSession();
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
