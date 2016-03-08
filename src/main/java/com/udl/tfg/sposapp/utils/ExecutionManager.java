package com.udl.tfg.sposapp.utils;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.udl.tfg.sposapp.models.MethodInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExecutionManager {

    private static final Logger logger = LoggerFactory.getLogger(SSHManager.class);

    private static String cplexMpsLp = "cplex -c \"read {0}\" \"optimize\" \"display solution variables -\" >> {1}";
    private static String cplexDatMod = "oplrun {0} {1} >> {2}";

    public static void LaunchExecution(Session session, List<String> parameters){

    }
}
