package ru.bprn.printhouse.data.entity;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.bprn.printhouse.views.template.HasMargins;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DigitalPrinting implements HasMargins {

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

    @NotBlank
    private String orientation = "Автоматически";


    @Override
    public Gap getMargins() {
        return printMashine.getGap();
    }

}
