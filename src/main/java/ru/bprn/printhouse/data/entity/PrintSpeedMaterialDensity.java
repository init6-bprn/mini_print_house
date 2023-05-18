package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import ru.bprn.printhouse.data.AbstractEntity;

@Data
@Entity
//@Table(name = "print_speed_material_density")
public class PrintSpeedMaterialDensity extends AbstractEntity {

    @NotNull
    @PositiveOrZero
    @Max(300)
    private Integer densityNoMore;

    @NotNull
    @PositiveOrZero
    @Max(300)
    private Integer speed;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "type_of_material", nullable = false )
    private TypeOfMaterial typeOfMaterial;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "print_mashine", nullable = false )
    private PrintMashine printMashine;

    public PrintSpeedMaterialDensity() {
        this.densityNoMore = 0;
        this.speed = 0;
    }

}