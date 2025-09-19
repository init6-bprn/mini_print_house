package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class TemplateEditor extends AbstractEditor<Templates> {

    private final TextField name = new TextField("Название шаблона:");
    private final TextArea description = new TextArea("Краткое описание:");

    // Поля для переменных
    private final IntegerField quantityField = new IntegerField("Тираж по умолчанию");
    private final IntegerField minQuantityField = new IntegerField("Минимальный тираж");
    private final IntegerField maxQuantityField = new IntegerField("Максимальный тираж");
    private final IntegerField stepQuantityField = new IntegerField("Шаг тиража");
    private final Checkbox roundCheckbox = new Checkbox("Математическое округление");
    private final TextArea roundMaskArea = new TextArea("Маска/формула округления");

    private final Binder<Variable> quantityBinder = new Binder<>(Variable.class);

    public TemplateEditor(Templates templates, Consumer<Object> onSave){
        super(onSave);

        description.setWidthFull();
        description.setMinRows(2);

        roundMaskArea.setWidthFull();
        roundMaskArea.setMinRows(2);
        roundMaskArea.setTooltipText("Можно использовать Groovy-код. Пример:\n" +
                "if (quantity <= 100) return '#'; // до рублей\n" +
                "if (quantity <= 1000) return '#.#'; // до десятков копеек\n" +
                "return '#.##'; // до копеек");

        this.binder.forField(name).bind(Templates::getName, Templates::setName);
        this.binder.bind(description, Templates::getDescription, Templates::setDescription);

        // Привязка к переменным
        bindVariable(quantityField, "quantity");
        bindVariable(roundCheckbox, "round");
        bindVariable(roundMaskArea, "roundMask");

        // Привязка к ограничениям переменной quantity
        quantityBinder.forField(minQuantityField)
                .withConverter(v -> v != null ? String.valueOf(v) : null, s -> tryParseInt(s).orElse(null))
                .bind(Variable::getMinValue, Variable::setMinValue);
        quantityBinder.forField(maxQuantityField)
                .withConverter(v -> v != null ? String.valueOf(v) : null, s -> tryParseInt(s).orElse(null))
                .bind(Variable::getMaxValue, Variable::setMaxValue);
        quantityBinder.forField(stepQuantityField)
                .withConverter(v -> v != null ? String.valueOf(v) : null, s -> tryParseInt(s).orElse(null))
                .bind(Variable::getStep, Variable::setStep);

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

        // Секция для тиража
        FormLayout quantityLayout = new FormLayout(quantityField, minQuantityField, maxQuantityField, stepQuantityField);
        quantityLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 4));
        Details quantityDetails = new Details("Настройки тиража", quantityLayout);
        quantityDetails.setOpened(true);

        var row3 = new FormLayout.FormRow();
        row3.add(quantityDetails, 6);

        // Секция для округления
        FormLayout roundingLayout = new FormLayout(roundCheckbox, roundMaskArea);
        roundingLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        Details roundingDetails = new Details("Настройки округления", roundingLayout);
        roundingDetails.setOpened(true);

        var row4 = new FormLayout.FormRow();
        row4.add(roundingDetails, 6);

        form.add(row1, row2, row3, row4);
        form.setExpandColumns(true);
        form.setWidthFull();
        return form;
    }

    private <FIELD extends HasValue<?, VALUE>, VALUE> void bindVariable(FIELD field, String key) {
        binder.forField(field)
                .bind(
                        template -> getVariable(template, key).map(v -> (VALUE)v.getValueAsObject()).orElse(null),
                        (template, value) -> getVariable(template, key).ifPresent(v -> v.setValue(value))
                );
    }

    private Optional<Variable> getVariable(Templates template, String key) {
        if (template == null || template.getVariables() == null) {
            return Optional.empty();
        }
        return template.getVariables().stream()
                .filter(v -> key.equals(v.getKey()))
                .findFirst();
    }

    private Optional<Integer> tryParseInt(String s) {
        if (s == null || s.isBlank()) return Optional.empty();
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @Override
    public void edit(Templates entity) {
        super.edit(entity);
        // Необходимо перепривязать биндер для ограничений при смене сущности
        getVariable(entity, "quantity").ifPresent(quantityBinder::setBean);
    }
}
