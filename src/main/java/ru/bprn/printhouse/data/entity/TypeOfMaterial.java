package ru.bprn.printhouse.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.bprn.printhouse.data.AbstractEntity;

@Data
@SequenceGenerator(name = "idgenerator", sequenceName = "TypeOfMaterial_SEQ", allocationSize = 1)
@Entity
@Table(name = "type_of_material")
public class TypeOfMaterial extends AbstractEntity {
    @NotNull
    @NotEmpty
    private String name = "";

    public TypeOfMaterial() {
    }

    public TypeOfMaterial(String name) {
        this.name = name;
    }


}