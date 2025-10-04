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

import java.util.HashSet;
import java.util.Map;
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

    @Override
    public void calculateLayoutSpecifics(Map<String, Object> context) {
        // Здесь находится вся логика расчета раскладки для листовой печати.

        double productWidth = (double) context.get("productWidthBeforeCut");
        double productLength = (double) context.get("productLengthBeforeCut");
        double workableSheetWidth = (double) context.get("workableSheetWidth");
        double workableSheetLength = (double) context.get("workableSheetLength");

        int cols_v = (int) (workableSheetWidth / productWidth);
        int rows_v = (int) (workableSheetLength / productLength);
        int total_v = cols_v * rows_v;

        int cols_h = (int) (workableSheetWidth / productLength);
        int rows_h = (int) (workableSheetLength / productWidth);
        int total_h = cols_h * rows_h;

        int quantityOnSheet;
        int rows = 0;
        int columns = 0;

        if (total_v >= total_h) {
            quantityOnSheet = total_v;
            rows = rows_v;
            columns = cols_v;   
        } else {
            quantityOnSheet = total_h;
            rows = rows_h;
            columns = cols_h;
        }

        // Обогащаем контекст новыми, готовыми к использованию переменными
        context.put("quantityProductsOnMainMaterial", quantityOnSheet);
        context.put("rows", rows);
        context.put("columns", columns);


        if (quantityOnSheet > 0) {
            int quantity = (int) context.get("quantity");
            double baseSheets = Math.ceil((double) quantity / quantityOnSheet);
            context.put("baseSheets", baseSheets);

            // Расчет веса одного изделия в граммах
            if (this.getDefaultMaterial() != null) {
                double itemAreaM2 = (productWidth * productLength) / 1_000_000.0; // Площадь изделия в м2
                double density = this.getDefaultMaterial().getThickness(); // Плотность в г/м2
                context.put("mass", itemAreaM2 * density);
            } else {
                context.put("mass", 0.0);
            }
        } else {
            context.put("baseSheets", Double.POSITIVE_INFINITY);
            context.put("mass", 0.0);
        }
    }
}
