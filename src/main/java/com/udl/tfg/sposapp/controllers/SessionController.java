package com.udl.tfg.sposapp.controllers;

import com.udl.tfg.sposapp.models.DataFile;
import com.udl.tfg.sposapp.models.Parameters;
import com.udl.tfg.sposapp.models.Session;
import com.udl.tfg.sposapp.repositories.DataFileRepository;
import com.udl.tfg.sposapp.repositories.ParametersRepository;
import com.udl.tfg.sposapp.repositories.SessionRepository;
import com.udl.tfg.sposapp.utils.*;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

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
import java.util.Iterator;
import java.util.List;

@RepositoryRestController
public class SessionController {

    private static final String MOD_MAIN_METHOD = "main {\n" +
            "    var before = new Date();\n" +
            "\tvar temp = before.getTime();\n" +
            "\t\tthisOplModel.generate();\n" +
            "    \tif (cplex.solve()) {\n" +
            "    \t\t\twriteln(\"+-+-+-+\");\n" +
            "    \t\t\twriteln(\"-- Objective = \", cplex.getObjValue());\n" +
            "    \t\t\twriteln(\"-- Solution: \");\n" +
            " \t\t\t\twriteln(thisOplModel.printSolution());\n" +
            "                var after = new Date();\n" +
            "\t\t\t\twriteln(\"-- Solving time ~= \",after.getTime()-temp, \"ms\");   \t\n" +
            "        }else{\n" +
            "        \t\twriteln(\"ERROR - No Solution for this model!\");\n" +
            "        }\n" +
            "    writeln(\"+-+-+-+\"); \n" +
            "}";

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private DataFileRepository dataFileRepository;

    @Autowired
    private ParametersRepository parametersRepository;

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


    @RequestMapping(value = "/session", method = RequestMethod.POST)
    public @ResponseBody HttpEntity<HashMap<String, String>> returnKey(HttpServletRequest request, @Valid @RequestBody Session session) throws Exception {
        if (session != null) {
            System.out.print("Generating session key...");
            session.generateKey();
            sessionRepository.save(session);
            return GeneratePostResponse(request, session);
        } else {
            throw new NullPointerException();
        }
    }

    @RequestMapping(value = "/session/{id}/uploadFiles", method = RequestMethod.POST)
    public @ResponseBody HttpEntity<Void> upload(@PathVariable String id, MultipartHttpServletRequest request, @RequestParam(value = "key", required = true) String key) throws Exception {

        Session session = sessionRepository.findOne(Long.parseLong(id));
        if (session == null)
            throw new NullPointerException();

        System.out.println("Receiving files");
        if (session.getKey().equals(key)) {
            List<DataFile> dataFiles = ParseFiles(request, session);
            Parameters params = session.getInfo();
            params.setFiles(dataFiles);
            parametersRepository.save(params);
            session.setInfo(params);
            sessionRepository.save(session);
            System.out.println("Launching execution");
            LaunchExecution(session);
        } else {
            throw new InvalidKeyException();
        }
        return ResponseEntity.ok().build();
    }

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
            String contentSeparator = "//++//@*@//++//";
            String fileSeparator = "//++//@^@//++//";
            try {
                String files = "";
                for (int i = 0; i < session.getInfo().getFiles().size(); i++) {
                    byte[] content = Files.readAllBytes(Paths.get(session.getInfo().getFiles().get(i).getPath()));
                    files += session.getInfo().getFiles().get(i).getName() + contentSeparator + new String(content, StandardCharsets.UTF_8);
                    if (i < session.getInfo().getFiles().size() - 1){
                        files += fileSeparator;
                    }
                }
                return files;
            } catch (OutOfMemoryError e)
            {
                return "Preview not available" + contentSeparator + "Ops! Input file is too large, file preview is disabled.";
            }
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
            System.out.println("Getting results");
                if (session.getResults() == null && session.getIP() != null) {
                    File f = new File(localStorageFolder + "/" + String.valueOf(id) + "/results.txt");
                    if (f.exists())
                    {
                        String content = readFile(f);
                        if (!content.isEmpty()) {
                            System.out.println("Parsing results");
                            resultsParser.ParseResults(session, readFile(f));
                        }
                    }

                }
                if (session.getResults() != null && (session.getResults().getCpuData() == null || session.getResults().getMemData() == null)){
                    System.out.println("Parsing charts");
                    resultsParser.ParseCharts(session);
                }

