package ru.bprn.printhouse.views.operation;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.function.ValueProvider;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.data.service.VariablesForMainWorksService;
import ru.bprn.printhouse.views.operation.service.FormulaEditor;
import ru.bprn.printhouse.views.operation.service.TypeOfOperationService;
import ru.bprn.printhouse.views.templates.FormulaDialog;

public class EditableTextArea<T> extends FormLayout.FormRow {
    private final TextArea area = new TextArea();
    public Binder<T> binder;

     public EditableTextArea(String label, Binder<T> binder, ValueProvider<T, String> getter, Setter<T, String> setter,
                                TypeOfOperationService worksService, VariablesForMainWorksService variablesService,
                                FormulasService formulasService) {
            this.binder = binder;
            area.setLabel(label);
            area.getElement().getStyle().set("align-self", "baseline");
            Button select = new Button("Выбрать", e-> {
                new FormulaDialog(formulasService, worksService, formulas->{
                    Notification.show("Вы выбрали: " + formulas.getName());
                    area.setValue(formulas.getFormula());
                }).open();
            });
            select.getElement().getStyle().set("align-self", "baseline");

            Button edit = new Button("Править", e -> {
                new FormulaEditor(area.getValue(), area::setValue, worksService, variablesService).open();
            });
            edit.getElement().getStyle().set("align-self", "baseline");

            binder.forField(area).bind(getter, setter);

            add(select, 1);
        add(area, 4);
        add(edit, 1);
     }

    public void refresh(Binder<T> binder) {
        this.binder = binder;
        this.binder.refreshFields();
    }

}
