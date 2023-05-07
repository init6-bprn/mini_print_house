package ru.bprn.printhouse.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import ru.bprn.printhouse.data.AbstractEntity;

@Data
@SequenceGenerator(name = "idgenerator", sequenceName = "PrintSpeedMaterialDensity_SEQ", allocationSize = 1)
@Entity
@Table(name = "print_speed_material_density")
public class PrintSpeedMaterialDensity extends AbstractEntity {

    private byte density_65_79;
    private byte density_80_90;
    private byte density_91_105;
    private byte density_106_129;
    private byte density_130_170;
    private byte density_171_220;
    private byte density_221_254;
    private byte density_300;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "type_of_material", nullable = false )
    private TypeOfMaterial typeOfMaterial;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "print_mashine", nullable = false )
    private PrintMashine printMashine;

    public PrintSpeedMaterialDensity() {
        this.density_65_79 = 0;
        this.density_80_90 = 0;
        this.density_91_105 = 0;
        this.density_106_129 = 0;
        this.density_130_170 = 0;
        this.density_171_220 = 0;
        this.density_221_254 = 0;
        this.density_300 = 0;
    }

}