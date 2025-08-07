package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import ru.bprn.printhouse.views.templates.entity.Templates;

import java.util.function.Consumer;

public class TemplateEditor extends AbstractEditor<Templates> {

    private final TextField name = new TextField("Название шаблона:");
    private final TextArea description = new TextArea("Краткое описание:");

    public TemplateEditor(Templates templates, Consumer<Templates> onSave){
        super(onSave);
        this.edit(templates);

        this.binder.forField(name).bind(Templates::getName, Templates::setName);

        description.setWidthFull();
        description.setMaxRows(5);
        this.binder.bind(description, Templates::getDescription, Templates::setDescription);

        add(buildForm());
    }


    @Override
    protected Component buildForm() {
        var form = new FormLayout(name, description);
        form.setAutoResponsive(true);
        form.setWidthFull();

        return form;
    }
}
