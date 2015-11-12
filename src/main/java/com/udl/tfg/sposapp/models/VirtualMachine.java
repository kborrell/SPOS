package com.udl.tfg.sposapp.models;

import org.hibernate.validator.constraints.NotBlank;

import javax.annotation.Nullable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.DecimalMin;

@Entity
public class VirtualMachine {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long Id;

    @NotBlank(message = "You must introduce the number of emulated CPU's")
    private int cpuCount;

    @NotBlank(message = "You must introduce the amount of memory that will have the VM created")
    @DecimalMin(message = "Amount of memory has to be greater than zero", value = "0")
    private int ram;

    @NotBlank(message = "You must enter the real percentage of real cpu's used by the VM")
    @DecimalMin(message = "This value must be greater than zero", value = "0")
    private float realPercentage;

    @Nullable
    private String IP;

    public int getCpuCount() {
        return cpuCount;
    }

    public void setCpuCount(int cpuCount) {
        this.cpuCount = cpuCount;
    }

    public int getRam() {
        return ram;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    public float getRealPercentage() {
        return realPercentage;
    }

    public void setRealPercentage(float realPercentage) {
        this.realPercentage = realPercentage;
    }

    @Nullable
    public String getIP() {
        return IP;
    }

    public void setIP(@Nullable String IP) {
        this.IP = IP;
    }
}
