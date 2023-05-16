package ru.bprn.printhouse.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.bprn.printhouse.data.AbstractEntity;

@Data
@Entity
@Table(name = "type_of_material")
public class TypeOfMaterial extends AbstractEntity {
    @NotNull
    @NotEmpty
    private String name = "Бумага";

    public TypeOfMaterial() {
    }

    public TypeOfMaterial(String name) {
        this.name = name;
    }

    @Override
    public String toString(){
        return this.name;
    }

}