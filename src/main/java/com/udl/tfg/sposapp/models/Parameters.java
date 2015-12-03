package com.udl.tfg.sposapp.models;

import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.validation.constraints.NotNull;


@Entity
public class Parameters {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long Id;

    @NotNull(message = "You must specify a method")
    @Enumerated(EnumType.STRING)
    private MethodCodes method;

    @NotNull(message = "You must specify a model")
    @Enumerated(EnumType.STRING)
    private ModelCodes model;

    @NotBlank(message = "You must upload at least one info file")
    private byte[] infoFile;

    private boolean isParallel;

    private int groupSize;

    public MethodCodes getMethod() {
        return method;
    }

    public void setMethod(MethodCodes method) {
        this.method = method;
    }

    public ModelCodes getModel() {
        return model;
    }

    public void setModel(ModelCodes model) {
        this.model = model;
    }

    public byte[] getInfoFile() {
        return infoFile;
    }

    public void setInfoFile(byte[] infoFile) {
        this.infoFile = infoFile;
    }

    public boolean isParallel() {
        return isParallel;
    }

    public void setIsParallel(boolean isParallel) {
        this.isParallel = isParallel;
    }

    public int getGroupSize() {
        return groupSize;
    }

    public void setGroupSize(int groupSize) {
        this.groupSize = groupSize;
    }
}
