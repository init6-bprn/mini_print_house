package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.theme.lumo.LumoUtility;
import lombok.Getter;
import ru.bprn.printhouse.data.entity.Formulas;
import ru.bprn.printhouse.views.operation.entity.TypeOfOperation;
import ru.bprn.printhouse.data.entity.VariablesForMainWorks;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.views.operation.service.TypeOfOperationService;
import ru.bprn.printhouse.data.entity.CalculationPhase;
import ru.bprn.printhouse.data.service.VariablesForMainWorksService;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class CreateFormula extends VerticalLayout {

    private final TextArea formulaField = new TextArea("Формула");
    private final BeanValidationBinder<Formulas> formulaBinder;
    private final StringBuilder strVariables = new StringBuilder();
    private final VariablesForMainWorksService list;
    private final TypeOfOperationService worksService;

    @Getter
    private Formulas formulaBean = new Formulas();

    public CreateFormula(FormulasService formulasService, VariablesForMainWorksService variables, TypeOfOperationService worksService) {
        this.worksService = worksService;
        formulaBinder = new BeanValidationBinder<>(Formulas.class);
        formulaBinder.setBean(formulaBean);
        this.list = variables;

        for (VariablesForMainWorks rec : list.findAll()){
            strVariables.append(rec.getName()).append(" = 1;");
        }

        var selectType = new Select<TypeOfOperation>();
        selectType.setItems(worksService.findAll());
        selectType.setLabel("Тип работы");
        formulaBinder.forField(selectType).asRequired().bind(Formulas::getTypeOfOperation, Formulas::setTypeOfOperation);

        var name = new TextField("Название формулы");
        formulaBinder.forField(name).withValidator(s -> !s.isEmpty(), "Не может быть пустым!")
                .bind(Formulas::getName, Formulas::setName);
        formulaBinder.forField(formulaField).withValidator(s -> {
            // Пропускаем валидацию для пустых формул, чтобы можно было сохранить шаблон
            if (s == null || s.isBlank()) {
                return true;
            }
            String str = strVariables + s+ ";";
            //Notification.show(str);
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            try {
                double d = ((Number) engine.eval(str)).doubleValue();
            } catch (ScriptException e) {
                return false;
            }
            return true;
        }, "Формула не верна!").bind(Formulas::getFormula, Formulas::setFormula);

        var phaseSelect = new Select<CalculationPhase>();
        phaseSelect.setLabel("Фаза расчета");
        phaseSelect.setItems(CalculationPhase.values());
        formulaBinder.forField(phaseSelect).asRequired().bind(Formulas::getPhase, Formulas::setPhase);

        var priorityField = new IntegerField("Приоритет");
        priorityField.setTooltipText("Чем меньше число, тем раньше выполнится формула внутри одной фазы");
        formulaBinder.forField(priorityField).asRequired().bind(Formulas::getPriority, Formulas::setPriority);

        name.setWidthFull();
        formulaField.setWidthFull();
        formulaField.setClearButtonVisible(true);
        formulaField.setMaxRows(3);

        this.add(selectType, name, new HorizontalLayout(phaseSelect, priorityField), formulaField, addVariablesButton());

        var saveButton = new Button("Save", buttonClickEvent -> {
            if (formulaBinder.isValid()) {
                try {
                    formulaBinder.writeBean(formulaBean);
                    formulasService.save(formulaBean);
                } catch (ValidationException e) {
                    Notification.show("Невозможно сохранить объект в БД");
                }
                close();
            }
        });
        var cancelButton = new Button("Cancel", buttonClickEvent -> {
            formulaBinder.removeBean();
            close();
        });
        var hl = new HorizontalLayout(JustifyContentMode.END, cancelButton, saveButton);
        hl.setWidthFull();
        this.add(hl);
    }

    private Div addVariablesButton() {
        var div = new Div();
        div.addClassNames(LumoUtility.Border.ALL, LumoUtility.Whitespace.NORMAL);
        for (VariablesForMainWorks rec : list.findAll()) {
            var button = new Button(rec.getName(), buttonClickEvent -> formulaField.setValue(formulaField.getValue()+rec.getName()+" "));
            button.setTooltipText(rec.getDescription());
            button.addThemeVariants(ButtonVariant.LUMO_SMALL);
            button.addClassNames(LumoUtility.Margin.SMALL);
            div.add(button);
        }

        return  div;
    }

    public void setFormulaBean(Formulas formula) {
        this.formulaBean = formula;
        formulaBinder.removeBean();
        formulaBinder.setBean(formulaBean);
        formulaBinder.refreshFields();
    }

    private void close(){
        if (this.getParent().isPresent()) {
            if (this.getParent().get() instanceof Dialog dialog) {
                dialog.close();
            }
        }
    }

}
