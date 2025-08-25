package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import ru.bprn.printhouse.data.entity.Formulas;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.views.operation.entity.TypeOfOperation;
import ru.bprn.printhouse.views.operation.service.TypeOfOperationService;

import java.util.List;
import java.util.stream.Collectors;

public class FormulaDialog extends Dialog {

        private final Grid<Formulas> grid = new Grid<>(Formulas.class, false);
        private final TextField nameFilter = new TextField("Поиск по названию");
        private final Select<TypeOfOperation> typeFilter = new Select<>();

        private final FormulasService formulasService;
        private final TypeOfOperationService worksService;

    private Formulas selectedFormula;

        public FormulaDialog(FormulasService formulasService,
                             TypeOfOperationService worksService,
                             FormulaSelectionListener selectionListener) {
            this.formulasService = formulasService;
            this.worksService = worksService;

            setHeaderTitle("Выбор формулы");
            setWidth("800px");
            setHeight("600px");
            setModal(true);
            setDraggable(true);
            setResizable(true);

            configureFilters();
            configureGrid();

            Button selectButton = new Button("Выбрать", event -> {
                if (selectedFormula != null) {
                    selectionListener.onFormulaSelected(selectedFormula);
                    close();
                } else {
                    Notification.show("Сначала выберите формулу");
                }
            });

            Button cancelButton = new Button("Отмена", event -> close());
            HorizontalLayout buttons = new HorizontalLayout(selectButton, cancelButton);

            VerticalLayout layout = new VerticalLayout(
                    createFilterLayout(),
                    grid,
                    buttons
            );
            layout.setSizeFull();
            layout.setPadding(false);
            layout.setSpacing(true);
            layout.setAlignItems(FlexComponent.Alignment.STRETCH);

            add(layout);
            updateGrid();
        }

        private void configureFilters() {
            nameFilter.setPlaceholder("Введите название...");
            nameFilter.setClearButtonVisible(true);
            nameFilter.setWidth("300px");
            nameFilter.addValueChangeListener(e -> updateGrid());

            typeFilter.setItems(worksService.findAll());
            typeFilter.setItemLabelGenerator(TypeOfOperation::getName);
            typeFilter.setPlaceholder("Тип работы");
            typeFilter.setWidth("300px");
            typeFilter.addValueChangeListener(e -> updateGrid());
        }

        private HorizontalLayout createFilterLayout() {
            HorizontalLayout filters = new HorizontalLayout(nameFilter, typeFilter);
            filters.setAlignItems(FlexComponent.Alignment.END);
            return filters;
        }

        private void configureGrid() {
            grid.addColumn(Formulas::getName).setHeader("Название");
            grid.addColumn(Formulas::getFormula).setHeader("Формула");
            grid.setSelectionMode(Grid.SelectionMode.SINGLE);
            grid.setHeight("400px");

            grid.addSelectionListener(event -> event.getFirstSelectedItem().ifPresent(formula -> {
                selectedFormula = formula;
            }));
        }

        private void updateGrid() {
            String name = nameFilter.getValue().trim().toLowerCase();
            TypeOfOperation selectedType = typeFilter.getValue();

            List<Formulas> filtered = formulasService.findAll().stream()
                    .filter(f -> f.getName().toLowerCase().contains(name))
                    .filter(f -> selectedType == null || f.getTypeOfOperation().equals(selectedType))
                    .collect(Collectors.toList());

            grid.setItems(filtered);
        }
    }
