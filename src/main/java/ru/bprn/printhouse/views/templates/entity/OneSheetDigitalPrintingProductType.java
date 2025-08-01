package ru.bprn.printhouse.views.templates.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "one_sheet_digital_printing_product_type")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class OneSheetDigitalPrintingProductType extends AbstractProductType implements HasMateria {

    private Map<String, Number> variables;// = new HashMap<>();

    // --------  Размер изделия, поля и расположение на печатном листе -----------------
    @Positive
    private Double productSizeX = 1d;

    @Positive
    private Double productSizeY = 1d;

    private Double bleed = 0d;

    private String materialFormula;

    private UUID id;

    private Set<UUID> materialsIDSet;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        OneSheetDigitalPrintingProductType that = (OneSheetDigitalPrintingProductType) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @Override
    public UUID getDefaultMaterial() {
        return this.id;
    }

    @Override
    public void setDefaultMaterial(UUID id) {
        this.id = id;
    }

    @Override
    public Set<UUID> getSelectedMaterials() {
        return this.materialsIDSet;
    }

    @Override
    public void setSelectedMaterials(Set<UUID> materialSet) {
        this.materialsIDSet = materialSet;
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
