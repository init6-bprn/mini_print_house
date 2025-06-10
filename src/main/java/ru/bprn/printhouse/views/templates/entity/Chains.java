package ru.bprn.printhouse.views.templates.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Chains extends AbstractTemplate {

    @Column(columnDefinition = "mediumtext")
    private String strJSON = "";

    @Override
    public String toString(){return name;}
}