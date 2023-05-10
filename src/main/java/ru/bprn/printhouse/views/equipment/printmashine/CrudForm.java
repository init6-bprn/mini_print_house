package ru.bprn.printhouse.views.equipment.printmashine;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.bprn.printhouse.data.AbstractEntity;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class CrudForm extends FormLayout {

    private final Button save = new Button("Save");
    private final Button delete = new Button("Delete");
    private final Button close = new Button("Cancel");
    private AbstractEntity entity;
    @Autowired
    private ApplicationContext ctx;

    public CrudForm(Class<? extends AbstractEntity> classe) {

        List<Field> fields = Arrays.stream(classe.getDeclaredFields()).toList();
        for (Field field: fields
        ) {
            if (field.getType().isAssignableFrom(String.class)) add(new TextField(field.getName()));
            if (field.getType().getName().contains("entity")) add(addComboBox(field));
        }

        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        add( new HorizontalLayout(save, delete, close));

    }

    public <T extends AbstractEntity> void setData (T ent) {
        this.entity = ent;
    }

    private ComboBox<AbstractEntity> addComboBox (Field field) {
        Object obj;
        ComboBox combo = new ComboBox<>(field.getName());

        if (ctx != null) obj = ctx.getBean(field.getDeclaringClass().toString() + "Service");
        else combo.setLabel("Context is null!");
        //combo.setItems(obj);
        return combo;
    }
}
