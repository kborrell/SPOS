package com.udl.tfg.sposapp.controllers;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sun.javaws.exceptions.InvalidArgumentException;
import com.sun.xml.internal.ws.api.message.ExceptionHasMessage;
import com.udl.tfg.sposapp.models.Session;
import com.udl.tfg.sposapp.repositories.SessionRepository;
import com.udl.tfg.sposapp.utils.SSHManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RepositoryRestController
public class SessionController {

    @Autowired
    private SessionRepository repository;

    @Autowired
    private SSHManager sshManager;

    @Value("${localStorageFolder}")
    private String localStorageFolder;

    @Value("${sshStorageFolder}")
    private String sshStorageFolder;

    @RequestMapping(value = "/session/{id}", method = RequestMethod.GET)
    public
    @ResponseBody
    Session getSession(@PathVariable String id, @RequestParam(value = "key", required = false) String key) throws Exception {
        Session session = repository.findOne(Long.parseLong(id));
        if (session == null)
            throw new NullPointerException();

        if (session.getKey().equals(key)) {
            return session;
        } else {
            throw new InvalidKeyException();
        }
    }

    @RequestMapping(value = "/session", method = RequestMethod.POST)
    @ResponseBody
    public HttpEntity<HashMap<String, String>> returnKey(HttpServletRequest request, @Valid @RequestBody Session session) throws Exception {
        if (session != null) {
            session.generateKey();
            repository.save(session);
            try {
                File sourceFile = saveInfoFile(session);
                sendInfoFile(session.getId(), sourceFile);
                return GeneratePostResponse(request, session);
            } catch (Exception e) {
                repository.delete(session);
                throw new Exception(e);
            }
        } else {
            throw new NullPointerException();
        }
    }

    private void sendInfoFile(long id, File sourceFile) throws Exception {
        if (sourceFile == null)
            return;

        String destPath = sshStorageFolder + "/" + String.valueOf(id) + "/" + sourceFile.getName();
        sshManager.OpenSession("192.168.101.79", 22, "root", "113321");
        sshManager.SendFile(sourceFile.getPath(), destPath);
    }

    private File saveInfoFile(Session session) throws IOException {
        Path storagePath = Paths.get(localStorageFolder, String.valueOf(session.getId()), session.getInfo().getInfoFileName());


        if (!Files.exists(storagePath.getParent())) {
            Files.createDirectories(storagePath.getParent());
        }

        File infoFile = storagePath.toFile();
        if (!infoFile.exists()) {
            infoFile.createNewFile();
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(infoFile));
        bw.write(session.getInfo().getInfoFileContent());
        bw.close();

        return infoFile;
    }

    private HttpEntity<HashMap<String, String>> GeneratePostResponse(HttpServletRequest request, @Valid @RequestBody Session session) {
        HashMap<String, String> response = new HashMap<>();
        response.put("id", String.valueOf(session.getId()));
        response.put("key", session.getKey());
        return ResponseEntity.created(URI.create(request.getRequestURL() + "/" + session.getId())).body(response);
    }

    @RequestMapping(value = "/session/{id}", method = RequestMethod.PUT)
    @ResponseBody
    public HttpEntity<Session> updateSession(@PathVariable String id, @Valid @RequestBody Session session, @RequestParam(value = "key", required = false) String key) throws Exception {
        Session oldSession = repository.findOne(Long.parseLong(id));
        if (oldSession != null) {
            if (!oldSession.getKey().equals(key))
                throw new InvalidKeyException();
            oldSession.setEmail(session.getEmail());
            oldSession.setInfo(session.getInfo());
            oldSession.setMaximumDuration(session.getMaximumDuration());
            oldSession.setSessionResults(session.getSessionResults());
            oldSession.setType(session.getType());
            oldSession.setVmConfig(session.getVmConfig());
            repository.save(oldSession);
            return ResponseEntity.ok().body(oldSession);
        } else {
            throw new NullPointerException();
        }
    }

    @RequestMapping(value = "/session/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public HttpEntity<Void> deleteSession(@PathVariable String id, @RequestParam(value = "key", required = false) String key) throws Exception {
        Session session = repository.findOne(Long.parseLong(id));
        if (session != null) {
            if (!session.getKey().equals(key))
                throw new InvalidKeyException();
            repository.delete(Long.parseLong(id));
            return ResponseEntity.ok().build();
        } else {
            throw new NullPointerException();
        }
    }

    @RequestMapping(value = "/session", method = RequestMethod.GET)
    public
    @ResponseBody
    HttpEntity<List<Session>> getSessions(HttpServletRequest request, @RequestParam(value = "key", required = false) String key) throws Exception {
        Iterable<Session> sessions = repository.findAll();
        if (sessions == null)
            throw new NullPointerException();

        List<Session> sessionsList = new ArrayList<>();
        for (Session actualSession : sessions) {
            if (actualSession.getKey().equals(key))
                sessionsList.add(actualSession);
        }

        if (sessionsList.size() == 0) {
            throw new InvalidKeyException();
        }

        return ResponseEntity.ok().body(sessionsList);
    }
}
