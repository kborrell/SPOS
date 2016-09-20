package com.udl.tfg.sposapp;


import com.jcraft.jsch.JSchException;
import com.udl.tfg.sposapp.models.*;
import com.udl.tfg.sposapp.repositories.MethodInfoRepository;
import com.udl.tfg.sposapp.repositories.ModelInfoRepository;
import com.udl.tfg.sposapp.repositories.SessionRepository;
import com.udl.tfg.sposapp.repositories.VirtualMachineRepository;
import com.udl.tfg.sposapp.utils.OCAManager;
import com.udl.tfg.sposapp.utils.SSHManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;

@Component
public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private VirtualMachineRepository virtualMachineRepository;
    @Autowired
    private ModelInfoRepository modelInfoRepository;
    @Autowired
    private MethodInfoRepository methodInfoRepository;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private SSHManager sshManager;
    @Autowired
    private OCAManager ocaManager;
    @Value("${localStorageFolder}") private String localStorageFolder;

    Map<MethodCodes, MethodInfo> methods = new HashMap<>();

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        PopulateDB();

        try {
            sshManager.Initialize();
        } catch (JSchException e) {
            e.printStackTrace();
        }

        ocaManager.Initialize();

        new Thread() {

            boolean sessionsHasResults(long id) {
                try {
                    File f = new File(localStorageFolder + "/" + String.valueOf(id) + "/results.txt");
                    byte[] encoded = new byte[0];
                    encoded = Files.readAllBytes(f.toPath());
                    String content = new String(encoded, Charset.defaultCharset());
                    if (!content.isEmpty()) {
                        return true;
                    }
                    return false;
                } catch (Exception e) {
                    return false;
                }
            }

            public void run() {
                while(true) {
                    for (Session session : sessionRepository.findAll()) {
                        if (!session.isVmDestroyed() && sessionsHasResults(session.getId()) && session.getVmConfig().getId() > 3) {
                            try {
                                ocaManager.deleteVM(session.getVmConfig().getApiID());
                                session.setVmDestroyed(true);
                                sessionRepository.save(session);
                            } catch (Exception ignored) {}
                        }
                    }
                    try {
                        Thread.sleep(5*60*1000); //Run every 5 minutes
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }.start();
    }

    private void PopulateDB() {
        CreateVmConfig(1, 1024, 0.5f);
        CreateVmConfig(4, 1024, 1f);
        CreateVmConfig(8, 4096, 2f);

        CreateMethods(MethodCodes.CPLEX, "General Solver", true, true, true, false, false);
        CreateMethods(MethodCodes.BD, "Benders Decompositions", false, false, true, false, false);
        CreateMethods(MethodCodes.pBD, "Parallel Benders Decomposition", false, false, true, true, false);
        CreateMethods(MethodCodes.CBD, "Cluster Benders Decomposition", false, false, true, false, true);
        CreateMethods(MethodCodes.pCBD, "Parallel Cluster Benders Decomposition", false, false, true, true, true);
        CreateMethods(MethodCodes.pBDc, "Parallel Benders Decomposition with Clusters", false, false, true, true, false);
        CreateMethods(MethodCodes.LD, "Lagrange Decomposition", false, false, true, false, false);
        CreateMethods(MethodCodes.pLD, "Parallel Lagrange Decomposition", false, false, true, true, false);
        CreateMethods(MethodCodes.Gurobi, "Gurobi Solver", true, true, false, false, false);
        CreateMethods(MethodCodes.Lpsolve, "Lpsolve Solver", true, true, false, false, false);

        CreateModels(ModelCodes.GDS2SP, "General Discrete S2SP", new ArrayList<>(
                Arrays.asList(
                        methods.get(MethodCodes.CPLEX),
                        methods.get(MethodCodes.BD),
                        methods.get(MethodCodes.pBD),
                        methods.get(MethodCodes.CBD),
                        methods.get(MethodCodes.pCBD)
                )
        ));
        CreateModels(ModelCodes.SFLP, "Stochastic Facility Location Problem", new ArrayList<>(
                Arrays.asList(
                        methods.get(MethodCodes.CPLEX),
                        methods.get(MethodCodes.pBDc),
                        methods.get(MethodCodes.LD),
                        methods.get(MethodCodes.pLD)
                )
        ));
        CreateModels(ModelCodes.GM01S2SP, "General Mixed 0-1 S2SP", new ArrayList<>(
                Arrays.asList(
                        methods.get(MethodCodes.CPLEX),
                        methods.get(MethodCodes.LD),
                        methods.get(MethodCodes.pLD)
                )
        ));
        CreateModels(ModelCodes.Determinist, "Determinist model", new ArrayList<>(
                Arrays.asList(
                        methods.get(MethodCodes.CPLEX),
                        methods.get(MethodCodes.Gurobi),
                        methods.get(MethodCodes.Lpsolve)
                )
        ));
    }

    private void CreateVmConfig(int virtualCPUs, int ram, float realCPUs) {
        VirtualMachine vm = new VirtualMachine();
        vm.setVirtualCPUs(virtualCPUs);
        vm.setRam(ram);
        vm.setrealCPUs(realCPUs);
        virtualMachineRepository.save(vm);
    }

    private void CreateModels(ModelCodes type, String name, List<MethodInfo> compatibleMethods) {
        ModelInfo mi = new ModelInfo();
        mi.setModel(type);
        mi.setName(name);
        mi.setCompatibleMethods(compatibleMethods);
        modelInfoRepository.save(mi);
    }

    private void CreateMethods(MethodCodes method, String name, boolean mpsSupport, boolean lpSupport,
                               boolean datSupport, boolean parallelizationSupport, boolean clusterSupport) {
        MethodInfo mi = new MethodInfo();
        mi.setMethod(method);
        mi.setName(name);
        mi.setMpsSupport(mpsSupport);
        mi.setLpSupport(lpSupport);
        mi.setDatSupport(datSupport);
        mi.setParallelizationSupport(parallelizationSupport);
        mi.setClusterSupport(clusterSupport);
        methodInfoRepository.save(mi);
        methods.put(method, mi);
    }
}
