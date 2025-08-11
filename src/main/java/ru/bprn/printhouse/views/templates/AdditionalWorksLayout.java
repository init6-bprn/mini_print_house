package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import lombok.Getter;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.operation.service.OperationService;
import ru.bprn.printhouse.data.service.JSONToObjectsHelper;

public class AdditionalWorksLayout extends VerticalLayout implements HasBinder{

    @Getter
    private final BeanValidationBinder<Operation> templateBinder;

    public AdditionalWorksLayout(Operation worksBean, OperationService service){
        super();
        this.setSizeFull();
        templateBinder = new BeanValidationBinder<>(Operation.class);
        templateBinder.setBean(worksBean);

        if (worksBean.isHaveAction()) {
            TextArea actionTextArea = new TextArea();
            actionTextArea.setEnabled(false);
            actionTextArea.setWidthFull();
            actionTextArea.setLabel(worksBean.getDescription());
            actionTextArea.setMaxRows(3);
            actionTextArea.setValue(worksBean.getActionFormula());
            this.add(actionTextArea);
            //templateBinder.forField(actionTextArea).asRequired().bind(Operation::getActionFormula, Operation::setActionFormula);
        }

        if (worksBean.isHaveMaterial()) {
            TextArea materialTextArea = new TextArea();
            materialTextArea.setEnabled(false);
            materialTextArea.setWidthFull();
            materialTextArea.setLabel(worksBean.getDescription());
            materialTextArea.setMaxRows(3);
            materialTextArea.setValue(worksBean.getActionFormula());

            Select<AbstractMaterials> materialSelect = new Select<>();
            materialSelect.setItems(templateBinder.getBean().getListOfMaterials());
            materialSelect.setValue(templateBinder.getBean().getDefaultMaterial());
            templateBinder.forField(materialSelect).bind(Operation::getDefaultMaterial, Operation::setDefaultMaterial);
            this.add(materialTextArea, materialSelect);
        }

    }

    @Override
    public Boolean isValid() {
        return templateBinder.isValid();
    }

    @Override
    public String[] getBeanAsString() {
        return JSONToObjectsHelper.getBeanAsJSONStr(templateBinder.getBean());
    }

    @Override
    public String getDescription() {
        return " " + templateBinder.getBean().getActionName() + ",";
    }
}
