package com.udl.tfg.sposapp.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

@Entity
public class VirtualMachine {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long Id;

    @NotNull(message = "You must introduce the number of emulated CPU's")
    private int virtualCPUs;

    @NotNull(message = "You must introduce the amount of memory that will have the VM created")
    @DecimalMin(message = "Amount of memory has to be greater than zero", value = "0")
    private int ram;

    @NotNull(message = "You must enter the real percentage of real cpu's used by the VM")
    @DecimalMin(message = "This value must be greater than zero", value = "0")
    private float realCPUs;

    private int apiID;

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public int getVirtualCPUs() {
        return virtualCPUs;
    }

    public void setVirtualCPUs(int virtualCPUs) {
        this.virtualCPUs = virtualCPUs;
    }

    public int getRam() {
        return ram;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    public float getrealCPUs() {
        return realCPUs;
    }

    public void setrealCPUs(float realCPUs) {
        this.realCPUs = realCPUs;
    }

    public int getApiID() {
        return apiID;
    }

    public void setApiID(int apiID) {
        this.apiID = apiID;
    }
}
