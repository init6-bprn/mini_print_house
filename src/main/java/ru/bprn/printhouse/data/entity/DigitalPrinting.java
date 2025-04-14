package ru.bprn.printhouse.data.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ru.bprn.printhouse.views.template.HasFormula;
import ru.bprn.printhouse.views.template.IsEquipment;
import ru.bprn.printhouse.views.template.HasMaterial;

import java.util.Set;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id",
        scope = DigitalPrinting.class)
public class DigitalPrinting implements IsEquipment, HasMaterial, HasFormula {

    @Id
    @EqualsAndHashCode.Include
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private PrintMashine printMashine;

    private QuantityColors quantityColorsCover;

    private QuantityColors quantityColorsBack;

    private ImposeCase imposeCase;

    private int quantityOfExtraLeaves = 0;

    private Formulas formula;

    private Set<Material> materials;

    private Material defaultMaterial;

    private Gap margins;

    private String description;

    private Formulas materialFormula;

    @JsonIgnoreProperties
    private int fullSizeX = 10000;

    @JsonIgnoreProperties
    private int fullSizeY = 10000;

    @NotBlank
    private String orientation = "Автоматически";

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String str) {
        this.description = str;

    }

    @Override
    public Set<Material> getSelectedMaterials() {
        return materials;
    }

    @Override
    public Formulas getMaterialFormula() {
        return materialFormula;
    }

    @Override
    public Formulas getFormula() {
        return formula;
    }

    @Override
    public int getFullSizeX() {
        var leaf = defaultMaterial.getSizeOfPrintLeaf();
        var gap = printMashine.getGap();
        return leaf.getWidth()-gap.getGapLeft()-gap.getGapRight();
    }

    @Override
    public int getFullSizeY() {
        var leaf = defaultMaterial.getSizeOfPrintLeaf();
        var gap = printMashine.getGap();
        return leaf.getLength()-gap.getGapTop()-gap.getGapBottom();
    }
}
