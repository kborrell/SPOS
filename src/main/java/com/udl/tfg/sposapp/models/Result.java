package com.udl.tfg.sposapp.models;

import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;


@Entity
public class Result {

    @Id
    @GeneratedValue
    private long Id;

    @NotNull
    private String results;

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public String getResults() {
        return results;
    }

    public void setResults(String results) {
        this.results = results;
    }
}
