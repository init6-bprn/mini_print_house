package ru.bprn.printhouse.views.machine.printers;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import ru.bprn.printhouse.data.AbstractEntity;
import ru.bprn.printhouse.data.entity.PrintMashine;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class CrudForm extends FormLayout {

    private final Button save = new Button("Save");
    private final Button delete = new Button("Delete");
    private final Button close = new Button("Cancel");
    private PrintMashine entity;

    private BeanValidationBinder<PrintMashine> binder;

    public CrudForm(Class<PrintMashine> classe)  {

        binder = new BeanValidationBinder<>(PrintMashine.class);

        List<Field> fields = Arrays.stream(classe.getDeclaredFields()).toList();

            for (Field field: fields) {
                String caps = field.getName().substring(0,1).toUpperCase() + field.getName().substring(1);
                if (field.getType().isAssignableFrom(String.class)) {
                    var textField = new TextField(caps);
                    add(textField);
                    binder.bind(textField, PrintMashine::getName,
                        PrintMashine::setName);
                }
                if (field.getType().isAssignableFrom(Integer.class)) add(new IntegerField(field.getName()));
                if (field.getType().isAssignableFrom(Float.class)) add(new NumberField(field.getName()));
                if (field.getType().getName().contains("entity")) add(addComboBox(field.getName()));

            }
        binder.bindInstanceFields(this);

        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        close.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        add( new HorizontalLayout(save, delete, close));

    }

    public void setData (PrintMashine ent) {
        binder.readBean(ent);
    }

    private ComboBox<AbstractEntity> addComboBox (String name) {
        Object obj;
        ComboBox combo = new ComboBox(name);

        //if (ctx != null) obj = ctx.getBean(field.getDeclaringClass().toString() + "Service");
        //else combo.setLabel("Context is null!");
        //combo.setItems(obj);
        return combo;
    }
}
