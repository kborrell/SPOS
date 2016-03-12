package com.udl.tfg.sposapp.models;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.rest.core.annotation.RestResource;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;


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

    @ElementCollection
    public List<DataFile> files;

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

    public List<DataFile> getFiles() {
        return files;
    }

    public void setFiles(List<DataFile> files) {
        this.files = files;
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
