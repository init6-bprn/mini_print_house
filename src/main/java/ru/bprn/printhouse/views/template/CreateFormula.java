package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;

import java.util.ArrayList;
import java.util.List;

public class CreateFormula extends Dialog {
    public CreateFormula() {

        this.setHeaderTitle("Редактирование формулы");
        var name = new TextField("Название формулы");
        var formulaField = new TextField("Фомула");

        this.add(name, formulaField, addVariablesButton());

        var saveButton = new Button("Save", buttonClickEvent -> {
            if (isValid()) save();
            this.close();
        });
        var cancelButton = new Button("Cancel", buttonClickEvent -> this.close());

        this.getFooter().add(cancelButton);
        this.getFooter().add(saveButton);
    }

    private Div addVariablesButton() {
        var div = new Div();
        VariablesRecord variable = new VariablesRecord("");
        List<VariablesRecord> list = new ArrayList<VariablesRecord>();
        list.add(variable);
        return  div;
    }

    private void save() {
    }

    private boolean isValid() {
        return true;
    }

}
