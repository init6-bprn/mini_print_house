package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import ru.bprn.printhouse.data.entity.AdditionalWorksBean;
import ru.bprn.printhouse.data.service.AdditionalWorksBeanService;

public class AdditionalWorksLayout extends VerticalLayout {

    public AdditionalWorksLayout(AdditionalWorksBean worksBean, AdditionalWorksBeanService service){
        super();
        this.setSizeFull();

        if (worksBean.isHaveAction()) {
            TextArea actionTextArea = new TextArea();
            actionTextArea.setWidthFull();
            actionTextArea.setLabel(worksBean.getActionFormula().getDescription());
            actionTextArea.setMaxRows(3);
            actionTextArea.setValue(worksBean.getActionFormula().getFormula());
            this.add(actionTextArea);
        }

        if (worksBean.isHaveMaterial()) {
            TextArea materialTextArea = new TextArea();
            materialTextArea.setWidthFull();
            materialTextArea.setLabel(worksBean.getActionFormula().getDescription());
            materialTextArea.setMaxRows(3);
            materialTextArea.setValue(worksBean.getActionFormula().getFormula());
            this.add(materialTextArea);
        }

    }
}
