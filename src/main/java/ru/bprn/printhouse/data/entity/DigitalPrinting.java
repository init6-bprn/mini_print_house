package ru.bprn.printhouse.data.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ru.bprn.printhouse.views.template.HasFormula;
import ru.bprn.printhouse.views.template.HasMaterial;
import ru.bprn.printhouse.views.template.IsMainPrintWork;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode

@JsonIdentityInfo(
        generator = ObjectIdGenerators.IntSequenceGenerator.class,
        property = "id",
        scope = DigitalPrinting.class)
public class DigitalPrinting implements IsMainPrintWork, HasMaterial, HasFormula {
    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private PrintMashine printMashine;

    private QuantityColors quantityColorsCover;

    private QuantityColors quantityColorsBack;

    private ImposeCase imposeCase;

    private Integer quantityOfExtraLeaves = 0;

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

    public @NotBlank String getOrientation() {
        return orientation;
    }

    public void setOrientation(@NotBlank String orientation) {
        this.orientation = orientation;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String str) {
        this.description = str;

    }

    @Override
    @JsonIgnore
    public Material getMaterial() {return defaultMaterial; }

    @Override
    @JsonIgnore
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
    @JsonIgnore
    public Integer getLeafSizeX() {
        return defaultMaterial.getSizeOfPrintLeaf().getLength();
    }

    @Override
    @JsonIgnore
    public Integer getLeafSizeY() {
        return defaultMaterial.getSizeOfPrintLeaf().getWidth();
    }

    @JsonIgnore
    @Override
    public Integer getPrintAreaX() {
        return defaultMaterial.getSizeOfPrintLeaf().getWidth()-printMashine.getGap().getGapRight()-printMashine.getGap().getGapLeft();
    }

    @JsonIgnore
    @Override
    public Integer getPrintAreaY() {
        return defaultMaterial.getSizeOfPrintLeaf().getLength()-printMashine.getGap().getGapTop()-printMashine.getGap().getGapBottom();
    }
}
