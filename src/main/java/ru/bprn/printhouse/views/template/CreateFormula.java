package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import lombok.Setter;
import ru.bprn.printhouse.data.entity.Formulas;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.List;

public class CreateFormula extends Dialog {
    private final TextField formulaField = new TextField("Фомула");
    private final BeanValidationBinder<Formulas> formulaBinder;
    private final StringBuilder strVariables = new StringBuilder();
    private final List<VariablesRecord> list;
    @Setter
    private Formulas formulaBean;

    public CreateFormula(Formulas bean) {
        this.formulaBean = bean;
        this.formulaBinder = new BeanValidationBinder<>(Formulas.class);
        formulaBinder.setBean(bean);

        list = new ListOfVariables().getList();
        for (VariablesRecord rec:list){
            strVariables.append(rec.name()).append(" = 1;");
        }

        this.setHeaderTitle("Редактирование формулы");
        var name = new TextField("Название формулы");
        formulaBinder.forField(name).withValidator(s -> !s.isEmpty(), "Не может быть пустым!")
                .bind(Formulas::getName, Formulas::setName);
        formulaBinder.forField(formulaField).withValidator(s -> {
            strVariables.append(s).append(";");
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            try {
                double d = ((Number) engine.eval(strVariables.toString())).doubleValue();
            } catch (ScriptException e) {
                return true;
            }
            return false;
        }, "Формула не верна!").bind(Formulas::getFormula, Formulas::setFormula);

        this.add(name, formulaField, addVariablesButton());

        var saveButton = new Button("Save", buttonClickEvent -> {
            if (formulaBinder.isValid()) {
                try {
                    formulaBinder.writeBean(formulaBean);
                } catch (ValidationException e) {
                    Notification.show("Невозможно сохранить объект в БД");
                }
                this.close();
            }
        });
        var cancelButton = new Button("Cancel", buttonClickEvent -> {
            formulaBinder.removeBean();
            this.close();
        });

        this.getFooter().add(cancelButton);
        this.getFooter().add(saveButton);
    }

    private Div addVariablesButton() {
        var div = new Div();
        for (VariablesRecord rec:list) {
            var button = new Button(rec.name(), buttonClickEvent -> {
                formulaField.setValue(formulaField.getValue()+rec.name()+" ");
            });
            button.setTooltipText(rec.description());
            //button.setThemeName();
            div.add(button);
        }

        return  div;
    }

}
