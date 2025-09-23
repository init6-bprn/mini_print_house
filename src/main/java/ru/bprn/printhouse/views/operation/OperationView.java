package ru.bprn.printhouse.views.operation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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
import ru.bprn.printhouse.views.MainLayout;
import ru.bprn.printhouse.views.machine.service.AbstractMachineService;
import ru.bprn.printhouse.views.material.service.AbstractMaterialService;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.operation.service.OperationService;
import ru.bprn.printhouse.views.operation.service.OperationVariableService;
import ru.bprn.printhouse.views.operation.service.TypeOfOperationService;
import ru.bprn.printhouse.views.templates.OperationEditor;
import ru.bprn.printhouse.views.templates.service.ProductTypeVariableService;
import ru.bprn.printhouse.views.templates.service.FormulaValidationService;
import ru.bprn.printhouse.views.templates.SelectAbstractMaterialsDialog;

@PageTitle("Создание дополнительных работ")
@Route(value = "additional_works", layout = MainLayout.class)
@AnonymousAllowed
public class OperationView extends SplitLayout {
    private final TextField filterField = new TextField();
    private final Grid<Operation> grid = new Grid<>(Operation.class, false);
    private final OperationService service;
    private final TypeOfOperationService typeOfOperationService;
    private final AbstractMaterialService materialService;
    private final SelectAbstractMaterialsDialog materialDialog;
    private final FormulaValidationService formulaValidationService;
    private final ProductTypeVariableService productTypeVariableService;
    private final AbstractMachineService abstractMachineService;
    private final OperationVariableService operationVariableService;
    private OperationEditor operationEditor;

    public OperationView(OperationService service,
                         ru.bprn.printhouse.data.service.FormulasService formulasService,
                         TypeOfOperationService typeOfOperationService,
                         AbstractMaterialService materialService,
                         FormulaValidationService formulaValidationService,
                         ProductTypeVariableService productTypeVariableService,
                         AbstractMachineService abstractMachineService) {
        this.service = service;
        this.typeOfOperationService = typeOfOperationService;
        this.materialService = materialService;
        this.operationVariableService = new OperationVariableService(); // Можно внедрить через @Autowired
        materialDialog = new SelectAbstractMaterialsDialog("Выберите материалы", materialService);
        this.formulaValidationService = formulaValidationService;
        this.productTypeVariableService = productTypeVariableService;
        this.abstractMachineService = abstractMachineService;
        this.setSizeFull();
        this.addToPrimary(addGrid());
        this.addToSecondary(operationEditor = new OperationEditor(null, this::save, typeOfOperationService, materialService,
                formulasService, formulaValidationService, productTypeVariableService, abstractMachineService, service));
        operationEditor.setEnabled(false);
        this.setOrientation(SplitLayout.Orientation.HORIZONTAL);
        this.setSizeFull();
        this.setSplitterPosition(50.0);
    }

    private Component addGrid() {
        filterField.setWidth("50%");
        filterField.setPlaceholder("Поиск");
        filterField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        filterField.setValueChangeMode(ValueChangeMode.LAZY);
        filterField.addValueChangeListener(e -> populate(e.getValue().trim()));
        filterField.setClearButtonVisible(true);

        grid.addColumn(Operation::getName,"Название" );
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        populate(null);

        grid.addItemClickListener(e->{
            if (e.getItem() != null) {
                operationEditor.setEnabled(true);
                operationEditor.editor(e.getItem());
            }
            else operationEditor.setEnabled(false);
        });

        materialDialog.getGrid().setItems(materialService.findAll());

        return  new VerticalLayout(buttons(), filterField, grid);
    }

    public void populate(String str) {
        grid.setItems(service.populate(str));
    }

    private void save(Object object){
        Operation note;
        if (object instanceof Operation) {
            note = service.save((Operation) object);
            Notification.show(note.getName()+" сохранено");
            populate(null);
            //cancel();
        }

        else Notification.show("Заполните требуемые поля!");
    }
/*
    private void cancel(){
        bean.removeBean();
        bean.refreshFields();
    }

 */

    private HorizontalLayout buttons() {
        var dialogChain = new ConfirmDialog("Внимание!" , "", "Да",
                confirmEvent -> {
                    deleteElement();
                    Notification.show("Элемент удален!");
                },
                "Нет", cancelEvent -> cancelEvent.getSource().close());

        var hl = new HorizontalLayout();

        var createTemplateButton = new Button(VaadinIcon.PLUS.create(), event -> {
            operationEditor.setEnabled(true);
            Operation newOperation = new Operation();
            newOperation.initializeVariables(operationVariableService);
            operationEditor.editor(newOperation);
        });
        createTemplateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createTemplateButton.setTooltipText("Создать новую работу");

        var duplicateButton = new Button(VaadinIcon.COPY_O.create(), event -> {
                service.duplicate(grid.asSingleSelect().getValue());
                populate(filterField.getValue().trim());
        });
        duplicateButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        duplicateButton.setTooltipText("Создать дубликат работы");

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
        service.delete(grid.asSingleSelect().getValue());
        populate(filterField.getValue().trim());
    }

}
