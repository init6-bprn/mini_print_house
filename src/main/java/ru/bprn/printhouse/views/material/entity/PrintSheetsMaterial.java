package ru.bprn.printhouse.views.material.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.bprn.printhouse.data.entity.TypeOfMaterial;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class PrintSheetsMaterial extends AbstractMaterials{

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_of_material", nullable = false )
    private TypeOfMaterial typeOfMaterial;

    @PositiveOrZero
    private Integer sizeX = 0;

    @PositiveOrZero
    private Integer sizeY = 0;

   @PositiveOrZero
    private Integer thickness;

}
