package com.udl.tfg.sposapp.utils;

import com.udl.tfg.sposapp.models.Result;
import com.udl.tfg.sposapp.models.Session;
import com.udl.tfg.sposapp.repositories.ResultRepository;
import com.udl.tfg.sposapp.repositories.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;

@Service
public class ResultsParser {

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    ResultRepository resultRepository;

    @Autowired
    private SSHManager sshManager;

    @Value("${localStorageFolder}") private String localStorageFolder;

    public void ParseResults(Session session, String results) throws Exception {
        if (!results.equals("")) {
            Result executionResults = new Result();
            executionResults.setFullResults(results.getBytes(Charset.forName("UTF-8")));
            String shortResults;
            switch (session.getInfo().getMethod().getMethod()) {
                case CPLEX:
                    shortResults = parseCplex(session, results, executionResults);
                    break;
                case Gurobi:
                    shortResults = parseGurobi(session, results, executionResults);
                    break;
                case Lpsolve:
                    shortResults = parseLpsolve(session, results, executionResults);
                    break;
                default:
                    shortResults = "";
                    System.out.println("UNKNOWN METHOD");
                    break;
            }

            executionResults.setShortResults(shortResults.getBytes(Charset.forName("UTF-8")));
            resultRepository.save(executionResults);
            session.setResults(executionResults);
            sessionRepository.save(session);
        }
    }

    private String parseCplex(Session session, String results, Result executionResults) throws Exception {
        if (session.getInfo().getFiles().size() > 1){
            return parseOpl(session, results, executionResults);
        } else {
            return parseMpsCplex(session, results, executionResults);
        }
    }

    private String parseOpl(Session session, String results, Result executionResults) {
        BufferedReader bufferedReader = new BufferedReader(new StringReader(results));
        boolean areResults = false;
        String shortResults = "";
        String line = null;
        int startTime = 0;
        int finishTime = 0;
        try {
            while ((line = bufferedReader.readLine()) != null){
                if (line.contains("StartTime: ")){
                    String time = line.substring(11);
                    startTime = Integer.parseInt(time);
                }
                if (line.contains("FinishTime: ")){
                    String time = line.substring(12);
                    finishTime = Integer.parseInt(time);
                }
                if (line.equals("+-+-+-+")) {
                    areResults = !areResults;
                    continue;
                }

                if (areResults && !line.startsWith("//")){
                    shortResults += line + "\n";
                }
            }
            executionResults.setStartTime(startTime);
            executionResults.setFinishTime(finishTime);
            return shortResults;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String parseMpsCplex(Session session, String results, Result executionResults) {
        BufferedReader bufferedReader = new BufferedReader(new StringReader(results));
        boolean areResults = false;
        String shortResults = "";
        String line = null;
        int startTime = 0;
        int finishTime = 0;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("MIP - Integer optimal solution")){
                    shortResults += line.substring(line.indexOf("Objective")) + "\n";
                }
                if (line.contains("Solution time")) {
                    shortResults += line.substring(0, line.indexOf("Iterations")) + "\n";
                }
                if (line.contains("Variable Name")) {
                    shortResults += "\n";
                    areResults = true;
                }
                if (line.contains("StartTime: ")){
                    String time = line.substring(11);
                    startTime = Integer.parseInt(time);
                }
                if (line.contains("FinishTime: ")){
                    String time = line.substring(12);
                    finishTime = Integer.parseInt(time);
                }
                if (areResults && line.contains("CPLEX>")) {
                    areResults = false;
                }
                if (areResults) {
                    shortResults += line + "\n";
                }
            }
            executionResults.setStartTime(startTime);
            executionResults.setFinishTime(finishTime);
            return shortResults;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String parseGurobi(Session session, String results, Result executionResults) {
        BufferedReader bufferedReader = new BufferedReader(new StringReader(results));
        String shortResults = "";
        String line = null;
        boolean areResults = false;
        boolean existZeroVar = false;
        int startTime = 0;
        int finishTime = 0;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("StartTime: ")){
                    String time = line.substring(11);
                    startTime = Integer.parseInt(time);
                    continue;
                }
                if (line.contains("FinishTime: ")){
                    String time = line.substring(12);
                    finishTime = Integer.parseInt(time);
                    continue;
                }
                if (line.contains("Explored")){
                    shortResults += "Execution time: " + line.substring(line.indexOf("in") + 2) + "\n";
                    continue;
                }
                if (line.contains("Best objective")) {
                    String[] data = line.split(",");
                    shortResults += String.join("\n", data);
                    continue;
                }

                if (line.contains("Objective value")) {
                    areResults = true;
                    shortResults += "\n\n" + line.substring(2) + "\n\n";
                    continue;
                }

                if (areResults){
                    String[] var = line.split(" ");
                    if (var.length > 1 && Integer.parseInt(var[var.length - 1]) > 0) {
                        shortResults += line + "\n";
                    } else {
                        existZeroVar = true;
                    }
                }
            }
            if (existZeroVar) {
                shortResults += "All other variables are 0.";
            }
            executionResults.setStartTime(startTime);
            executionResults.setFinishTime(finishTime);
            return shortResults;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String parseLpsolve(Session session, String results, Result executionResults)  {
        BufferedReader bufferedReader = new BufferedReader(new StringReader(results));
        boolean areResults = false;
        boolean existZeroVar = false;
        String shortResults = "";
        String line = null;
        int startTime = 0;
        int finishTime = 0;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("Value of objective function")){
                    shortResults += line + "\n";
                }
                if (line.contains("Actual values of the variables")) {
                    areResults = true;
                    shortResults += "\n";
                    continue;
                }
                if (line.contains("StartTime: ")){
                    String time = line.substring(11);
                    startTime = Integer.parseInt(time);
                }
                if (line.contains("FinishTime: ")){
                    String time = line.substring(12);
                    finishTime = Integer.parseInt(time);
                }
                if (line.startsWith("real")) {
                    shortResults += "Execution times: \n";
                }

                if (areResults){
                    shortResults += line + "\n";
                }
            }
            executionResults.setStartTime(startTime);
            executionResults.setFinishTime(finishTime);
            return shortResults;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void ParseCharts(Session session) {
        Result results = session.getResults();
        try {
            File cpuFile = new File(localStorageFolder + "/" + String.valueOf(session.getId()) + "/cpuData.txt");
            File memFile = new File(localStorageFolder + "/" + String.valueOf(session.getId()) + "/memData.txt");
            String cpuData = readFile(cpuFile).trim();
            String memData = readFile(memFile).trim();
            if (cpuData.length() > 0 && memData.length() > 0){
                results.setCpuData(cpuData.getBytes(Charset.forName("UTF-8")));
                results.setMemData(memData.getBytes(Charset.forName("UTF-8")));
                resultRepository.save(results);
                session.setResults(results);
                sessionRepository.save(session);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String readFile(File f) throws IOException {
        byte[] encoded = Files.readAllBytes(f.toPath());
        return new String(encoded, Charset.defaultCharset());
    }
}
