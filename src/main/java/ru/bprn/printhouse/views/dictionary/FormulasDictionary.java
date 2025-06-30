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
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import lombok.Getter;
import ru.bprn.printhouse.data.entity.*;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.views.additionalWorks.service.TypeOfWorksService;
import ru.bprn.printhouse.data.service.VariablesForMainWorksService;
import ru.bprn.printhouse.views.MainLayout;
import ru.bprn.printhouse.views.material.entity.Material;
import ru.bprn.printhouse.views.templates.CreateFormula;

import java.util.List;

@PageTitle("Словарь формул для расчета работ и материалов")
@Route(value = "formulas_dictionary", layout = MainLayout.class)
@AnonymousAllowed
public class FormulasDictionary extends VerticalLayout {
    private final TextField formulaField = new TextField("Фомула");
    private final BeanValidationBinder<Formulas> formulaBinder;
    private final StringBuilder strVariables = new StringBuilder();
    private List<VariablesForMainWorks> list;
    private final FormulasService formulasService;
    private final VariablesForMainWorksService variables;
    private CreateFormula formLayout;
    private final Grid<Formulas> grid = new Grid<>(Formulas.class, false);
    private final TextField filterField = new TextField();

    @Getter
    private Formulas formulaBean = new Formulas();
    
    public FormulasDictionary(FormulasService formulasService, VariablesForMainWorksService variables, TypeOfWorksService worksService){
        this.formulasService = formulasService;
        this.variables = variables;

        formLayout = new CreateFormula(formulasService, variables, worksService);
        var split = new SplitLayout(addGrid(), formLayout, SplitLayout.Orientation.HORIZONTAL);
        split.setSizeFull();
        split.setSplitterPosition(40.0);
        this.add(split);

        this.setSizeFull();

        formulaBinder = new BeanValidationBinder<>(Formulas.class);
        formulaBinder.setBean(formulaBean);


        //addDialog();
        addComponents();
    }

    private Component addGrid() {
        filterField.setWidth("50%");
        filterField.setPlaceholder("Поиск");
        filterField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        filterField.setValueChangeMode(ValueChangeMode.LAZY);
        filterField.addValueChangeListener(e -> populate(e.getValue().trim()));
        filterField.setClearButtonVisible(true);

        grid.addColumn(Formulas::getName).setHeader("Название");
        grid.addColumn(Formulas::getTypeOfWorks).setHeader("Тип работы");
        grid.setItems(formulasService.findAll());
        grid.setHeight("50%");
        grid.addItemClickListener(formulasEvent -> {
           formLayout.setFormulaBean(formulasEvent.getItem());
        });
        return new VerticalLayout(buttons(), filterField, grid);
    }

    private void addComponents(){

        var selector = new Select<String>("Переменные для:", selectStringComponentValueChangeEvent -> {
            String s = selectStringComponentValueChangeEvent.getValue();
            String clazz = "";
            switch (s) {
                case "Однолистовая цифровая печать": clazz = DigitalPrinting.class.getSimpleName();
                    break;
                case "Материал для цифровой печати": clazz = Material.class.getSimpleName();
                    break;
            }
            list.addAll(variables.findAllClazz(clazz));

        });
    }

    private HorizontalLayout buttons() {
        var dialogChain = new ConfirmDialog("Внимание!" , "", "Да",
                confirmEvent -> {
                    deleteElement();
                    Notification.show("Элемент удален!");
                },
                "Нет", cancelEvent -> cancelEvent.getSource().close());

        var hl = new HorizontalLayout();

        var createTemplateButton = new Button(VaadinIcon.PLUS.create(), event -> {
            formulaBinder.setBean(new Formulas());
            formulaBinder.refreshFields();
        });
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
        formulasService.delete(formulaBinder.getBean());
        formulaBinder.removeBean();
        formulaBinder.refreshFields();
        populate(filterField.getValue().trim());
    }

    public void populate(String str) {
        grid.setItems(formulasService.populate(str));

    }
}
