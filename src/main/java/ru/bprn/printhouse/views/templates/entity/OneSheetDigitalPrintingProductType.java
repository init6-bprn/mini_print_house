package ru.bprn.printhouse.views.templates.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;

import java.util.Set;

@Entity
@Table(name = "one_sheet_digital_printing_product_type")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OneSheetDigitalPrintingProductType extends AbstractProductType implements HasMateria {

    // --------  Размер изделия, поля и расположение на печатном листе -----------------
    @Positive
    private Double productSizeX = 1d;

    @Positive
    private Double productSizeY = 1d;

    private Double bleed = 0d;

    private String materialFormula;

    @ManyToOne
    private AbstractMaterials defaultMaterial;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "one_sheet_digital_printing_product_type___abstract_material",
            joinColumns = @JoinColumn(name = "product_type_id"),
            inverseJoinColumns = @JoinColumn(name = "material_id"))
    private Set<AbstractMaterials> selectedMaterials;

    private boolean multiplay;

    @Override
    public AbstractMaterials getDefaultMaterial() {
        return this.defaultMaterial;
    }

    @Override
    public void setDefaultMaterial(AbstractMaterials material) {
        this.defaultMaterial = material;
    }

    @Override
    public Set<AbstractMaterials> getSelectedMaterials() {
        return this.selectedMaterials;
    }

    @Override
    public void setSelectedMaterials(Set<AbstractMaterials> materialSet) {
        this.selectedMaterials = materialSet;
    }

    @Override
    public String getMaterialFormula() {
        return materialFormula;
    }

    @Override
    public void setMaterialFormula(String formula) {
        this.materialFormula = formula;

    }
}
