package com.udl.tfg.sposapp.handlers;

import com.udl.tfg.sposapp.models.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@RepositoryEventHandler(Session.class)
public class SessionEventHandler {

    @Value("${storageFolder}")
    private String storageFolder;

    @HandleBeforeCreate
    public void handleSessionBeforeCreate(Session session){
        saveInfoFile(session);
    }

    private void saveInfoFile(Session session) {
        Path storagePath = Paths.get(storageFolder, String.valueOf(session.getId()), session.getInfo().getInfoFileName());

        try {
            if (!Files.exists(storagePath.getParent())){
                Files.createDirectories(storagePath.getParent());
            }

            File infoFile = storagePath.toFile();
            if (!infoFile.exists()){
                infoFile.createNewFile();
            }

            BufferedWriter bw = new BufferedWriter(new FileWriter(infoFile));
            bw.write(session.getInfo().getInfoFileContent());
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
