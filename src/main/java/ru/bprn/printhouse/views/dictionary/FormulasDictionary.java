package ru.bprn.printhouse.views.dictionary;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ru.bprn.printhouse.data.entity.Formulas;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.views.MainLayout;
import ru.bprn.printhouse.views.operation.service.TypeOfOperationService;
import ru.bprn.printhouse.views.templates.service.FormulaValidationService;
import ru.bprn.printhouse.views.templates.service.ProductTypeVariableService;
import ru.bprn.printhouse.views.templates.service.TemplateVariableService;

@PageTitle("Словарь формул для расчета работ и материалов")
@Route(value = "formulas_dictionary", layout = MainLayout.class)
@AnonymousAllowed
public class FormulasDictionary extends VerticalLayout {
    private final FormulasService formulasService;
    private FormulasEditor editor;
    private final Grid<Formulas> grid = new Grid<>(Formulas.class, false);
    private final TextField filterField = new TextField();
    
    public FormulasDictionary(
            FormulasService formulasService,
            TypeOfOperationService worksService,
            FormulaValidationService formulaValidationService,
            ProductTypeVariableService productTypeVariableService,
            TemplateVariableService templateVariableService
    ) {
        this.formulasService = formulasService;

        editor = new FormulasEditor(
                this::saveFormula,
                worksService,
                formulaValidationService,
                productTypeVariableService,
                templateVariableService
        );
        editor.edit(null); // Изначально редактор пуст

        var split = new SplitLayout(createGridLayout(), editor, SplitLayout.Orientation.HORIZONTAL);
        split.setSizeFull();
        split.setSplitterPosition(40.0);
        this.add(split);

        this.setSizeFull();
    }

    private Component createGridLayout() {
        filterField.setWidth("50%");
        filterField.setPlaceholder("Поиск");
        filterField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        filterField.setValueChangeMode(ValueChangeMode.LAZY);
        filterField.addValueChangeListener(e -> populate(e.getValue().trim()));
        filterField.setClearButtonVisible(true);

        grid.addColumn(Formulas::getName).setHeader("Название");
        grid.addColumn(Formulas::getTypeOfOperation).setHeader("Тип работы");
        grid.setItems(formulasService.findAll());
        grid.setHeight("50%");
        grid.asSingleSelect().addValueChangeListener(event -> editor.edit(event.getValue()));

        return new VerticalLayout(buttons(), filterField, grid);
    }

    private HorizontalLayout buttons() {
        var dialogChain = new ConfirmDialog("Внимание!" , "", "Да",
                confirmEvent -> {
                    deleteElement();
                    Notification.show("Элемент удален!");
                },
                "Нет", cancelEvent -> cancelEvent.getSource().close());

        var hl = new HorizontalLayout();

        var createTemplateButton = new Button(VaadinIcon.PLUS.create(), event -> editor.edit(new Formulas()));
        createTemplateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createTemplateButton.setTooltipText("Создать новую формулу?");

        var duplicateButton = new Button(VaadinIcon.COPY_O.create(), event -> {
            formulasService.duplicate(grid.asSingleSelect().getValue());
            populate(filterField.getValue().trim());
        });
        duplicateButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        duplicateButton.setTooltipText("Создать дубликат формулы?");

        var deleteButton = new Button(VaadinIcon.CLOSE.create(), event -> {
            if (!grid.asSingleSelect().isEmpty()) {
                dialogChain.setText("Вы уверены, что хотите удалить " + grid.asSingleSelect().getValue().getName() + " ?");
                dialogChain.open();
            }
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.setTooltipText("Удалить элемент");

        hl.add(createTemplateButton, duplicateButton, deleteButton);
        return hl;
    }

    private void deleteElement() {
        formulasService.delete(grid.asSingleSelect().getValue());
        editor.edit(null);
        populate(filterField.getValue().trim());
    }

    public void populate(String str) {
        grid.setItems(formulasService.populate(str));
    }

    private void saveFormula(Object formulaObject) {
        Formulas formula = (Formulas) formulaObject;
        formulasService.save(formula);
        populate(filterField.getValue().trim());
        grid.select(formula);
        editor.edit(formula);
    }
}
