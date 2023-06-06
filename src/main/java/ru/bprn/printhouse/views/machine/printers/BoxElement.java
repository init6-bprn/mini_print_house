package ru.bprn.printhouse.views.machine.printers;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.BoxSizing;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class BoxElement<T> extends VerticalLayout{

    @Autowired
    ApplicationContext applicationContext;

    public BoxElement (Class<T> domainType){
        this.setBoxSizing(BoxSizing.BORDER_BOX);
        this.setMaxWidth("100px");
        //this.addElement(new TextField(domainType));

        T bean = applicationContext.getBean(domainType);

    }

    public void addElement (Component comp){

    }

}
