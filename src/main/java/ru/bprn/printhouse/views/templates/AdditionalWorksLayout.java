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
            TextArea textArea = new TextArea();
            textArea.setWidthFull();
            textArea.setLabel(worksBean.getActionFormula().getDescription());
            textArea.setMaxRows(3);
            textArea.setValue(worksBean.getActionFormula().getFormula());
            this.add(textArea);
        }

        if (worksBean.isHaveMaterial()) {
            //worksBean.getListOfMaterials();
        }

    }
}
