package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.binder.Binder;

import java.util.function.Consumer;

public abstract class AbstractEditor<T> extends VerticalLayout {

    protected final Binder<T> binder = new Binder<>();
    protected T currentEntity;
    protected final Consumer<Object> onSave;

    private final Button saveButton = new Button("Сохранить");
    private final Button cancelButton = new Button("Отмена");

    public AbstractEditor(Consumer<Object> onSave) {
        this.onSave = onSave;
        setSpacing(true);
        setPadding(true);
        setSizeFull();

        saveButton.addClickListener(e -> save());
        cancelButton.addClickListener(e -> clear());
    }

    protected abstract Component buildForm(); // реализуется в наследниках

    public void edit(T entity) {
        this.currentEntity = entity;
        binder.setBean(entity);
    }

    public void addButtons(){
        HorizontalLayout buttons = new HorizontalLayout(saveButton, cancelButton);
        buttons.setWidthFull();
        add(buttons);
    }


    private void save() {
        if (binder.validate().isOk()) {
            onSave.accept(currentEntity);
            Notification.show("Сохранено", 3000, Notification.Position.TOP_CENTER);
            clear();
        }
    }

    private void clear() {
        binder.setBean(null);
        currentEntity = null;
    }
}
