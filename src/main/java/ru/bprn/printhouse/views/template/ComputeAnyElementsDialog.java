package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import lombok.Getter;
import lombok.Setter;
import ru.bprn.printhouse.data.entity.WorkFlow;
import ru.bprn.printhouse.data.service.JSONToObjectsHelper;

public class ComputeAnyElementsDialog extends Dialog {
    private int tirage = 1;
    private final TextArea textArea = new TextArea();
    private int sizeX = 3000;
    private int sizeY = 3000;

    @Getter
    @Setter
    private WorkFlow workFlow;

    public ComputeAnyElementsDialog(){
        super("Расчет переменных величин");
        setHeight("75%");
        setWidth("75%");
        textArea.setSizeFull();
        add(tirage());
        add(textArea);
    }

    private IntegerField tirage() {
        var integerField = new IntegerField("Введите тираж");

        integerField.setMin(1);
        integerField.setMax(500);
        integerField.setValue(1);

        integerField.addValueChangeListener(e->{
            this.calc();
            tirage = e.getValue();
            textArea.setValue(setText());
        });
        return integerField;
    }

    private String setText() {
        var str = new StringBuilder();
        str.append("Размер печатного листа: ").append(sizeX).append("x").append(sizeY);
        return str.toString();
    }

    private void calc() {
        var list = JSONToObjectsHelper.getListOfObjReqType(workFlow.getStrJSON(), IsEquipment.class);
        if (list.isEmpty()) Notification.show("Empty!!!");
        for (IsEquipment equipment : list) {
            if (equipment.getFullSizeX()<sizeX) sizeX = equipment.getFullSizeX();
            if (equipment.getFullSizeY()<sizeY) sizeY = equipment.getFullSizeY();
        }

    }


}
