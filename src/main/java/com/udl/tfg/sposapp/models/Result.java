package com.udl.tfg.sposapp.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;


@Entity
public class Result {

    @Id
    @GeneratedValue
    private long Id;
}
