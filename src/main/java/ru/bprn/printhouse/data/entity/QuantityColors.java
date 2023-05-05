package ru.bprn.printhouse.data.entity;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.bprn.printhouse.data.AbstractEntity;

@Data
@Entity
public class QuantityColors extends AbstractEntity {

    @NotNull
    @NotEmpty
    private String name = "";

    // @OneToMany (fetch = FetchType.EAGER, mappedBy = "quantityColors")
    //private List<PrintMashine> printMashine;

    public QuantityColors() {
    }

    public  QuantityColors(String name) {
        this.name = name;
    }

    public String toString(){
        return getName();
    }

}
