package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import lombok.Getter;
import ru.bprn.printhouse.data.entity.StandartSize;
import ru.bprn.printhouse.data.service.StandartSizeService;

public class
SizeDialog extends Dialog {
    private final TextField text;
    private final NumberField lengthField;
    private final NumberField widthField;
    @Getter
    private StandartSize standartSize;

    public SizeDialog (StandartSizeService standartSizeService){
        this.setHeaderTitle("Новый стандартный размер");
        VerticalLayout dialogLayout = new VerticalLayout();

        text = new TextField();
        text.setLabel("Введите название");
        text.setValue("Например: А65");

        lengthField = new NumberField("Длина");
        lengthField.setValue(0d);

        widthField = new NumberField("Ширина");
        widthField.setValue(0d);

        dialogLayout.add(text, lengthField, widthField);
        this.add(dialogLayout);

        Button saveButton = new Button("Save", e -> {
            standartSize = new StandartSize();
            standartSize.setName(text.getValue());
            standartSize.setLength(lengthField.getValue());
            standartSize.setWidth(widthField.getValue());
            standartSizeService.save(standartSize);
            this.close();
        });

        Button cancelButton = new Button("Cancel", e -> {
            standartSize = null;
            this.close();
        });

        this.getFooter().add(cancelButton);
        this.getFooter().add(saveButton);
    }
    public void setX(Double x){
        lengthField.setValue(x);
    }

    public void setY(Double y){
        widthField.setValue(y);
    }

    public void setName(String name){
        text.setValue(name);
    }

}
