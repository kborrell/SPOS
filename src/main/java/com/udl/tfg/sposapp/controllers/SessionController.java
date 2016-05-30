package com.udl.tfg.sposapp.controllers;

import com.udl.tfg.sposapp.models.DataFile;
import com.udl.tfg.sposapp.models.Session;
import com.udl.tfg.sposapp.models.VirtualMachine;
import com.udl.tfg.sposapp.repositories.SessionRepository;
import com.udl.tfg.sposapp.repositories.VirtualMachineRepository;
import com.udl.tfg.sposapp.utils.ExecutionManager;
import com.udl.tfg.sposapp.utils.OCAManager;
import com.udl.tfg.sposapp.utils.ResultsParser;
import com.udl.tfg.sposapp.utils.SSHManager;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
    private SessionRepository sessionRepository;

    @Autowired
    private VirtualMachineRepository vmRepository;

    @Autowired
    private SSHManager sshManager;

    @Autowired
    private ResultsParser resultsParser;

    @Autowired
    private ExecutionManager executionManager;

    @Value("${localStorageFolder}")
    private String localStorageFolder;

    @Value("${sshStorageFolder}")
    private String sshStorageFolder;

    @RequestMapping(value = "/session", method = RequestMethod.GET)
    public @ResponseBody HttpEntity<List<Session>> getSessions(HttpServletRequest request, @RequestParam(value = "key", required = false) String key) throws Exception {
        Iterable<Session> sessions = sessionRepository.findAll();
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

    @RequestMapping(value = "/session/{id}", method = RequestMethod.GET)
    public @ResponseBody Session getSession(@PathVariable String id, @RequestParam(value = "key", required = false) String key) throws Exception {
        Session session = sessionRepository.findOne(Long.parseLong(id));
        if (session == null)
            throw new NullPointerException();

        if (session.getKey().equals(key)) {
            return session;
        } else {
            throw new InvalidKeyException();
        }
    }

    @RequestMapping(value = "/session/{id}/getFile", method = RequestMethod.POST)
    public @ResponseBody String getSessionFile(@PathVariable String id, @RequestParam(value = "key", required = false) String key) throws Exception {
        Session session = sessionRepository.findOne(Long.parseLong(id));
        if (session == null)
            throw new NullPointerException();

        if (session.getKey().equals(key)) {
            String files = "";
            for (int i = 0; i < session.getInfo().getFiles().size(); i++) {
                files += session.getInfo().getFiles().get(i).getName() + "@" + new String(session.getInfo().getFiles().get(i).getContent(), StandardCharsets.UTF_8);
                if (i < session.getInfo().getFiles().size() - 1){
                    files += "^";
                }
            }
            return files;
        } else {
            throw new InvalidKeyException();
        }
    }

    @RequestMapping(value = "/session/{id}/getResults", method = RequestMethod.POST)
    public @ResponseBody String[] getSessionResults(@PathVariable String id, @RequestParam(value = "key", required = false) String key) throws Exception {
        Session session = sessionRepository.findOne(Long.parseLong(id));
        if (session == null)
            throw new NullPointerException();

        if (session.getKey().equals(key)) {
                File f = getFile(session.getId(), "results.txt");
                resultsParser.ParseResults(session, readFile(f));
                resultsParser.ParseCharts(session);
                return new String[]{
                    new String(session.getResults().getShortResults(), Charset.forName("UTF-8")),
                    new String(session.getResults().getFullResults(), Charset.forName("UTF-8"))
                };
        } else {
            throw new InvalidKeyException();
        }
    }

    @RequestMapping(value = "/session", method = RequestMethod.POST)
    public @ResponseBody HttpEntity<HashMap<String, String>> returnKey(HttpServletRequest request, @Valid @RequestBody Session session) throws Exception {
        if (session != null) {
            session.generateKey();
            sessionRepository.save(session);
            SendFiles(session);
            new Thread() {
                public void run() {
                    try {
                        session.setIP("192.168.101.113");
                        sessionRepository.save(session);
                        executionManager.LaunchExecution(session);
                    } catch (Exception e) {
                        sessionRepository.delete(session);
                    }
                }
            }.start();
            return GeneratePostResponse(request, session);
        } else {
            throw new NullPointerException();
        }
    }

    @RequestMapping(value = "/session/{id}", method = RequestMethod.PUT)
    public @ResponseBody HttpEntity<Session> updateSession(@PathVariable String id, @Valid @RequestBody Session session, @RequestParam(value = "key", required = false) String key) throws Exception {
        Session oldSession = sessionRepository.findOne(Long.parseLong(id));
        if (oldSession != null) {
            if (!oldSession.getKey().equals(key))
                throw new InvalidKeyException();
            oldSession.setEmail(session.getEmail());
            oldSession.setInfo(session.getInfo());
            oldSession.setMaximumDuration(session.getMaximumDuration());
            oldSession.setResults(session.getResults());
            oldSession.setType(session.getType());
            oldSession.setVmConfig(session.getVmConfig());
            sessionRepository.save(oldSession);
            return ResponseEntity.ok().body(oldSession);
        } else {
            throw new NullPointerException();
        }
    }

    @RequestMapping(value = "/session/{id}", method = RequestMethod.DELETE)
    public @ResponseBody HttpEntity<Void> deleteSession(@PathVariable String id, @RequestParam(value = "key", required = false) String key) throws Exception {
        Session session = sessionRepository.findOne(Long.parseLong(id));
        if (session != null) {
            if (!session.getKey().equals(key))
                throw new InvalidKeyException();
            sessionRepository.delete(Long.parseLong(id));
            return ResponseEntity.ok().build();
        } else {
            throw new NullPointerException();
        }
    }

    private void SendFiles(Session session) throws Exception {
        sshManager.OpenSession(GetVmIp(), 22, "root");
        sshManager.CleanPath(sshStorageFolder + "/" + String.valueOf(session.getId()));
        for (int i=0; i < session.getInfo().getFiles().size(); i++){
            File file = saveFile(session, session.getInfo().getFiles().get(i));
            sendFile(session.getId(), file);
        }
        sshManager.CloseSession();
    }

    private File saveFile(Session session, DataFile dataFile) throws IOException {
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

    private void sendFile(long id, File sourceFile) throws Exception {
        if (sourceFile == null)
            return;

        String destPath = sshStorageFolder + "/" + String.valueOf(id) + "/" + sourceFile.getName();
        sshManager.SendFile(sourceFile.getPath(), destPath);
    }

    private String readFile(File f) throws IOException {
        byte[] encoded = Files.readAllBytes(f.toPath());
        return new String(encoded, Charset.defaultCharset());
    }

    private String GetVmIp() {
        return "192.168.101.113";
//        List<Integer> vmIDs = ocaManager.GetAllVmIds();
//        if (vmIDs.size() > 0){
//            return ocaManager.GetIP(vmIDs.get(0));
//        }
//        return "";
    }

    private File getFile(long id, String fileName) throws Exception {
        String srcPath = sshStorageFolder + "/" + String.valueOf(id) + "/" + fileName;
        sshManager.OpenSession(GetVmIp(), 22, "root");
        File f = sshManager.ReceiveFile(srcPath, localStorageFolder + "/" + String.valueOf(id) + "/" + fileName);
        sshManager.CloseSession();
        return f;
    }

    private HttpEntity<HashMap<String, String>> GeneratePostResponse(HttpServletRequest request, @Valid @RequestBody Session session) {
        HashMap<String, String> response = new HashMap<>();
        response.put("id", String.valueOf(session.getId()));
        response.put("key", session.getKey());
        return ResponseEntity.created(URI.create(request.getRequestURL() + "/" + session.getId())).body(response);
    }
}
