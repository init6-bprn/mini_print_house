package ru.bprn.printhouse.data.entity;

import lombok.Data;
import ru.bprn.printhouse.data.AbstractEntity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Entity
public class TypeOfPrinter extends AbstractEntity {

    @NotNull
    @NotEmpty
    private String name = "";

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "typeOfPrinter")
    private List<PrintMashine> printMashine;

    public TypeOfPrinter (String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return getName();
    }

}
