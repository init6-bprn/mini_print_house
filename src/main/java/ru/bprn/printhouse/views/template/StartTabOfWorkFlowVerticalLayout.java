package ru.bprn.printhouse.views.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import ru.bprn.printhouse.data.entity.Gap;
import ru.bprn.printhouse.data.entity.StandartSize;
import ru.bprn.printhouse.data.entity.WorkFlow;
import ru.bprn.printhouse.data.service.*;

import java.util.Objects;

@UIScope
@AnonymousAllowed
public class StartTabOfWorkFlowVerticalLayout extends VerticalLayout implements HasBinder, Price{

    @Getter
    private final BeanValidationBinder<WorkFlow> templateBinder;

    @Getter
    private final Checkbox autoNamed = new Checkbox("Задать имя автоматически");

    @Autowired
    public StartTabOfWorkFlowVerticalLayout(StandartSizeService standartSizeService, TypeOfMaterialService TypeOfMaterialService,
                                            MaterialService materialService, GapService gapService,
                                            ImposeCaseService imposeCaseService){

        templateBinder = new BeanValidationBinder<>(WorkFlow.class);
        setSizeUndefined();

        addNameOfTemplate();

        addDescription();
    }

    private void addDescription(){
        var description = new TextField("Добавьте описание продукта:");
        description.setSizeFull();
        description.setValueChangeMode(ValueChangeMode.ON_BLUR);
        templateBinder.bind(description, WorkFlow::getDescription, WorkFlow::setDescription);
        this.add(description);
    }

    private  void addNameOfTemplate() {
        var nameOfTemplate = new TextField("Название шаблона: ");

        nameOfTemplate.setReadOnly(true);
        autoNamed.setValue(true);
        autoNamed.addValueChangeListener(e->{
            if (e.getValue()) {
                nameOfTemplate.setReadOnly(true);
                setDefaultName();
                templateBinder.refreshFields();
            }
            else nameOfTemplate.setReadOnly(false);
        });
        nameOfTemplate.setSizeFull();
        nameOfTemplate.setValueChangeMode(ValueChangeMode.EAGER);
        templateBinder.bind(nameOfTemplate, WorkFlow::getName, WorkFlow::setName);
        this.add(nameOfTemplate, autoNamed);
    }

    private String setDefaultName() {
        var bean = templateBinder.getBean();
        var stringName = new StringBuilder();

        return  stringName.toString();
    }

    @Override
    public Boolean isValid() {
        templateBinder.validate();
        return templateBinder.isValid();
    }

    @Override
    public String[] getBeanAsString(){
        return JSONToObjectsHelper.getBeanAsJSONStr(templateBinder.getBean());
    }

    @Override
    public double getPriceOfOperation() {
        return 1d;
    }

    @Override
    public double getPriceOfWork() {
        return 0;
    }

    @Override
    public double getPriceOfAmmo() {
        return 0;
    }

    @Override
    public int getTimeOfOperationPerSec() {
        return 0;
    }

    @Override
    public String getFormula() {
        return "price*leaves";
    }
}
