package ru.bprn.printhouse.views.dictionary;


import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import lombok.Getter;
import ru.bprn.printhouse.data.entity.DigitalPrinting;
import ru.bprn.printhouse.data.entity.Formulas;
import ru.bprn.printhouse.data.entity.Material;
import ru.bprn.printhouse.data.entity.VariablesForMainWorks;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.data.service.VariablesForMainWorksService;
import ru.bprn.printhouse.views.MainLayout;
import ru.bprn.printhouse.views.template.VariablesRecord;

import java.util.List;

@PageTitle("Словарь формул для расчета работ и материалов")
@Route(value = "formulas_dictionary", layout = MainLayout.class)
@AnonymousAllowed
public class FormulasDictionary extends VerticalLayout {
    private final TextField formulaField = new TextField("Фомула");
    private final BeanValidationBinder<Formulas> formulaBinder;
    private final StringBuilder strVariables = new StringBuilder();
    private List<VariablesForMainWorks> list;
    private final FormulasService formulasService;
    private final VariablesForMainWorksService variables;

    @Getter
    private Formulas formulaBean = new Formulas();
    
    public FormulasDictionary(FormulasService formulasService, VariablesForMainWorksService variables){
        this.formulasService = formulasService;
        this.variables = variables;

        formulaBinder = new BeanValidationBinder<>(Formulas.class);
        formulaBinder.setBean(formulaBean);

        addComponents();
    }

    private void addComponents(){

        var selector = new Select<String>("Переменные для:", selectStringComponentValueChangeEvent -> {
            String s = selectStringComponentValueChangeEvent.getValue();
            String clazz = "";
            switch (s) {
                case "Однолистовая цифровая печать": clazz = DigitalPrinting.class.getSimpleName();
                    break;
                case "Материал для цифровой печати": clazz = Material.class.getSimpleName();
                    break;
            }
            list.addAll(variables.findAllClazz(clazz));

        });
    }
}
