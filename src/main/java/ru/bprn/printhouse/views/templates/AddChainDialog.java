package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import ru.bprn.printhouse.views.templates.entity.Chains;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.service.ChainsService;

import java.util.Set;

public class AddChainDialog extends Dialog {

    private final Templates template;
    private final ChainsService chainsService;

    public AddChainDialog(Templates template, ChainsService chainsService){
        super();
        this.template=template;
        this.chainsService=chainsService;
        this.setHeaderTitle("Добавить цепочку");
        //this.setText("Выберите цепочку из списка");
        this.add(components());
        //this.setConfirmButton("Добавить", confirm ->{
    }

    private void addChainToTemplate(Set<Chains> set) {
        var oldSet = template.getChains();
    }

    private Component components() {
        var vl = new VerticalLayout();
        var grid = new Grid<>(Chains.class, false);
        grid.setWidthFull();
        grid.setHeight("40%");
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        vl.add(grid);

        var hl = new HorizontalLayout();
        var saveButton = new Button("", event -> {
            addChainToTemplate(grid.asMultiSelect().getSelectedItems());
        });
        return vl;
    }
}
