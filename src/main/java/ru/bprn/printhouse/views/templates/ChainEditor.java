package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import lombok.Setter;
import ru.bprn.printhouse.views.templates.entity.AbstractTemplate;
import ru.bprn.printhouse.views.templates.entity.Chains;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.service.ChainsService;
import ru.bprn.printhouse.views.templates.service.TemplatesService;

import java.util.ArrayList;
import java.util.List;

public class ChainEditor extends VerticalLayout {


    private Chains chains;

    @Setter
    private Templates template;

    private final BeanValidationBinder<Chains> chainsBinder = new BeanValidationBinder<>(Chains.class);
    private final ChainsService service;
    private final TemplatesService templatesService;
    private final TreeGrid<AbstractTemplate> treeGrid;
    private final SplitLayout splitLayout;

    public ChainEditor(SplitLayout splitLayout, TreeGrid<AbstractTemplate> treeGrid, ChainsService service, TemplatesService templatesService){
        this.templatesService = templatesService;
        this.chains = new Chains();
        this.service = service;
        this.treeGrid = treeGrid;
        this.splitLayout = splitLayout;
        this.setSizeFull();

    var name = new TextField("Название цепочки:");
        name.setWidthFull();
        this.chainsBinder.bind(name, Chains::getName, Chains::setName);

    var saveButton = new Button("Save", o -> saveBean());
    var cancelButton = new Button("Cancel", o ->cancelBean());
    var hl = new HorizontalLayout(FlexComponent.Alignment.END, saveButton, cancelButton);

        this.add(name, hl);

        chainsBinder.readBean(this.chains);
        this.chainsBinder.refreshFields();
}

private void saveBean() {
    if (chainsBinder.writeBeanIfValid(chains)) {
        service.save(chains);
        Notification.show("Цепочка сохранена!");
        var set = template.getChains();
        service.findById(chains.getId()).ifPresent(set::add);
        template.setChains(set);
        templatesService.save(template);
        Notification.show("Шаблон сохранен!");
        showPrimary();
        treeGrid.setItems(templatesService.findAllAsAbstractTemplates(), this::getChains);
    }
}

private void cancelBean(){
    treeGrid.setItems(templatesService.findAllAsAbstractTemplates(), this::getChains);
    showPrimary();
}

private void showPrimary(){
    splitLayout.getPrimaryComponent().setVisible(true);
    splitLayout.getSecondaryComponent().getElement().setEnabled(false);
    splitLayout.setSplitterPosition(50);
}

private List<AbstractTemplate> getChains(AbstractTemplate abstractTemplate) {
    var template = templatesService.findById(abstractTemplate.getId());
    List<AbstractTemplate> list = new ArrayList<>();
    template.ifPresent(templates -> list.addAll(templates.getChains()));
    return list;
}

public void setChains(Chains chains) {
    chainsBinder.removeBean();
    chainsBinder.refreshFields();
    this.chains = chains;
    chainsBinder.readBean(this.chains);

}
}
