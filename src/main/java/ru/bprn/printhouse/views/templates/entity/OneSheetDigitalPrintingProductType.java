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

public class OneSheetDigitalPrintingProductType extends AbstractProductType {

    @ManyToOne(fetch = FetchType.EAGER)
    private PrintSheetsMaterial defaultMaterial;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.REFRESH}) // Оставил только одну аннотацию с правильным каскадом
    @JoinTable(
            name = "one_sheet_digital_printing_product_type___abstract_material",
            joinColumns = @JoinColumn(name = "product_type_id"),
            inverseJoinColumns = @JoinColumn(name = "material_id"))
    private Set<PrintSheetsMaterial> selectedMaterials = new HashSet<>();

    /**
     * Возвращает основной материал (бумагу) для этого компонента.
     * Реализует абстрактный метод из родителя.
     */
    @Override
    public PrintSheetsMaterial getDefaultMaterial() {
        return this.defaultMaterial;
    }

    @Override
    public Set<AbstractMaterials> getSelectedMaterials() {
        // Возвращаем копию, чтобы избежать прямого изменения коллекции
        return new HashSet<>(this.selectedMaterials);
    }

    /**
     * Кастомный сеттер для `selectedMaterials`, который принимает более общий тип Set&lt;AbstractMaterials&gt;
     * и выполняет приведение типов. Это упрощает привязку данных в UI.
     * @param materials Набор материалов общего типа.
     */
    public void setSelectedMaterials(Set<AbstractMaterials> materials) {
        if (materials == null) {
            this.selectedMaterials = new HashSet<>();
        } else {
            this.selectedMaterials = materials.stream()
                    .map(m -> (PrintSheetsMaterial) m)
                    .collect(Collectors.toSet());
        }
    }

    // Метод @PrePersist addVariable() был удален.
    // Теперь инициализация переменных происходит через
    // AbstractProductType.initializeVariables()
    // при создании нового объекта в TemplatesView.EntityFactory.
    
}
