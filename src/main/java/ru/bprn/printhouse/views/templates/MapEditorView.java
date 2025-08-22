package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.validator.DoubleRangeValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import lombok.Getter;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MapEditorView extends VerticalLayout {

        private final Grid<Variable> grid = new Grid<>(Variable.class, false);
        @Getter
        private List<Variable> variables = new ArrayList<>();
        private final ListDataProvider<Variable> dataProvider;
        private final Consumer<List<Variable>> changeCallback;

        public MapEditorView(List<Variable> variables, Consumer<List<Variable>> changeCallback) {
            this.changeCallback = changeCallback;
            setSizeFull();
            setPadding(false);

            // Инициализация данных
            this.variables = variables;
            dataProvider = new ListDataProvider<>(this.variables);

            configureGrid();
            add(createToolbar(), grid);
        }

        private Component createToolbar() {
            Button addButton = new Button("Добавить переменную", new Icon(VaadinIcon.PLUS),
                    e -> addNewVariable());
            addButton.setThemeName("primary");

            return new HorizontalLayout(addButton);
        }

        private void configureGrid() {
            grid.setSizeFull();
            grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
            grid.setDataProvider(dataProvider);

            // Колонка с ключом
            grid.addColumn(createKeyRenderer())
                    .setHeader("Ключ")
                    .setAutoWidth(true)
                    .setFlexGrow(1);

            // Колонка со значением
            grid.addColumn(createValueRenderer())
                    .setHeader("Значение")
                    .setAutoWidth(true)
                    .setFlexGrow(0);

            // Колонка с описанием
            grid.addColumn(createDescriptionRenderer())
                    .setHeader("Описание")
                    .setAutoWidth(true)
                    .setFlexGrow(3);

            // Контекстное меню
            GridContextMenu<Variable> contextMenu = grid.addContextMenu();
            contextMenu.addItem("Добавить переменную", e -> addNewVariable());
            contextMenu.addItem("Удалить переменную", e -> {
                if (e.getItem().isPresent()) {
                    deleteVariable(e.getItem().get());
                }
            });

            // Двойной клик для редактирования
            grid.addItemDoubleClickListener(e -> grid.getEditor().editItem(e.getItem()));
            //grid.getColumns().forEach(col -> col.setAutoWidth(true));
            grid.setHeight("250px");
        }

        private ComponentRenderer<Component, Variable> createKeyRenderer() {
            return new ComponentRenderer<>(variable -> {
                TextField keyField = new TextField();
                keyField.setValue(variable.getKey());
                keyField.setWidthFull();

                Binder<Variable> binder = new Binder<>(Variable.class);
                binder.forField(keyField)
                        .withValidator(new StringLengthValidator(
                                "Название не может быть пустым", 1, null))
                        .bind(Variable::getKey, Variable::setKey);

                keyField.addBlurListener(e -> {
                    try {
                        binder.writeBean(variable);
                        dataProvider.refreshItem(variable);
                    } catch (ValidationException ex) {
                        Notification.show("Ошибка валидации: " + ex.getMessage(), 3000,
                                Notification.Position.MIDDLE);
                        keyField.setValue(variable.getKey());
                    }
                });

                return keyField;
            });
        }

        private ComponentRenderer<Component, Variable> createValueRenderer() {
            return new ComponentRenderer<>(variable -> {
                TextField valueField = new TextField();
                valueField.setValue(variable.getValue().toString());
                valueField.setWidthFull();

                Binder<Variable> binder = new Binder<>(Variable.class);
                binder.forField(valueField)
                        .withConverter(
                                Double::valueOf,
                                String::valueOf,
                                "Введите корректное число"
                        )
                        .withValidator(new DoubleRangeValidator(
                                "Значение должно быть числом", null, null))
                        .bind(Variable::getValue, Variable::setValue);

                valueField.addBlurListener(e -> {
                    try {
                        binder.writeBean(variable);
                        dataProvider.refreshItem(variable);
                    } catch (ValidationException ex) {
                        Notification.show("Ошибка валидации: " + ex.getMessage(), 3000,
                                Notification.Position.MIDDLE);
                        valueField.setValue(variable.getValue().toString());
                    }
                });

                return valueField;
            });
        }

        private ComponentRenderer<Component, Variable> createDescriptionRenderer() {
            return new ComponentRenderer<>(variable -> {
                TextField descriptionField = new TextField();
                descriptionField.setValue(variable.getDescription());
                descriptionField.setWidthFull();

                Binder<Variable> binder = new Binder<>(Variable.class);
                binder.forField(descriptionField)
                        .bind(Variable::getDescription, Variable::setDescription);

                descriptionField.addBlurListener(e -> {
                    try {
                        binder.writeBean(variable);
                        dataProvider.refreshItem(variable);
                    } catch (ValidationException ex) {
                        Notification.show("Ошибка валидации: " + ex.getMessage(), 3000,
                                Notification.Position.MIDDLE);
                        descriptionField.setValue(variable.getDescription());
                    }
                });

                return descriptionField;
            });
        }

        private void addNewVariable() {
            Variable newVariable = new Variable("новая_переменная", 0.0, "Новое описание");
            variables.add(newVariable);
            dataProvider.getItems().add(newVariable);
            dataProvider.refreshAll();
            notifyParent();
            Notification.show("Переменная добавлена", 2000, Notification.Position.BOTTOM_END);
        }

        private void deleteVariable(Variable variable) {
            variables.remove(variable);
            dataProvider.getItems().remove(variable);
            dataProvider.refreshAll();
            notifyParent();
            Notification.show("Переменная удалена", 2000, Notification.Position.BOTTOM_END);
        }

        public void setVariables (List<Variable> list) {
            this.variables = list;
            dataProvider.getItems().clear();
            dataProvider.getItems().addAll(list);
            //grid.setItems(variables);
        }

    private void notifyParent() {
        if (changeCallback != null) {
            changeCallback.accept(new ArrayList<>(variables));
        }
    }

    }

