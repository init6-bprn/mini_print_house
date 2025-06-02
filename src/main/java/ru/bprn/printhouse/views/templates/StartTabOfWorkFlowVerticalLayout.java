package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import com.vaadin.flow.spring.annotation.UIScope;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import ru.bprn.printhouse.data.entity.WorkFlow;

@UIScope
@AnonymousAllowed
public class StartTabOfWorkFlowVerticalLayout extends VerticalLayout{

    @Getter
    private final BeanValidationBinder<WorkFlow> templateBinder;

    @Getter
    private final Checkbox autoNamed = new Checkbox("Задать имя автоматически");

    @Autowired
    public StartTabOfWorkFlowVerticalLayout(){

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

}
