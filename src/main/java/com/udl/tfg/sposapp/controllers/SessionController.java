package com.udl.tfg.sposapp.controllers;

import com.udl.tfg.sposapp.models.Session;
import com.udl.tfg.sposapp.repositories.SessionRepository;
import com.udl.tfg.sposapp.repositories.VirtualMachineRepository;
import com.udl.tfg.sposapp.utils.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.util.HashMap;

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

    @Autowired
    private OCAManager ocaManager;

    @Value("${localStorageFolder}") private String localStorageFolder;
    @Value("${sshStorageFolder}")   private String sshStorageFolder;


    @RequestMapping(value = "/session/{id}", method = RequestMethod.GET)
    public @ResponseBody Session getSession(@PathVariable String id, @RequestParam(value = "key", required = true) String key) throws Exception {
        Session session = sessionRepository.findOne(Long.parseLong(id));
        if (session == null)
            throw new NullPointerException();

        if (session.getKey().equals(key)) {
            return session;
        } else {
            throw new InvalidKeyException();
        }
    }

    @RequestMapping(value = "/session/{id}/inputFiles", method = RequestMethod.GET)
    public @ResponseBody String getSessionFile(@PathVariable String id, @RequestParam(value = "key", required = true) String key) throws Exception {
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

    @RequestMapping(value = "/session/{id}/results", method = RequestMethod.GET)
    public @ResponseBody String[] getSessionResults(@PathVariable String id, @RequestParam(value = "key", required = true) String key) throws Exception {
        Session session = sessionRepository.findOne(Long.parseLong(id));
        if (session == null)
            throw new NullPointerException();

        if (session.getKey().equals(key)) {
                if (session.getResults() == null && session.getIP() != null) {
                    File f = new File(localStorageFolder + "/" + String.valueOf(id) + "/results.txt");
                    String content = readFile(f);
                    if (!content.isEmpty()) {
                        resultsParser.ParseResults(session, readFile(f));
                        resultsParser.ParseCharts(session);
                        //ocaManager.deleteVM(session.getVmConfig().getApiID());
                    }
                }

                if (session.getResults() == null) {
                    return new String[]{"","","","","-1"};
                } else {
                    return new String[]{
                            new String(session.getResults().getShortResults(), Charset.forName("UTF-8")),
                            new String(session.getResults().getFullResults(), Charset.forName("UTF-8")),
                            new String(session.getResults().getCpuData(), Charset.forName("UTF-8")),
                            new String(session.getResults().getMemData(), Charset.forName("UTF-8")),
                            String.valueOf(session.getResults().getFinishTime() - session.getResults().getStartTime())
                    };
                }
        } else {
            throw new InvalidKeyException();
        }
    }

    @RequestMapping(value = "/session", method = RequestMethod.POST)
    public @ResponseBody HttpEntity<HashMap<String, String>> returnKey(HttpServletRequest request, @Valid @RequestBody Session session) throws Exception {
        if (session != null) {
            session.generateKey();
            sessionRepository.save(session);
            String ip = "";
            try {
                ip = GetVMIp(session);
            } catch (Exception e) {
                sessionRepository.delete(session);
                throw new RuntimeException("VMERROR - There was an error creating the virtual machine. Please try again later. ERR: " + e.getMessage());
            }
            new RunExecutionThread(session, sessionRepository, executionManager, ocaManager, sshManager, localStorageFolder, sshStorageFolder, ip).start();
            return GeneratePostResponse(request, session);
        } else {
            throw new NullPointerException();
        }
    }

    @RequestMapping(value = "/session/{id}", method = RequestMethod.PUT)
    public @ResponseBody HttpEntity<Session> updateSession(@PathVariable String id, @Valid @RequestBody Session session, @RequestParam(value = "key", required = true) String key) throws Exception {
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
    public @ResponseBody HttpEntity<Void> deleteSession(@PathVariable String id, @RequestParam(value = "key", required = true) String key) throws Exception {
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

    private String readFile(File f) throws IOException {
        byte[] encoded = Files.readAllBytes(f.toPath());
        return new String(encoded, Charset.defaultCharset());
    }

    private HttpEntity<HashMap<String, String>> GeneratePostResponse(HttpServletRequest request, @Valid @RequestBody Session session) {
        HashMap<String, String> response = new HashMap<>();
        response.put("id", String.valueOf(session.getId()));
        response.put("key", session.getKey());
        return ResponseEntity.created(URI.create(request.getRequestURL() + "/" + session.getId())).body(response);
    }

    private String GetVMIp(Session session) {
        if (session.getIP() != null)
            return session.getIP();

        if (session.getVmConfig().getId() < 4)
            return getPredefinedVMIp(session.getVmConfig().getId());

        try {
            return ocaManager.createNewVM(session.getVmConfig());
        } catch (Exception e) {
            return null;
        }
    }

    private String getPredefinedVMIp(long id) {
        switch ((int)id) {
            case 1:
                return ocaManager.GetPredefinedVMIP(OCAManager.PredefinedVM.LOW);
            case 2:
                return ocaManager.GetPredefinedVMIP(OCAManager.PredefinedVM.MEDIUM);
            case 3:
                return ocaManager.GetPredefinedVMIP(OCAManager.PredefinedVM.HIGH);
            default:
                return null;
        }
    }
}
