package com.udl.tfg.sposapp.models;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;


@Entity
public class Parameters {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long Id;

    @NotBlank(message = "You must specify a method")
    private MethodType method;

    @NotBlank(message = "You must specify a model")
    private ModelType model;

    @NotBlank(message = "You must upload at least one info file")
    private byte[] infoFile;

    private boolean isParallel;

    private int groupSize;

    public MethodType getMethod() {
        return method;
    }

    public void setMethod(MethodType method) {
        this.method = method;
    }

    public ModelType getModel() {
        return model;
    }

    public void setModel(ModelType model) {
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
