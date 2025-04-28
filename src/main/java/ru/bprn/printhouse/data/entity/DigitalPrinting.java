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

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private Long id;

// --------  Размер изделия, поля и расположение на печатном листе -----------------
    private StandartSize standartSize;

    @Positive
    private Double productSizeX = 1.0;

    @Positive
    private Double productSizeY = 1.0;

    private Gap bleed;

    @NotBlank
    private String orientation = "Автоматически";


    // Размер изделия с полями
    @Positive
    private double fullProductSizeX = 1.0;

    @Positive
    private double fullProductSizeY = 1.0;


// -------  Принтер, цветность печати и отступы ------
    private PrintMashine printMashine;

    private QuantityColors quantityColorsCover;

    private QuantityColors quantityColorsBack;

    @PositiveOrZero
    private Integer quantityOfExtraLeaves = 0;

    private Gap margins;

    @Positive
    private Double printAreaSizeX = 1.0;

    @Positive
    private Double printAreaSizeY = 1.0;


// ------  Материалы и формула расчета -------
    private Formulas formula;

    private Set<Material> materials;

    private Material defaultMaterial;

    private Formulas materialFormula;

    // Размер печатного листа
    @Positive
    private Double printSheetSizeX = 1.0;

    @Positive
    private Double printSheetSizeY = 1.0;


// -----  Вспомогательные элементы для расчета ------
    //private Set<VariablesForMainWorks> variables;

    @PositiveOrZero
    private Integer quantityOfProduct = 0;

    @Positive
    private int rowsOnSheet = 1;

    @Positive
    private int columnsOnSheet = 1;

    @Positive
    private int quantityProductionsOnSheet = 1;

    @PositiveOrZero
    private Integer quantityOfPrintSheets = 0;


    private void calc() {
        calcPrintAreaSize();
        calcProductionsOnSheet();

    }

    private void calcFullProductSize(){
        if (bleed!=null) {
            fullProductSizeX = productSizeX + bleed.getGapLeft() + bleed.getGapRight();
            fullProductSizeY = productSizeY + bleed.getGapBottom() + bleed.getGapTop();
        } else {
            fullProductSizeX = productSizeX;
            fullProductSizeY = productSizeY;
        }
    }

    private void calcPrintAreaSize(){
        if (defaultMaterial!=null) {
            printAreaSizeX = printSheetSizeX - margins.getGapRight() - margins.getGapLeft();
            printAreaSizeY = printSheetSizeY - margins.getGapTop() - margins.getGapBottom();
        }
    }

    private void calcProductionsOnSheet(){
        int[] mass = {1,1,1};

        var mass1 = getQuantity(printAreaSizeX, productSizeY, fullProductSizeX, fullProductSizeY);
        var mass2 = getQuantity(printAreaSizeX, productSizeY, fullProductSizeY, fullProductSizeX);

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

        if (quantityOfProduct != 0) {
            quantityOfPrintSheets = quantityOfProduct / mass[2];
            if (quantityOfProduct % mass[2] != 0) quantityOfPrintSheets++;
            quantityOfPrintSheets += quantityOfExtraLeaves;
        } else quantityOfPrintSheets = 0;

        rowsOnSheet = mass[0];
        columnsOnSheet = mass[1];
        quantityProductionsOnSheet = mass[2];

    }

    private int[] getQuantity(double sizeLeafX, double sizeLeafY, Double sizeElementX, Double sizeElementY) {
        int[] mass = new int[3];
        mass[0] = (int) (sizeLeafX/sizeElementX);
        mass[1] = (int) (sizeLeafY/sizeElementY);
        mass[2] = mass[1]*mass[0];
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
        this.printSheetSizeX = (double) defaultMaterial.getSizeOfPrintLeaf().getLength();
        this.printSheetSizeY = (double) defaultMaterial.getSizeOfPrintLeaf().getWidth();
        calc();
    }

    public void setProductSizeX(@Positive Double productSizeX) {
        this.productSizeX = productSizeX;
        calcFullProductSize();
    }

    public void setProductSizeY(@Positive Double productSizeY) {
        this.productSizeY = productSizeY;
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

    public String toString() {
        return this.getClass().getName();
    }
}
