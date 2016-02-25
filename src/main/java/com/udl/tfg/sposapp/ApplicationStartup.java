package com.udl.tfg.sposapp;


import com.jcraft.jsch.JSchException;
import com.udl.tfg.sposapp.models.*;
import com.udl.tfg.sposapp.repositories.MethodInfoRepository;
import com.udl.tfg.sposapp.repositories.ModelInfoRepository;
import com.udl.tfg.sposapp.repositories.VirtualMachineRepository;
import com.udl.tfg.sposapp.utils.OCAManager;
import com.udl.tfg.sposapp.utils.SSHManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

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
    private SSHManager sshManager;
    @Autowired
    private OCAManager ocaManager;

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
    }

    private void PopulateDB() {
        CreateVmConfig(1, 64, 0.1f);
        CreateVmConfig(10, 512, 0.3f);
        CreateVmConfig(20, 1024, 0.6f);

        CreateMethods(MethodCodes.CPLEX, "General Solver", true, true, true, false, false);
        CreateMethods(MethodCodes.BD, "Benders Decompositions", false, false, true, false, false);
        CreateMethods(MethodCodes.pBD, "Parallel Benders Decomposition", false, false, true, true, false);
        CreateMethods(MethodCodes.CBD, "Cluster Benders Decomposition", false, false, true, false, true);
        CreateMethods(MethodCodes.pCBD, "Parallel Cluster Benders Decomposition", false, false, true, true, true);
        CreateMethods(MethodCodes.pBDc, "Parallel Benders Decomposition with Clusters", false, false, true, true, false);
        CreateMethods(MethodCodes.LD, "Lagrange Decomposition", false, false, true, false, false);
        CreateMethods(MethodCodes.pLD, "Parallel Lagrange Decomposition", false, false, true, true, false);
        CreateMethods(MethodCodes.Gurobi, "Gurobi Solver", true, false, false, false, false);
        CreateMethods(MethodCodes.Xpress, "Xpress Solver", true, false, false, false, false);
        CreateMethods(MethodCodes.Lpsolve, "Lpsolve Solver", true, false, false, false, false);

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
    }

    private void CreateVmConfig(int cpuCount, int ram, float realPercentage) {
        VirtualMachine vm = new VirtualMachine();
        vm.setCpuCount(cpuCount);
        vm.setRam(ram);
        vm.setRealPercentage(realPercentage);
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
