package com.udl.tfg.sposapp;


import com.udl.tfg.sposapp.models.VirtualMachine;
import com.udl.tfg.sposapp.repositories.VirtualMachineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private VirtualMachineRepository repository;

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event){
        CreateLowVMConfig();
        CreateMediumVMConfig();
        CreateHighVMConfig();
    }

    private void CreateLowVMConfig() {
        VirtualMachine vm = new VirtualMachine();
        vm.setCpuCount(1);
        vm.setRam(64);
        vm.setRealPercentage(10);
        repository.save(vm);
    }

    private void CreateMediumVMConfig() {
        VirtualMachine vm = new VirtualMachine();
        vm.setCpuCount(10);
        vm.setRam(512);
        vm.setRealPercentage(30);
        repository.save(vm);
    }

    private void CreateHighVMConfig() {
        VirtualMachine vm = new VirtualMachine();
        vm.setCpuCount(20);
        vm.setRam(1024);
        vm.setRealPercentage(60);
        repository.save(vm);
    }
}
