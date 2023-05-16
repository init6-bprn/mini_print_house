package ru.bprn.printhouse.data.entity;

import lombok.Data;
import ru.bprn.printhouse.data.AbstractEntity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Data
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

    @Override
    public String toString() {
        return getName();
    }

}
