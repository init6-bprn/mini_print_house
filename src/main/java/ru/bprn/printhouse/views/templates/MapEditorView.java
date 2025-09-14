package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.validator.StringLengthValidator;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class MapEditorView extends VerticalLayout {

        private final Grid<Variable> grid = new Grid<>(Variable.class, false);
        private final ListDataProvider<Variable> dataProvider;
        private final Consumer<List<Variable>> changeCallback;

        public MapEditorView(List<Variable> variables, Consumer<List<Variable>> changeCallback) {
            this.changeCallback = changeCallback;
            setSizeFull();
            setPadding(false);

            // DataProvider инициализируется копией переданного списка.
            // Это будет наш единственный источник данных для Grid.
            dataProvider = new ListDataProvider<>(variables != null ? new ArrayList<>(variables) : new ArrayList<>());

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

            // Колонка с чекбоксом "Показывать"
            grid.addColumn(createShowRenderer())
                    .setHeader("Пок.")
                    .setTooltipGenerator(variable -> "Показывать для редактирования пользователю")
                    .setAutoWidth(true)
                    .setFlexGrow(0);
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

            // Колонка с типом
            grid.addColumn(createTypeRenderer())
                    .setHeader("Тип")
                    .setAutoWidth(true)
                    .setFlexGrow(1);

            // Колонка с описанием
            grid.addColumn(createDescriptionRenderer())
                    .setHeader("Описание")
                    .setAutoWidth(true)
                    .setFlexGrow(3);

            // Колонка с настройками
            grid.addColumn(createSettingsRenderer())
                    .setHeader("Настр.")
                    .setTooltipGenerator(variable -> "Настроить ограничения для переменной")
                    .setAutoWidth(true)
                    .setFlexGrow(0);

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
                        notifyParent();
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
                Binder<Variable> binder = new Binder<>(Variable.class);

                // Для boolean используем Checkbox, для остальных - TextField с конвертером
                if (variable.getType() == Variable.VariableType.BOOLEAN) {
                    Checkbox checkbox = new Checkbox();
                    binder.forField(checkbox)
                            .bind(v -> (Boolean) v.getValueAsObject(), (v, val) -> v.setValue(val));
                    binder.setBean(variable);
                    checkbox.addValueChangeListener(e -> {
                        try {
                            binder.writeBean(variable);
                            dataProvider.refreshItem(variable);
                            notifyParent();
                        } catch (ValidationException ignored) {}
                    });
                    return checkbox;
                } else {
                    TextField valueField = new TextField();
                    valueField.setValue(variable.getValue() != null ? variable.getValue() : "");
                    valueField.setWidthFull();

                    var binding = binder.forField(valueField);

                    switch (variable.getType()) {
                        case INTEGER:
                            var intBinding = binding.withConverter(
                                    s -> (s == null || s.isBlank()) ? 0 : Integer.parseInt(s),
                                    i -> i == null ? "" : String.valueOf(i),
                                    "Введите целое число"
                            );
                            if (variable.getMinValue() != null) {
                                intBinding.withValidator(v -> v >= Integer.parseInt(variable.getMinValue()), "Значение меньше минимального");
                            }
                            if (variable.getMaxValue() != null) {
                                intBinding.withValidator(v -> v <= Integer.parseInt(variable.getMaxValue()), "Значение больше максимального");
                            }
                            intBinding.bind(v -> (Integer) v.getValueAsObject(), (v, val) -> v.setValue(val));
                            break;
                        case DOUBLE:
                            var doubleBinding = binding.withConverter(
                                    s -> (s == null || s.isBlank()) ? 0.0 : Double.parseDouble(s.replace(",", ".")),
                                    d -> d == null ? "" : String.valueOf(d),
                                    "Введите число"
                            );
                            if (variable.getMinValue() != null) {
                                doubleBinding.withValidator(v -> v >= Double.parseDouble(variable.getMinValue()), "Значение меньше минимального");
                            }
                            if (variable.getMaxValue() != null) {
                                doubleBinding.withValidator(v -> v <= Double.parseDouble(variable.getMaxValue()), "Значение больше максимального");
                            }
                            doubleBinding.bind(v -> (Double) v.getValueAsObject(), (v, val) -> v.setValue(val));
                            break;
                        case STRING:
                        default:
                            binding.withConverter(
                                    s -> s, // No conversion needed for string
                                    s -> s
                            );
                            binding.withValidator(new StringLengthValidator(
                                    "Длина строки не соответствует ограничениям",
                                    tryParseInt(variable.getMinValue()).orElse(null),
                                    tryParseInt(variable.getMaxValue()).orElse(null)
                            ));
                            binding.bind(Variable::getValue, Variable::setValue);
                            break;
                    }
                    binder.setBean(variable);

                    valueField.addBlurListener(e -> {
                        try {
                            binder.writeBean(variable);
                            dataProvider.refreshItem(variable);
                            notifyParent();
                        } catch (ValidationException ex) {
                            Notification.show(ex.getValidationErrors().get(0).getErrorMessage(), 3000, Notification.Position.MIDDLE);
                            // Возвращаем старое значение в поле
                            binder.readBean(variable);
                        }
                    });
                    return valueField;
                }
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
                        notifyParent();
                    } catch (ValidationException ex) {
                        Notification.show("Ошибка валидации: " + ex.getMessage(), 3000,
                                Notification.Position.MIDDLE);
                        descriptionField.setValue(variable.getDescription());
                    }
                });

                return descriptionField;
            });
        }

        private ComponentRenderer<Component, Variable> createShowRenderer() {
            return new ComponentRenderer<>(variable -> {
                Checkbox showCheckbox = new Checkbox();
                showCheckbox.setValue(variable.isShow());
                showCheckbox.addValueChangeListener(event -> {
                    if (grid.getEditor().isOpen()) {
                        grid.getEditor().cancel(); // Закрываем редактор, если он был открыт
                    }
                    variable.setShow(event.getValue());
                    notifyParent();
                });
                return showCheckbox;
            });
        }

        private ComponentRenderer<Component, Variable> createSettingsRenderer() {
            return new ComponentRenderer<>(variable -> {
                Button settingsButton = new Button(new Icon(VaadinIcon.COG));
                settingsButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
                settingsButton.addClickListener(e -> openSettingsDialog(variable));
                return settingsButton;
            });
        }

        private void openSettingsDialog(Variable variable) {
            Dialog dialog = new Dialog();
            dialog.setHeaderTitle("Настройки для: " + variable.getKey());

            FormLayout formLayout = new FormLayout();
            formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

            switch (variable.getType()) {
                case INTEGER:
                case DOUBLE:
                    NumberField minField = new NumberField("Минимум");
                    tryParseDouble(variable.getMinValue()).ifPresent(minField::setValue);

                    NumberField maxField = new NumberField("Максимум");
                    tryParseDouble(variable.getMaxValue()).ifPresent(maxField::setValue);

                    NumberField stepField = new NumberField("Шаг");
                    tryParseDouble(variable.getStep()).ifPresent(stepField::setValue);

                    dialog.addOpenedChangeListener(event -> {
                        if (!event.isOpened()) { // on close
                            variable.setMinValue(minField.getValue() != null ? String.valueOf(minField.getValue()) : null);
                            variable.setMaxValue(maxField.getValue() != null ? String.valueOf(maxField.getValue()) : null);
                            variable.setStep(stepField.getValue() != null ? String.valueOf(stepField.getValue()) : null);
                            dataProvider.refreshItem(variable);
                            notifyParent();
                        }
                    });

                    formLayout.add(minField, maxField, stepField);
                    break;
                case STRING:
                    IntegerField minLengthField = new IntegerField("Мин. длина");
                    tryParseInt(variable.getMinValue()).ifPresent(minLengthField::setValue);

                    IntegerField maxLengthField = new IntegerField("Макс. длина");
                    tryParseInt(variable.getMaxValue()).ifPresent(maxLengthField::setValue);

                    TextField patternField = new TextField("Шаблон (regex)");
                    patternField.setValue(variable.getPattern() != null ? variable.getPattern() : "");

                    dialog.addOpenedChangeListener(event -> {
                        if (!event.isOpened()) { // on close
                            variable.setMinValue(minLengthField.getValue() != null ? String.valueOf(minLengthField.getValue()) : null);
                            variable.setMaxValue(maxLengthField.getValue() != null ? String.valueOf(maxLengthField.getValue()) : null);
                            variable.setPattern(!patternField.getValue().isBlank() ? patternField.getValue() : null);
                            dataProvider.refreshItem(variable);
                            notifyParent();
                        }
                    });

                    formLayout.add(minLengthField, maxLengthField, patternField);
                    break;
                case BOOLEAN:
                    formLayout.add(new com.vaadin.flow.component.html.Span("Для типа 'Да/Нет' настройки не требуются."));
                    break;
            }

            dialog.add(formLayout);
            Button closeButton = new Button("Закрыть", e -> dialog.close());
            dialog.getFooter().add(closeButton);
            dialog.open();
        }

        private java.util.Optional<Integer> tryParseInt(String s) {
            if (s == null || s.isBlank()) return java.util.Optional.empty();
            try {
                return java.util.Optional.of(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                return java.util.Optional.empty();
            }
        }

        private java.util.Optional<Double> tryParseDouble(String s) {
            if (s == null || s.isBlank()) return java.util.Optional.empty();
            try {
                // Использование NumberFormat для учета локали (точка или запятая)
                NumberFormat format = NumberFormat.getInstance(getLocale());
                Number number = format.parse(s);
                return java.util.Optional.of(number.doubleValue());
            } catch (java.text.ParseException e) {
                // Попытка прямой замены запятой на точку как запасной вариант
                try {
                    return java.util.Optional.of(Double.parseDouble(s.replace(',', '.')));
                } catch (NumberFormatException ex) {
                    return java.util.Optional.empty();
                }
            }
        }


        private ComponentRenderer<Component, Variable> createTypeRenderer() {
            return new ComponentRenderer<>(variable -> {
                Select<Variable.VariableType> typeSelect = new Select<>();
                typeSelect.setItems(Variable.VariableType.values());
                typeSelect.setItemLabelGenerator(Variable.VariableType::getDescription);
                typeSelect.setValue(variable.getType());
                typeSelect.setWidthFull();

                typeSelect.addValueChangeListener(e -> {
                    variable.setType(e.getValue());
                    // При смене типа, сбрасываем значение на стандартное для нового типа
                    variable.setValue(null);
                    variable.getValueAsObject(); // Это установит дефолтное значение

                    dataProvider.refreshItem(variable);
                    notifyParent();
                });

                return typeSelect;
            });
        }

        private void addNewVariable() {
            Variable newVariable = new Variable("новая_переменная", "новое значение", "Новое описание", Variable.VariableType.STRING);
            dataProvider.getItems().add(newVariable); // Модифицируем внутренний список dataProvider
            dataProvider.refreshAll();
            notifyParent();
            Notification.show("Переменная добавлена", 2000, Notification.Position.BOTTOM_END);
        }

        private void deleteVariable(Variable variable) {
            dataProvider.getItems().remove(variable); // Модифицируем внутренний список dataProvider
            dataProvider.refreshAll();
            notifyParent();
            Notification.show("Переменная удалена", 2000, Notification.Position.BOTTOM_END);
        }

        public void setVariables(List<Variable> newVariables) {
            // 1. Получаем доступ к внутреннему списку DataProvider
            Collection<Variable> items = dataProvider.getItems();
            // 2. Очищаем его
            items.clear();
            // 3. Добавляем все новые элементы
            if (newVariables != null) items.addAll(newVariables);
            // 4. Уведомляем Grid, что данные полностью изменились.
            dataProvider.refreshAll();
        }

    private void notifyParent() {
        if (changeCallback != null) {
            changeCallback.accept(new ArrayList<>(dataProvider.getItems()));
        }
    }

    }
