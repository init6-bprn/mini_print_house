package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import ru.bprn.printhouse.views.operation.entity.Operation;

import java.util.function.Consumer;

public class OperationEditor extends AbstractEditor<Operation> {

    public OperationEditor(Operation operation, Consumer<Object> onSave) {
        super(onSave);

    }

    @Override
    protected Component buildForm() {
        return new FormLayout();
    }
}
