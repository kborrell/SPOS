package com.udl.tfg.sposapp;


import com.udl.tfg.sposapp.models.*;
import com.udl.tfg.sposapp.repositories.MethodInfoRepository;
import com.udl.tfg.sposapp.repositories.ModelInfoRepository;
import com.udl.tfg.sposapp.repositories.VirtualMachineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private VirtualMachineRepository virtualMachineRepository;
    @Autowired
    private ModelInfoRepository modelInfoRepository;
    @Autowired
    private MethodInfoRepository methodInfoRepository;

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event){
        CreateVmConfig(1, 64, 0.1f);
        CreateVmConfig(10, 512, 0.3f);
        CreateVmConfig(20, 1024, 0.6f);

        CreateModels(ModelCodes.GDS2SP, "General Discrete S2SP", new ArrayList<>(
            Arrays.asList(
                MethodCodes.CPLEX,
                MethodCodes.BD,
                MethodCodes.pBD,
                MethodCodes.CBD,
                MethodCodes.pCBD
            )
        ));
        CreateModels(ModelCodes.SFLP, "Stochastic Facility Location Problem", new ArrayList<>(
            Arrays.asList(
                MethodCodes.CPLEX,
                MethodCodes.pBDc,
                MethodCodes.LD,
                MethodCodes.pLD
            )
        ));
        CreateModels(ModelCodes.GM01S2SP, "General Mixed 0-1 S2SP", new ArrayList<> (
                Arrays.asList(
                MethodCodes.CPLEX,
                MethodCodes.LD,
                MethodCodes.pLD
                )
        ));

        CreateMethods(MethodCodes.CPLEX, "General Solver", true, true, true, false, false);
        CreateMethods(MethodCodes.BD, "Benders Decompositions", false, false, true, false, false);
        CreateMethods(MethodCodes.pBD, "Parallel Benders Decomposition", false, false, true, true, false);
        CreateMethods(MethodCodes.CBD, "Cluster Benders Decomposition", false, false, true, false, true);
        CreateMethods(MethodCodes.pCBD, "Parallel Cluster Benders Decomposition", false, false, true, true, true);
        CreateMethods(MethodCodes.pBDc, "Parallel Benders Decomposition with Clusters", false, false, true, true, false);
        CreateMethods(MethodCodes.LD, "Lagrande Decomposition", false, false, true, false, false);
        CreateMethods(MethodCodes.pLD, "Parallel Lagrande Decomposition", false, false, true, true, false);
    }

    private void CreateVmConfig(int cpuCount, int ram, float realPercentage){
        VirtualMachine vm = new VirtualMachine();
        vm.setCpuCount(cpuCount);
        vm.setRam(ram);
        vm.setRealPercentage(realPercentage);
        virtualMachineRepository.save(vm);
    }

    private void CreateModels(ModelCodes type, String name, List<MethodCodes> compatibleMethods){
        ModelInfo mi = new ModelInfo();
        mi.setModel(type);
        mi.setName(name);
        mi.setCompatibleMethods(compatibleMethods);
        modelInfoRepository.save(mi);
    }

    private void CreateMethods(MethodCodes method, String name, boolean mpsSupport, boolean lpSupport,
                               boolean datSupport, boolean parallelizationSupport, boolean clusterSupport){
        MethodInfo mi = new MethodInfo();
        mi.setMethod(method);
        mi.setName(name);
        mi.setMpsSupport(mpsSupport);
        mi.setLpSupport(lpSupport);
        mi.setDatSupport(datSupport);
        mi.setParallelizationSupport(parallelizationSupport);
        mi.setClusterSupport(clusterSupport);
        methodInfoRepository.save(mi);
    }
}
