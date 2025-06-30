package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import lombok.Getter;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.service.AbstractMaterialService;

import java.util.Set;

public class SelectAbstractMaterialsDialog extends Dialog {

    private final TextField filterField = new TextField();
    private final AbstractMaterialService service;
    @Getter
    private Grid<AbstractMaterials> grid = new Grid<>();

    public SelectAbstractMaterialsDialog(String title, AbstractMaterialService service){
        super(title);
        this.service = service;
        this.setCloseOnEsc(true);
        this.setCloseOnOutsideClick(false);
        this.setModal(true);
        this.setHeight("75%");
        this.setWidth("75%");

        var saveButton = new Button("Ok", buttonClickEvent -> this.close());
        this.getFooter().add(saveButton);
        this.add(addGrid());
    }

    public void setSelectedMaterial(Set<AbstractMaterials> materials) {
        if (!materials.isEmpty()) for (AbstractMaterials m : materials) grid.select(m);
    }

    private Component addGrid(){
        var layout = new VerticalLayout();
        layout.setSizeFull();
        filterField.setWidth("50%");
        filterField.setPlaceholder("Поиск");
        filterField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        filterField.setValueChangeMode(ValueChangeMode.LAZY);
        filterField.addValueChangeListener(e -> populate(e.getValue().trim()));
        filterField.setClearButtonVisible(true);

        grid.addColumn(AbstractMaterials::getName).setHeader("Название");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();
        grid.getHeaderRows().clear();
        grid.getListDataView();
        layout.add(filterField, grid);

        return layout;
    }

    public void populate(String str) {
        grid.setItems(service.populate(str));

    }
}
