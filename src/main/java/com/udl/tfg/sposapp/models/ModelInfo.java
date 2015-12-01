package com.udl.tfg.sposapp.models;

import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import javax.swing.text.StringContent;
import javax.validation.constraints.NotNull;

@Entity
public class ModelInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long Id;

    @NotNull(message = "You must specify model type")
    @Enumerated(EnumType.STRING)
    private ModelType model;

    private String name;

    @NotNull(message = "You must specify compatible methods")
    @Enumerated(EnumType.STRING)
    private MethodType[] compatibleMethods;

    public ModelType getModel() {
        return model;
    }

    public void setModel(ModelType model) {
        this.model = model;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MethodType[] getCompatibleMethods() {
        return compatibleMethods;
    }

    public void setCompatibleMethods(MethodType[] compatibleMethods) {
        this.compatibleMethods = compatibleMethods;
    }
}
