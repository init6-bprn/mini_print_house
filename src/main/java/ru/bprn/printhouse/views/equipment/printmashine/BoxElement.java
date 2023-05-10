package ru.bprn.printhouse.views.equipment.printmashine;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.bprn.printhouse.data.AbstractEntity;

public class BoxElement<T> extends VerticalLayout{

    @Autowired
    private ApplicationContext applicationContext;

    public BoxElement (Class<T> domainType){
        this.setBoxSizing(BoxSizing.BORDER_BOX);
        this.setMaxWidth("100px");
        //this.addElement(new TextField(domainType));

        T bean = applicationContext.getBean(domainType);

    }

    public void addElement (Component comp){

    }

}
