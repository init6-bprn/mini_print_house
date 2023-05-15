package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import ru.bprn.printhouse.data.AbstractEntity;

@Data
@Entity
@Table(name = "print_speed_material_density")
public class PrintSpeedMaterialDensity extends AbstractEntity {

    private byte densityNoMore;

    private byte speed;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "type_of_material", nullable = false )
    private TypeOfMaterial typeOfMaterial;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "print_mashine", nullable = false )
    private PrintMashine printMashine;

    public PrintSpeedMaterialDensity() {
        this.densityNoMore = 0;
        this.speed = 0;
    }

}