package com.udl.tfg.sposapp.utils;

import com.udl.tfg.sposapp.models.Result;
import com.udl.tfg.sposapp.models.Session;
import com.udl.tfg.sposapp.repositories.ResultRepository;
import com.udl.tfg.sposapp.repositories.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Service
public class ResultsParser {

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    ResultRepository resultRepository;

    @Autowired
    private SSHManager sshManager;

    public void ParseResults(Session session, String results) throws Exception {
        if (!results.equals("")) {
            Result executionResults = new Result();
           // executionResults.setFullResults(results.getBytes(Charset.forName("UTF-8")));
            switch (session.getInfo().getMethod().getMethod()) {
                case CPLEX:
                    parseCplex(session, results, executionResults);
                    break;
                case Gurobi:
                    parseGurobi(session, results, executionResults);
                    break;
                case Lpsolve:
                    parseLpsolve(session, results, executionResults);
                    break;
                default:
                    System.out.println("UNKNOWN METHOD");
                    break;
            }
            resultRepository.save(executionResults);
            session.setResults(executionResults);
            sessionRepository.save(session);
        }
    }

    private void parseCplex(Session session, String results, Result executionResults) throws Exception {
        if (session.getInfo().getFiles().size() > 1){
            parseOpl(session, results, executionResults);
        } else {
            parseMpsCplex(session, results, executionResults);
        }
    }

    private void parseOpl(Session session, String results, Result executionResults) {
        BufferedReader bufferedReader = new BufferedReader(new StringReader(results));
        boolean areResults = false;
        String shortResults = "";
        String fullResults = "";
        String line = null;
        int startTime = 0;
        int finishTime = 0;
        try {
            while ((line = bufferedReader.readLine()) != null){
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
                if (line.equals("+-+-+-+")) {
                    areResults = !areResults;
                    continue;
                }

                if (areResults && !line.startsWith("//")){
                    shortResults += line + "\n";
                }

                fullResults += line + "\n";
            }
            executionResults.setStartTime(startTime);
            executionResults.setFinishTime(finishTime);
            executionResults.setFullResults(fullResults.getBytes(StandardCharsets.UTF_8));
            if (shortResults.isEmpty()){
                executionResults.setShortResults(fullResults.getBytes(StandardCharsets.UTF_8));
            } else {
                executionResults.setShortResults(shortResults.getBytes(StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseMpsCplex(Session session, String results, Result executionResults) {
        BufferedReader bufferedReader = new BufferedReader(new StringReader(results));
        boolean areResults = false;
        String shortResults = "";
        String fullResults = "";
        String line = null;
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
                if (line.contains("MIP - Integer optimal")){
                    shortResults += line.substring(line.indexOf("Objective")) + "\n";
                }
                if (line.contains("Solution time")) {
                    shortResults += line.substring(0, line.indexOf("Iterations")) + "\n";
                }
                if (line.contains("Variable Name")) {
                    shortResults += "\n";
                    areResults = true;
                }
                if (areResults && line.contains("CPLEX>")) {
                    areResults = false;
                }
                if (areResults) {
                    shortResults += line + "\n";
                }
                fullResults += line + "\n";
            }
            executionResults.setStartTime(startTime);
            executionResults.setFinishTime(finishTime);
            executionResults.setFullResults(fullResults.getBytes(StandardCharsets.UTF_8));
            executionResults.setShortResults(shortResults.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseGurobi(Session session, String results, Result executionResults) {
        BufferedReader bufferedReader = new BufferedReader(new StringReader(results));
        String shortResults = "";
        String fullResults = "";
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
                fullResults += line + "\n";
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
                    if (var.length > 1 && Float.parseFloat(var[var.length - 1]) > 0) {
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
            executionResults.setFullResults(fullResults.getBytes(StandardCharsets.UTF_8));
            executionResults.setShortResults(shortResults.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseLpsolve(Session session, String results, Result executionResults)  {
        BufferedReader bufferedReader = new BufferedReader(new StringReader(results));
        boolean areResults = false;
        boolean existZeroVar = false;
        String shortResults = "";
        String fullResults = "";
        String line = null;
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
                fullResults += line + "\n";
                if (line.contains("Value of objective function")){
                    shortResults += line + "\n";
                }
                if (line.contains("Actual values of the variables")) {
                    areResults = true;
                    shortResults += "\n";
                    continue;
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
            executionResults.setFullResults(fullResults.getBytes(StandardCharsets.UTF_8));
            executionResults.setShortResults(shortResults.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void ParseCharts(Session session) {
        Result results = session.getResults();
        try {
            String cpuData = readFile("/home/sposApp/sessions/" + String.valueOf(session.getId()) + "/cpuData.txt").trim();
            String memData = readFile("/home/sposApp/sessions/" + String.valueOf(session.getId()) + "/memData.txt").trim();
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

    private String readFile(String filePath) throws IOException {
        File f = new File(filePath);
        if (f.exists())
        {
            byte[] encoded = Files.readAllBytes(f.toPath());
            return new String(encoded, Charset.defaultCharset());
        } else {
            return "";
        }
    }
}
