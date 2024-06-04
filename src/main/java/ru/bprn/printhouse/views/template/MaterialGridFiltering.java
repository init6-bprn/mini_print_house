package ru.bprn.printhouse.views.template;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.value.ValueChangeMode;
import ru.bprn.printhouse.data.entity.Material;
import ru.bprn.printhouse.data.service.MaterialService;

import java.util.List;
import java.util.function.Consumer;

public class MaterialGridFiltering extends HorizontalLayout {


    public MaterialGridFiltering(MaterialService materialService) {

        Grid<Material> grid = new Grid<>(Material.class, false);
        Grid.Column<Material> typeColumn = grid.addColumn(Material::getTypeOfMaterial);
        Grid.Column<Material> sizeColumn = grid.addColumn(Material::getSizeOfPrintLeaf);
        Grid.Column<Material> thicknessColumn = grid.addColumn(Material::getThickness);

        List<Material> material = materialService.findAll();
        GridListDataView<Material> dataView = grid.setItems(material);
        PersonFilter personFilter = new PersonFilter(dataView);

        grid.getHeaderRows().clear();
        HeaderRow headerRow = grid.appendHeaderRow();

        headerRow.getCell(typeColumn).setComponent(
                createFilterHeader("Тип материала", personFilter::setFullName));
        headerRow.getCell(sizeColumn).setComponent(
                createFilterHeader("Размер печатного листа", personFilter::setEmail));
        headerRow.getCell(thicknessColumn).setComponent(
                createFilterHeader("Плотность", personFilter::setProfession));
        // end::snippet1[]

        add(grid);
    }

    private static Component createFilterHeader(String labelText,
                                                Consumer<String> filterChangeConsumer) {

        NativeLabel label = new NativeLabel(labelText);
        label.getStyle().set("padding-top", "var(--lumo-space-m)")
                .set("font-size", "var(--lumo-font-size-xs)");
        TextField textField = new TextField();
        textField.setValueChangeMode(ValueChangeMode.EAGER);
        textField.setClearButtonVisible(true);
        textField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        textField.setWidthFull();
        textField.getStyle().set("max-width", "100%");
        textField.addValueChangeListener(
                e -> filterChangeConsumer.accept(e.getValue()));
        VerticalLayout layout = new VerticalLayout(label, textField);
        layout.getThemeList().clear();
        layout.getThemeList().add("spacing-xs");

        return layout;
    }

    private static class PersonFilter {
        private final GridListDataView<Material> dataView;

        private String fullName;
        private String email;
        private String profession;

        public PersonFilter(GridListDataView<Material> dataView) {
            this.dataView = dataView;
            this.dataView.addFilter(this::test);
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
            this.dataView.refreshAll();
        }

        public void setEmail(String email) {
            this.email = email;
            this.dataView.refreshAll();
        }

        public void setProfession(String profession) {
            this.profession = profession;
            this.dataView.refreshAll();
        }

        public boolean test(Material person) {
            boolean matchesFullName = matches(person.getTypeOfMaterial().toString(), fullName);
            boolean matchesEmail = matches(person.getSizeOfPrintLeaf().toString(), email);
            boolean matchesProfession = matches(person.getThickness().toString(), profession);

            return matchesFullName && matchesEmail && matchesProfession;
        }

        private boolean matches(String value, String searchTerm) {
            return searchTerm == null || searchTerm.isEmpty()
                    || value.toLowerCase().contains(searchTerm.toLowerCase());
        }
    }

}

