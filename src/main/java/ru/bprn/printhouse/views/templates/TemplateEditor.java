package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import ru.bprn.printhouse.views.templates.entity.Templates;

import java.util.List;
import java.util.function.Consumer;

public class TemplateEditor extends AbstractEditor<Templates> {

    private final TextField name = new TextField("Название шаблона:");
    private final TextArea description = new TextArea("Краткое описание:");
    private final IntegerField minQuantity = new IntegerField("Минимальный тираж");
    private final IntegerField maxQuantity = new IntegerField("Максимальный тираж");
    private final IntegerField step = new IntegerField("Шаг тиража, кратно:");
    private final IntegerField quantityField = new IntegerField("Тираж по умолчанию");
    private final Checkbox checkbox = new Checkbox("Математическое округление (uncheck - округление в большую)", true);

    public TemplateEditor(Templates templates, Consumer<Object> onSave){
        super(onSave);

        description.setWidthFull();
        description.setMaxRows(10);
        description.setMinRows(3);

        this.binder.forField(name).bind(Templates::getName, Templates::setName);
        this.binder.bind(description, Templates::getDescription, Templates::setDescription);
        this.binder.forField(minQuantity).bind(Templates::getMinQuantity, Templates::setMinQuantity);
        this.binder.forField(maxQuantity).bind(Templates::getMaxQuantity, Templates::setMaxQuantity);
        this.binder.forField(quantityField).bind(Templates::getQuantity, Templates::setQuantity);
        this.binder.forField(step).bind(Templates::getStep, Templates::setStep);
        this.binder.forField(checkbox).bind(Templates::isRoundForMath, Templates::setRoundForMath);

        this.edit(templates);
        add(buildForm());
        addButtons();
    }


    @Override
    protected Component buildForm() {
        FormLayout form = new FormLayout();
        form.setColumnWidth("6em");
        form.setResponsiveSteps(List.of(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("120px", 2),
                new FormLayout.ResponsiveStep("240px", 3),
                new FormLayout.ResponsiveStep("360px", 4),
                new FormLayout.ResponsiveStep("480px", 5),
                new FormLayout.ResponsiveStep("600px", 6)
        ));
        form.setAutoResponsive(true);
        form.setExpandFields(true);
        var row1 = new FormLayout.FormRow();
        row1.add(name, 6);
        var row2 = new FormLayout.FormRow();
        row2.add(description, 6);
        var row3 = new FormLayout.FormRow();
        row3.add(quantityField, minQuantity, maxQuantity, step);
        var row4 = new FormLayout.FormRow();
        row4.add(checkbox, 6);
        form.add(row1, row2, row3, row4);
        form.setExpandColumns(true);
        form.setWidthFull();
        return form;
    }
}
