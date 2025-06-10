package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
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
    private final Grid<Chains> grid = new Grid<>(Chains.class, true);
    GridListDataView<Chains> dataView;

    public AddChainDialog(Templates template, TemplatesService templatesService){
        //super();
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
        grid.setSizeFull();
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        dataView = grid.setItems(templatesService.finAllChains());

        TextField text = new TextField();
        text.setWidth("50%");
        text.setPlaceholder("Поиск");
        text.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        text.setValueChangeMode(ValueChangeMode.EAGER);
        text.addValueChangeListener(e -> dataView.refreshAll());

        dataView.addFilter(item->{
           String search = text.getValue().trim();
           if (search.isEmpty()) return true;
           else return item.getName().toLowerCase().contains(search.toLowerCase());
        });

        vl.add(text, grid);

        var hl = new HorizontalLayout();
        hl.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        hl.setWidth("100%");
        var saveButton = new Button("Сохранить", event -> {
            addChainToTemplate(grid.asMultiSelect().getSelectedItems());
            this.close();
        });
        var cancelButton = new Button("Отменить", event -> this.close());
        hl.add(saveButton, cancelButton);
        vl.add(hl);
        return vl;
    }

    public void populate() { dataView = grid.setItems(templatesService.finAllChains());}
}
