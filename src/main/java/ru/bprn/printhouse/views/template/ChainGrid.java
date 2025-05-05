package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChainGrid extends VerticalLayout {

    private final Button createButton = new Button(VaadinIcon.PLUS.create());
    private final Button updateButton  = new Button(VaadinIcon.EDIT.create());
    private final Button duplicateButton = new Button(VaadinIcon.COPY_O.create());
    private final Button deleteButton = new Button(VaadinIcon.CLOSE.create());
    private final Grid<? extends WorkChain> grid = new Grid<>(WorkChain.class, false);

    public ChainGrid(Class<WorkChain> clazz) {
        super();
        this.setSizeUndefined();

        //this.addColumn(WorkFlow::getName).setHeader("Имя");
        //this.setItems(this.workFlowService.findAll());
        grid.setHeight("200px");
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        var hl = new HorizontalLayout();
        hl.add(createButton, updateButton, duplicateButton, deleteButton);
        this.add(hl,this);

    }

    public void setGridData(){

    }

    public void create(){

    }

    public void update(){

    }

    public void delete(){

    }

}
