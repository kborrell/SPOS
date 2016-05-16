package com.udl.tfg.sposapp.utils;

import com.udl.tfg.sposapp.models.Result;
import com.udl.tfg.sposapp.models.Session;
import com.udl.tfg.sposapp.repositories.ResultRepository;
import com.udl.tfg.sposapp.repositories.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;

@Service
public class ResultsParser {

    @Autowired
    SessionRepository sessionRepository;

    @Autowired
    ResultRepository resultRepository;

    public void ParseResults(Session session, String results) throws Exception {
        Result executionResults = new Result();
        executionResults.setFullResults(results.getBytes(Charset.forName("UTF-8")));

        String shortResults;
        switch (session.getInfo().getMethod().getMethod()) {
            case CPLEX:
                shortResults = parseCplex(session, results);
                break;
            case Gurobi:
                shortResults = parseGurobi(session, results);
                break;
            case Lpsolve:
                shortResults = parseLpsolve(session, results);
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

    private String parseCplex(Session session, String results) throws Exception {
        if (session.getInfo().getFiles().size() > 1){
            return parseOpl(session, results);
        } else {
            return parseMpsCplex(session, results);
        }
    }

    private String parseOpl(Session session, String results) {
        BufferedReader bufferedReader = new BufferedReader(new StringReader(results));
        boolean areResults = false;
        String shortResults = "";
        String line = null;
        try {
            while ((line = bufferedReader.readLine()) != null){
                if (line.equals("+-+-+-+")) {
                    areResults = !areResults;
                    continue;
                }

                if (areResults && !line.startsWith("//")){
                    shortResults += line + "\n";
                }
            }
            return shortResults;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String parseMpsCplex(Session session, String results) {
        BufferedReader bufferedReader = new BufferedReader(new StringReader(results));
        boolean areResults = false;
        String shortResults = "";
        String line = null;
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
                if (areResults && line.contains("CPLEX>")) {
                    areResults = false;
                }
                if (areResults) {
                    shortResults += line + "\n";
                }
            }
            return shortResults;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String parseGurobi(Session session, String results) {
        BufferedReader bufferedReader = new BufferedReader(new StringReader(results));
        String shortResults = "";
        String line = null;
        boolean areResults = false;
        boolean existZeroVar = false;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("Explored")){
                    shortResults += "Execution time: " + line.substring(line.indexOf("in") + 2) + "\n";
                }
                if (line.contains("Best objective")) {
                    String[] data = line.split(",");
                    shortResults += String.join("\n", data);
                }

                if (line.contains("Objective value")) {
                    areResults = true;
                    shortResults += "\n\n" + line.substring(2) + "\n\n";
                    continue;
                }

                if (areResults){
                    String[] var = line.split(" ");
                    if (var.length > 0 && Integer.parseInt(var[var.length - 1]) > 0) {
                        shortResults += line + "\n";
                    } else {
                        existZeroVar = true;
                    }
                }
            }
            if (existZeroVar) {
                shortResults += "All other variables are 0.";
            }
            return shortResults;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    private String parseLpsolve(Session session, String results)  {
        BufferedReader bufferedReader = new BufferedReader(new StringReader(results));
        boolean areResults = false;
        boolean existZeroVar = false;
        String shortResults = "";
        String line = null;
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

                if (line.startsWith("real")) {
                    shortResults += "Execution times: \n";
                }

                if (areResults){
                    shortResults += line + "\n";
                }
            }
            return shortResults;
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
