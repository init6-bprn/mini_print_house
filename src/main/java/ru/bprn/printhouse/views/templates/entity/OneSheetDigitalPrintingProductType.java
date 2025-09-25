package ru.bprn.printhouse.views.templates.entity;

import com.vaadin.flow.component.icon.VaadinIcon;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.bprn.printhouse.annotation.MenuItem;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.entity.PrintSheetsMaterial;

import java.util.Optional;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "one_sheet_digital_printing_product_type")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@MenuItem(name = "Однолистовая Печать", icon = VaadinIcon.PRINT, context = "product", description = "Компонент однолистовой печати")
public class OneSheetDigitalPrintingProductType extends AbstractProductType implements HasMateria{

    @ManyToOne(fetch = FetchType.EAGER)
    private PrintSheetsMaterial defaultMaterial;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH}) // Оставил только одну аннотацию с правильным каскадом
    @JoinTable(
            name = "one_sheet_digital_printing_product_type___abstract_material",
            joinColumns = @JoinColumn(name = "product_type_id"),
            inverseJoinColumns = @JoinColumn(name = "material_id"))
    private Set<PrintSheetsMaterial> selectedMaterials = new HashSet<>();

    @Override
    @Transient
    public AbstractMaterials getDefaultMat() {
        return this.defaultMaterial;
    }

    @Transient
    @Override
    public Set<AbstractMaterials> getSelectedMat() {
        return this.selectedMaterials.stream().map(m -> (AbstractMaterials) m).collect(Collectors.toSet());
    }

    @Override
    @Transient
    public String getMatFormula() {
        return getVariableValueAsString("materialFormula").orElse("");
    }

    private Optional<String> getVariableValueAsString(String key) {
        return getVariables().stream().filter(v -> key.equals(v.getKey())).map(Variable::getValue).findFirst();
    }

    // Метод @PrePersist addVariable() был удален.
    // Теперь инициализация переменных происходит через
    // AbstractProductType.initializeVariables()
    // при создании нового объекта в TemplatesView.EntityFactory.
    
}
