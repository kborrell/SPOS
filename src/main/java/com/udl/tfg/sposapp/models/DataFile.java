package com.udl.tfg.sposapp.models;

import javax.annotation.Nullable;
import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;

@Entity
public class DataFile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long Id;

    @NotNull
    private String extension;

    @NotNull
    private String name;

    @Lob
    @NotNull
    private byte[] content;

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
