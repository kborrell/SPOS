package com.udl.tfg.sposapp.models;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long Id;

    @NotBlank(message = "You must provide a valid email address")
    @Email
    private String email;

    @NotNull(message = "You must choose a solution type")
    @Enumerated(EnumType.STRING)
    private SolutionType type;

    @NotNull(message = "A maximum duration is required. -1 for optimal execution.")
    private int maximumDuration;

    @ManyToOne
    private VirtualMachine vmConfig;

    @ManyToOne
    private Parameters info;

    @OneToOne
    private Result sessionResults;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public SolutionType getType() {
        return type;
    }

    public void setType(SolutionType type) {
        this.type = type;
    }

    public VirtualMachine getVmConfig() {
        return vmConfig;
    }

    public void setVmConfig(VirtualMachine vmConfig) {
        this.vmConfig = vmConfig;
    }

    public Parameters getInfo() {
        return info;
    }

    public void setInfo(Parameters info) {
        this.info = info;
    }

    public Result getSessionResults() {
        return sessionResults;
    }

    public void setSessionResults(Result sessionResults) {
        this.sessionResults = sessionResults;
    }

    public int getMaximumDuration() {
        return maximumDuration;
    }

    public void setMaximumDuration(int maximumDuration) {
        this.maximumDuration = maximumDuration;
    }
}
