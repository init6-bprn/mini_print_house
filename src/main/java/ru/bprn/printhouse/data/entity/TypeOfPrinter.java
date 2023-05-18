package ru.bprn.printhouse.data.entity;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import ru.bprn.printhouse.data.AbstractEntity;

@Entity
public class TypeOfPrinter extends AbstractEntity {

    @NotNull
    @NotEmpty
    private String name = "";

    //@OneToMany(fetch = FetchType.EAGER, mappedBy = "typeOfPrinter")
    //private List<PrintMashine> printMashine;

    public TypeOfPrinter() {
    }

    public TypeOfPrinter (String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return  this.name;
    }
}
