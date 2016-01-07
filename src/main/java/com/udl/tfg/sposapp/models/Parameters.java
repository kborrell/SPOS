package com.udl.tfg.sposapp.models;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import javax.validation.constraints.NotNull;


@Entity
public class Parameters {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long Id;

    @NotNull(message = "You must specify a method")
    @RestResource(exported = false)
    @ManyToOne
    private MethodInfo method;

    @NotNull(message = "You must specify a model")
    @RestResource(exported = false)
    @ManyToOne
    private ModelInfo model;

    @NotBlank(message = "You must upload at least one info file")
    private String infoFile;

    private boolean isParallel;

    private int groupSize;

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public MethodInfo getMethod() {
        return method;
    }

    public void setMethod(MethodInfo method) {
        this.method = method;
    }

    public ModelInfo getModel() {
        return model;
    }

    public void setModel(ModelInfo model) {
        this.model = model;
    }

    public String getInfoFile() {
        return infoFile;
    }

    public void setInfoFile(String infoFile) {
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
