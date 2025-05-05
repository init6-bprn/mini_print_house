package ru.bprn.printhouse.data.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import ru.bprn.printhouse.views.template.HasFormula;
import ru.bprn.printhouse.views.template.HasMaterial;
import ru.bprn.printhouse.views.template.WorkChain;

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
        scope = DigitalPrinting.class)
public class DigitalPrinting implements WorkChain, HasMaterial, HasFormula {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private Long id;

    private String name;

// --------  Размер изделия, поля и расположение на печатном листе -----------------
    private StandartSize standartSize;

    @Positive
    private Double productSizeX = 1.0;

    @Positive
    private Double productSizeY = 1.0;

    private Gap bleed;

    @NotBlank
    private String orientation = "Автоматически";


// -------  Принтер, цветность печати и отступы ------
    private PrintMashine printMashine;

    private QuantityColors quantityColorsCover;

    private QuantityColors quantityColorsBack;

    @PositiveOrZero
    private Integer quantityOfExtraLeaves = 0;

    private Gap margins;


// ------  Материалы и формула расчета -------
    private Formulas formula;

    private Set<Material> materials;

    private Material defaultMaterial;

    private Formulas materialFormula;


// -----  Вспомогательные элементы для расчета ------
    // ----- Массив переменных для расчета формул работ, материалов.
    @JsonIgnore
    private Map<String, Number> variables;

    @PositiveOrZero
    private Integer quantityOfProduct = 0;


    public void calc() {
        calcPrintAreaSize();
        calcProductionsOnSheet();
    }

    private void calcFullProductSize(){
        double x = 0;
        double y = 0;
        if (bleed!=null) {
            x += bleed.getGapLeft() + bleed.getGapRight();
            y += bleed.getGapBottom() + bleed.getGapTop();
        }
        variables.put("fullProductSizeX", variables.get("productSizeX").doubleValue() + x);
        variables.put("fullProductSizeY", variables.get("productSizeY").doubleValue() + y);
    }

    private void calcPrintAreaSize(){
        if (defaultMaterial!=null) {
            variables.put("printAreaSizeX", variables.get("printSheetSizeX").doubleValue() - margins.getGapRight() - margins.getGapLeft());
            variables.put("printAreaSizeY", variables.get("printSheetSizeY").doubleValue() - margins.getGapTop() - margins.getGapBottom());
        }
    }

    private void calcProductionsOnSheet(){
        int[] mass = {1,1,1};

        var mass1 = getQuantity(variables.get("printAreaSizeX").doubleValue(), variables.get("printAreaSizeY").doubleValue(), variables.get("fullProductSizeX").doubleValue(), variables.get("fullProductSizeY").doubleValue());
        var mass2 = getQuantity(variables.get("printAreaSizeX").doubleValue(), variables.get("printAreaSizeY").doubleValue(), variables.get("fullProductSizeY").doubleValue(), variables.get("fullProductSizeX").doubleValue());

        switch (this.orientation) {
            case "Автоматически":
                if (mass1[2] >= mass2[2]) mass = mass1;
                else mass = mass2;
                break;
            case "Вертикальная":
                mass = mass1;
                break;
            case "Горизонтальная":
                mass = mass2;
                break;
        }

        int quantity = variables.get("quantityOfProduct").intValue();
        if (quantity != 0) {
            var q = quantity / mass[2];
            if ((quantity % mass[2]) != 0) q++;
            q += quantityOfExtraLeaves;
            variables.put("quantityOfPrintSheets", q);
        } else variables.put("quantityOfPrintSheets", 0);

        variables.put("rowsOnSheet", mass[0]);
        variables.put("columnsOnSheet", mass[1]);
        variables.put("quantityProductionsOnSheet", mass[3]);

    }

    private int[] getQuantity(double sizeLeafX, double sizeLeafY, double sizeElementX, double sizeElementY) {
        int[] mass = new int[3];
        mass[0] = (int) (sizeLeafX/sizeElementX);
        mass[1] = (int) (sizeLeafY/sizeElementY);
        mass[2] = mass[1] * mass[0];
        return mass;
    }

    public void setMargins(Gap margins) {
        this.margins = margins;
        calc();
    }

    public @NotBlank String getOrientation() {
        return orientation;
    }

    public void setOrientation(@NotBlank String orientation) {
        this.orientation = orientation;
        calc();
    }

    public void setDefaultMaterial(Material defaultMaterial) {
        this.defaultMaterial = defaultMaterial;
        variables.put("printSheetSizeX", (long) defaultMaterial.getSizeOfPrintLeaf().getLength());
        variables.put("printSheetSizeY", (long) defaultMaterial.getSizeOfPrintLeaf().getWidth());
        calc();
    }

    public void setProductSizeX(@Positive Double productSizeX) {
        this.productSizeX = productSizeX;
        variables.put("productSizeX", productSizeX.longValue());
        calcFullProductSize();
    }

    public void setProductSizeY(@Positive Double productSizeY) {
        this.productSizeY = productSizeY;
        variables.put("productSizeY", productSizeY.longValue());
        calcFullProductSize();
    }

    public void setBleed(Gap bleed) {
        this.bleed = bleed;
        calcFullProductSize();
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

    public String toString() {
        return this.getClass().getName();
    }

    @Override
    public String getString() {
        return this.name;
    }

    @Override
    public void calcVariables() {
        calc();
    }

}
