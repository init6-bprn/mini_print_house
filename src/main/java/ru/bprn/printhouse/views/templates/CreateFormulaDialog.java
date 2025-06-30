package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.dialog.Dialog;
import ru.bprn.printhouse.data.entity.Formulas;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.views.additionalWorks.service.TypeOfWorksService;
import ru.bprn.printhouse.data.service.VariablesForMainWorksService;

public class CreateFormulaDialog extends Dialog {
    private final CreateFormula layout;

    public CreateFormulaDialog(FormulasService formulasService, VariablesForMainWorksService service, TypeOfWorksService worksService) {
        layout = new CreateFormula(formulasService, service, worksService);
        layout.setSizeFull();
        this.setHeight("80%");
        this.setWidth("80%");
        this.setHeaderTitle("Редактирование формулы");
        this.add(layout);
    }

    public void setFormulaBean(Formulas formula) {
        layout.setFormulaBean(formula);
    }
}
