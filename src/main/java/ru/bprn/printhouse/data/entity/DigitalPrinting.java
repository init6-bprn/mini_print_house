package ru.bprn.printhouse.data.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.bprn.printhouse.views.template.HasFormula;
import ru.bprn.printhouse.views.template.IsEquipment;
import ru.bprn.printhouse.views.template.HasMaterial;

import java.util.Collections;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DigitalPrinting implements IsEquipment, HasMaterial, HasFormula {

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
        return Collections.unmodifiableSet(materials);
    }

    @Override
    public Formulas getMaterialFormula() {
        return materialFormula;
    }

    @Override
    public Formulas getFormulas() {
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
