package com.udl.tfg.sposapp.models;

import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.validation.constraints.NotNull;


@Entity
public class Result {

    @Id
    @GeneratedValue
    private long Id;

    @Lob
    @NotNull
    private byte[] fullResults;

    @Lob
    @NotNull
    private byte[] shortResults;

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public byte[] getShortResults() {
        return shortResults;
    }

    public void setShortResults(byte[] shortResults) {
        this.shortResults = shortResults;
    }

    public byte[] getFullResults() {
        return fullResults;
    }

    public void setFullResults(byte[] fullResults) {
        this.fullResults = fullResults;
    }
}
