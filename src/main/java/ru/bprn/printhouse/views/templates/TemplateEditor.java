package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import ru.bprn.printhouse.views.templates.entity.AbstractTemplate;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.service.TemplatesService;

public class TemplateEditor extends VerticalLayout {
    private final Templates template;
    private final BeanValidationBinder<Templates> templatesBinder = new BeanValidationBinder<>(Templates.class);
    private final TemplatesService service;
    private final TreeGrid<AbstractTemplate> treeGrid;

    public TemplateEditor(Templates template, TreeGrid<AbstractTemplate> treeGrid, TemplatesService service){
        super();

        this.template = template;
        this.service = service;
        this.treeGrid = treeGrid;
        this.setSizeFull();

        templatesBinder.setBean(template);
        var name = new TextField("Название шаблона:");
        name.setWidthFull();
        templatesBinder.bind(name, Templates::getName, Templates::setName);

        var description = new TextArea("Краткое описание:");
        description.setWidthFull();
        description.setMaxRows(5);
        templatesBinder.bind(description, Templates::getDescription, Templates::setDescription);

        var saveButton = new Button("Save", o -> saveBean());
        var cancelButton = new Button("Cancel", o ->cancelBean());
        var hl = new HorizontalLayout(FlexComponent.Alignment.END, saveButton, cancelButton);

        this.add(name, description, hl);
    }

    private void saveBean() {
        if (template != null) {
            try {
                templatesBinder.writeBean(template);
                service.save(template);
                Notification.show("Сохранено!");
                treeGrid.getDataProvider().refreshAll();
            } catch (ValidationException e) {
                Notification.show("Есть невалидные значения. Не сохранено!");
            }
        }
        else Notification.show("Нечего сохранять!");
    }

    private void cancelBean(){
        templatesBinder.removeBean();
        templatesBinder.refreshFields();
        treeGrid.getDataProvider().refreshAll();
    }
}
