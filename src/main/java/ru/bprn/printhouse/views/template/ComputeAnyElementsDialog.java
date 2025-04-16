package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import lombok.Getter;
import lombok.Setter;
import ru.bprn.printhouse.data.entity.WorkFlow;
import ru.bprn.printhouse.data.service.WorkFlowService;

public class ComputeAnyElementsDialog extends Dialog {
    private int tirage = 1;
    private final TextArea textArea = new TextArea();
    private final WorkFlowService workFlowService;

    @Getter
    @Setter
    private WorkFlow workFlow;

    public ComputeAnyElementsDialog(WorkFlowService workFlowService){
        super("Расчет переменных величин");
        this.workFlowService = workFlowService;
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
            ;
            if (e.getValue()!=null) {
                workFlow.setQuantityOfProduct(e.getValue());
                tirage = e.getValue();
                textArea.setValue(this.calc());
            }
        });
        return integerField;
    }

    private String calc() {
        var sb = new StringBuilder();
        workFlowService.calcWorkflowParameters(workFlow);
        sb.append("Размер печатного листа: ").append(workFlow.getPrintSizeX()).append("x").append(workFlow.getPrintSizeY()).append("\n");
        sb.append("Размер области печати: ").append(workFlow.getPrintAreaX()).append("x").append(workFlow.getPrintAreaY()).append("\n");
        sb.append("Печатных листов: ").append(workFlow.getQuantityOfPrintLeaves()).append("\n");
        sb.append("Изделий на листе: ").append(workFlow.getQuantityProductionsOnLeaf()).append("\n");

        return sb.toString();
    }


}
