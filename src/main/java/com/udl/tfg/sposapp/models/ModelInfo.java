package com.udl.tfg.sposapp.models;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
public class ModelInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long Id;

    @NotNull(message = "You must specify model type")
    @Enumerated(EnumType.STRING)
    private ModelCodes model;

    private String name;

    @NotNull(message = "You must specify compatible methods")
    @ElementCollection
    private List<MethodCodes> compatibleMethods;

    public ModelCodes getModel() {
        return model;
    }

    public void setModel(ModelCodes model) {
        this.model = model;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MethodCodes> getCompatibleMethods() {
        return compatibleMethods;
    }

    public void setCompatibleMethods(List<MethodCodes> compatibleMethods) {
        this.compatibleMethods = compatibleMethods;
    }
}
