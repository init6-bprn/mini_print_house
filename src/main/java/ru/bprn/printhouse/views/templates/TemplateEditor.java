package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import ru.bprn.printhouse.views.templates.entity.AbstractTemplate;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.service.TemplatesService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TemplateEditor extends VerticalLayout {

    private Templates template;

    private final BeanValidationBinder<Templates> templatesBinder = new BeanValidationBinder<>(Templates.class);
    private final TemplatesService service;
    private final TreeGrid<AbstractTemplate> treeGrid;
    private final SplitLayout splitLayout;

    public TemplateEditor(SplitLayout splitLayout, TreeGrid<AbstractTemplate> treeGrid, TemplatesService service){
        this.service = service;
        this.treeGrid = treeGrid;
        this.splitLayout = splitLayout;
        this.setSizeFull();

        var name = new TextField("Название шаблона:");
        name.setWidthFull();
        this.templatesBinder.bind(name, Templates::getName, Templates::setName);

        var description = new TextArea("Краткое описание:");
        description.setWidthFull();
        description.setMaxRows(5);
        this.templatesBinder.bind(description, Templates::getDescription, Templates::setDescription);

        var saveButton = new Button("Save", o -> saveBean());
        var cancelButton = new Button("Cancel", o ->cancelBean());
        var hl = new HorizontalLayout(FlexComponent.Alignment.END, saveButton, cancelButton);

        this.add(name, description, hl);

        templatesBinder.readBean(this.template);
        this.templatesBinder.refreshFields();
    }

    private void saveBean() {
        if (templatesBinder.writeBeanIfValid(template)) {
            service.save(template);
            Notification.show("Сохранено!");
            showPrimary();
            populateGrid();
        }
    }

    private void cancelBean(){
        //treeGrid.setItems(service.findAllAsAbstractTemplates(), this::getChains);
        showPrimary();
    }

    private void showPrimary(){
        splitLayout.getPrimaryComponent().setVisible(true);
        splitLayout.getSecondaryComponent().getElement().setEnabled(false);
        splitLayout.setSplitterPosition(50);
    }

    private List<AbstractTemplate> getChains(AbstractTemplate abstractTemplate) {
        var template = service.findById(abstractTemplate.getId());
        List<AbstractTemplate> list = new ArrayList<>();
        template.ifPresent(templates -> list.addAll(templates.getChains()));
        return list;
    }

    public void setTemplate(Templates template) {
        templatesBinder.removeBean();
        templatesBinder.refreshFields();
        this.template = template;
        templatesBinder.readBean(this.template);

    }

    private void populateGrid() {
        Collection<AbstractTemplate> collection = service.findAllAsAbstractTemplates();
        TreeData<AbstractTemplate> data = new TreeData<>();
        data.addItems(null, collection);
        for (AbstractTemplate temp : collection) {
            if (temp instanceof Templates) {
                Templates t = (Templates) temp;
                Collection<AbstractTemplate> c = new ArrayList<>(t.getChains());
                data.addItems(temp, c);
            }
        }
        TreeDataProvider<AbstractTemplate> treeData = new TreeDataProvider<>(data);
        treeGrid.setDataProvider(treeData);
    }
}
