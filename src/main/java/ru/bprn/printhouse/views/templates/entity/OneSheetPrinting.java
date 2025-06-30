package ru.bprn.printhouse.views.templates.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;
import ru.bprn.printhouse.data.entity.Formulas;
import ru.bprn.printhouse.data.entity.Gap;
import ru.bprn.printhouse.views.material.entity.Material;
import ru.bprn.printhouse.data.entity.StandartSize;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode

@JsonIdentityInfo(
        generator = ObjectIdGenerators.IntSequenceGenerator.class,
        property = "id",
        scope = OneSheetPrinting.class)
public class OneSheetPrinting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private Long id;

    private Map<String, Number> variables;// = new HashMap<>();

    // --------  Размер изделия, поля и расположение на печатном листе -----------------
    private StandartSize standartSize;

    @Positive
    private Double productSizeX = 1d;

    @Positive
    private Double productSizeY = 1d;

    private Gap bleed;

    @NotBlank
    private String orientation = "Автоматически";


    // ------  Материалы и формула расчета -------
    private Set<Material> materials;

    private Material defaultMaterial;

    private Formulas materialFormula;

    @Override
    public String toString() {
        return "Однолистовая печать, размер: "+productSizeX+"X"+productSizeY
                +", материал: "+defaultMaterial.getName()+", плотность: "+defaultMaterial.getThickness().toString();
    }

}
