package com.udl.tfg.sposapp.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.rest.core.annotation.RestResource;

import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.security.SecureRandom;

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

    @ManyToOne(cascade = {CascadeType.ALL})
    @RestResource(exported = false)
    private VirtualMachine vmConfig;

    @ManyToOne(cascade = {CascadeType.ALL})
    @RestResource(exported = false)
    private Parameters info;

    @OneToOne(cascade = {CascadeType.ALL})
    @RestResource(exported = false)
    private Result sessionResults;

    @JsonIgnore
    private String key = "";

    public long getId() {
        return Id;
    }

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

    public String getKey() {
        return key;
    }

    public void generateKey(){
        String AB = "123456789ABCDEFGHIJKLMNOPKRSTUVWYZabcdefghijklmnopkrstuvwyz";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++){
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        this.key = sb.toString();
    }
}
