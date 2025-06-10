package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.Setter;
import ru.bprn.printhouse.views.templates.entity.Chains;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.service.TemplatesService;

import java.util.HashSet;
import java.util.Set;

public class AddChainDialog extends Dialog {

    @Setter
    private Templates template;
    private final TemplatesService templatesService;

    public AddChainDialog(Templates template, TemplatesService templatesService){
        super();
        this.template=template;
        this.templatesService=templatesService;
        this.setHeaderTitle("Добавить цепочку");
        this.setWidth("60%");
        this.setHeight("60%");
        this.add(components());

    }

    private void addChainToTemplate(Set<Chains> set) {
        var oldSet = template.getChains();
        Set<Chains> newSet = new HashSet<>(oldSet);
        for (Chains c : set) newSet.add(templatesService.duplicateChain(c));
        template.setChains(newSet);
        templatesService.saveAndFlush(template);
    }

    private Component components() {
        var vl = new VerticalLayout();
        vl.setSizeFull();
        var grid = new Grid<>(Chains.class, true);
        grid.setSizeFull();
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setItems(templatesService.finAllChains());
        this.addOpenedChangeListener(openedChangeEvent -> grid.setItems(templatesService.finAllChains()));

        vl.add(grid);

        var hl = new HorizontalLayout();
        var saveButton = new Button("Сохранить", event -> {
            addChainToTemplate(grid.asMultiSelect().getSelectedItems());
            this.close();
        });
        var cancelButton = new Button("Отменить", event -> this.close());
        hl.add(saveButton, cancelButton);
        vl.add(hl);
        return vl;
    }
}