                if (session.getResults() == null) {
                    return new String[]{"","","","","-1"};
                } else {
                    String shortResults = session.getResults().getShortResults() == null ? "" : new String(session.getResults().getShortResults(), Charset.forName("UTF-8"));
                    String fullResults = session.getResults().getFullResults() == null ? "" : new String(session.getResults().getFullResults(), Charset.forName("UTF-8"));
                    String cpuResults = session.getResults().getCpuData() == null ? "" : new String(session.getResults().getCpuData(), Charset.forName("UTF-8"));
                    String memResults = session.getResults().getMemData() == null ? "" : new String(session.getResults().getMemData(), Charset.forName("UTF-8"));
                    return new String[]{
                            shortResults,
                            fullResults,
                            cpuResults,
                            memResults,
                            String.valueOf(session.getResults().getFinishTime() - session.getResults().getStartTime())
                    };
                }
        } else {
            throw new InvalidKeyException();
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

    private void LaunchExecution(Session session) {
        String ip;
        try {
            ip = GetVMIp(session);
        } catch (Exception e) {
            System.out.println("Error getting ip: " + e.getMessage());
            sessionRepository.delete(session);
            throw new RuntimeException("VMERROR - There was an error creating the virtual machine. Please try again later. ERR: " + e.getMessage());
        }
        new RunExecutionThread(session, sessionRepository, executionManager, ocaManager, sshManager, sshStorageFolder, ip).start();
    }

    private List<DataFile> ParseFiles(MultipartHttpServletRequest request, Session session) throws IOException {
        Iterator<String> itr =  request.getFileNames();
        List<DataFile> dataFiles = new ArrayList<>();
        MultipartFile mpf;

        CleanSessionPath(session);

        while(itr.hasNext()){
            mpf = request.getFile(itr.next());

            DataFile df = new DataFile();
            String path = localStorageFolder + "/" + String.valueOf(session.getId()) + "/" + mpf.getOriginalFilename();
            df.setPath(path);
            df.setName(mpf.getOriginalFilename());
            df.setExtension(mpf.getOriginalFilename().split("\\.")[1]);

            byte[] content = mpf.getBytes();
            if (df.getExtension().toLowerCase().equals("mod"))
            {
                content = PrepareModFile(mpf.getBytes());
            }
            saveFile(df.getPath(), content);

            dataFileRepository.save(df);
            dataFiles.add(df);
        }

        return dataFiles;
    }

    private void CleanSessionPath(Session session) {
        Path storagePath = Paths.get(localStorageFolder, String.valueOf(session.getId()));
        try {
            if (Files.exists(storagePath)){
                FileUtils.cleanDirectory(storagePath.toFile());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File saveFile(String path, byte[] bytes) throws IOException {
        Path storagePath = Paths.get(path);

        if (!Files.exists(storagePath.getParent())) {
            Files.createDirectories(storagePath.getParent());
        }

        File infoFile = storagePath.toFile();
        if (!infoFile.exists()) {
            infoFile.createNewFile();
        }

        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(infoFile));
        bufferedOutputStream.write(bytes);
        bufferedOutputStream.flush();
        bufferedOutputStream.close();
        return infoFile;
    }

    private byte[] PrepareModFile(byte[] oldContent) {
        String content = new String(oldContent, StandardCharsets.UTF_8);
        if (!content.contains("main")){
            content = content + "\n" + MOD_MAIN_METHOD;
        }
        return content.getBytes(StandardCharsets.UTF_8);
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
            System.out.println("Error creating VM: " + e.getMessage());
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
