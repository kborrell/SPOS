package com.udl.tfg.sposapp.utils;

import com.udl.tfg.sposapp.models.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExecutionManager {

    @Autowired
    private SSHManager sshManager;

    private final Logger logger = LoggerFactory.getLogger(SSHManager.class);

    private String cplexMpsLp = "ts cplex-exec %1$s %2$s %3$s %4$s %5%s";
    private String cplexDatMod = "ts cplex-opl %1$s %2$s %3$s %4$s %5$s %6%s";
    private String gurobi = "ts gurobi-exec %1$s %2$s %3$s %4$s %5%s";
    private String lpsolveMPS = "ts lpsolve-mps %1$s %2$s %3$s %4$s %5%s";
    private String lpsolveLP = "ts lpsolve-lp %1$s %2$s %3$s %4$s %5%s";

    public void LaunchExecution(Session session) throws Exception {
        sshManager.OpenSession(session.getIP(), 22, "root");
        switch (session.getInfo().getMethod().getMethod()) {
            case CPLEX:
                runCplex(session);
                break;
            case Gurobi:
                runGurobi(session);
                break;
            case Lpsolve:
                runLpsolve(session);
                break;
            default:
                System.out.println("UNKNOWN METHOD");
                break;
        }
        sshManager.CloseSession();
    }

    private void runCplex(Session session) throws Exception {
        if (session.getInfo().getFiles().size() > 1){
            sshManager.ExecuteCommand(String.format(cplexDatMod, session.getId(), session.getKey(),
                    session.getEmail(), session.getInfo().getFiles().get(0).getName(), session.getInfo().getFiles().get(1).getName(), session.getMaximumDuration()));
        } else {
            sshManager.ExecuteCommand(String.format(cplexMpsLp, session.getId(), session.getKey(), session.getEmail(), session.getInfo().getFiles().get(0).getName(), session.getMaximumDuration()));
        }
    }

    private void runGurobi(Session session) throws Exception {
        sshManager.ExecuteCommand(String.format(gurobi, session.getId(), session.getKey(), session.getEmail(), session.getInfo().getFiles().get(0).getName(), session.getMaximumDuration()));
    }

    private void runLpsolve(Session session) throws Exception {
        if (session.getInfo().getFiles().get(0).getExtension().equals("mps")){
            sshManager.ExecuteCommand(String.format(lpsolveMPS, session.getId(), session.getKey(), session.getEmail(), session.getInfo().getFiles().get(0).getName(), session.getMaximumDuration()));
        } else {
            sshManager.ExecuteCommand(String.format(lpsolveLP, session.getId(), session.getKey(), session.getEmail(), session.getInfo().getFiles().get(0).getName(), session.getMaximumDuration()));
        }
    }
}
