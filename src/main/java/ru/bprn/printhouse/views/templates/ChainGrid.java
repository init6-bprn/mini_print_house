package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChainGrid extends VerticalLayout {

    private final Button createButton = new Button(VaadinIcon.PLUS.create());
    private final Button updateButton  = new Button(VaadinIcon.EDIT.create());
    private final Button duplicateButton = new Button(VaadinIcon.COPY_O.create());
    private final Button deleteButton = new Button(VaadinIcon.CLOSE.create());
    private Grid<WorkChain> grid = new Grid<>(WorkChain.class, false);
    private WorkChain workChain;

    public ChainGrid(List<WorkChain> workChain) {
        super();
        this.setSizeUndefined();

        grid.addColumn(WorkChain::getString).setHeader("Название цепочки:");
        grid.setItems(workChain);
        grid.setHeight("200px");
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        var hl = new HorizontalLayout();
        hl.add(createButton, updateButton, duplicateButton, deleteButton);
        this.add(hl,this);

    }

    public void setGridData(List<WorkChain> workChain){
        grid.setItems(workChain);
        grid.getListDataView().refreshAll();

    }

    public void create(){

    }

    public void update(){

    }

    public void delete(){

    }

}
