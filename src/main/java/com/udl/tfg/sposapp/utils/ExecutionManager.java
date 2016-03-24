package com.udl.tfg.sposapp.utils;

import com.jcraft.jsch.JSch;
import com.udl.tfg.sposapp.models.MethodCodes;
import com.udl.tfg.sposapp.models.MethodInfo;
import com.udl.tfg.sposapp.models.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringArrayPropertyEditor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExecutionManager {

    @Autowired
    private static SSHManager sshManager;

    private static final Logger logger = LoggerFactory.getLogger(SSHManager.class);

    private static String cplexMpsLp = "";//cplex -c \"read {0}\" \"optimize\" \"display solution variables -\" >> {1}";
    private static String cplexDatMod = "cplex-opl {0} {1} {2}";

    public static void LaunchExecution(Session session) throws Exception {
        sshManager.OpenSession("192.168.101.113", 22, "root");
        switch (session.getInfo().getMethod().getMethod()) {
            case CPLEX:
                runCplex(session);
                break;
            default:
                System.out.println("UNKNOWN METHOD");
                break;
        }
        sshManager.CloseSession();
    }

    private static void runCplex(Session session) throws Exception {
        if (session.getInfo().getFiles().size() > 1){
            sshManager.ExecuteCommand(String.format(cplexDatMod, session.getId(), session.getInfo().getFiles().get(0).getName(), session.getInfo().getFiles().get(1).getName()));
        } else {
            sshManager.ExecuteCommand(String.format(cplexMpsLp, session.getId(), session.getInfo().getFiles().get(0).getName()));
        }
    }
}
