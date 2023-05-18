package ru.bprn.printhouse.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import ru.bprn.printhouse.data.AbstractEntity;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}