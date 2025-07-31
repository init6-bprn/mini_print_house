package ru.bprn.printhouse.views.operation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.data.entity.Formulas;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.operation.entity.TypeOfOperation;
import ru.bprn.printhouse.views.operation.service.OperationService;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.views.material.service.AbstractMaterialService;
import ru.bprn.printhouse.views.operation.service.TypeOfOperationService;
import ru.bprn.printhouse.views.MainLayout;
import ru.bprn.printhouse.views.templates.SelectAbstractMaterialsDialog;

@PageTitle("Создание дополнительных работ")
@Route(value = "additional_works", layout = MainLayout.class)
@AnonymousAllowed
public class OperationView extends VerticalLayout {
    private final TextField filterField = new TextField();
    private final Grid<Operation> grid = new Grid<>(Operation.class, false);
    private final OperationService service;
    private final FormulasService formulasService;
    private final TypeOfOperationService typeOfOperationService;
    private final AbstractMaterialService materialService;
    private final BeanValidationBinder<Operation> bean = new BeanValidationBinder<>(Operation.class);
    private final SelectAbstractMaterialsDialog materialDialog;
    private final Select<AbstractMaterials> materialSelect = new Select<>();

    public OperationView(OperationService service,
                         FormulasService formulasService,
                         TypeOfOperationService typeOfOperationService,
                         AbstractMaterialService materialService) {
        this.service = service;
        this.formulasService = formulasService;
        this.typeOfOperationService = typeOfOperationService;
        this.materialService = materialService;
        materialDialog = new SelectAbstractMaterialsDialog("Выберите материалы", materialService);
        this.setSizeFull();
        var splitLayout = new SplitLayout(addGrid(), addForm(), SplitLayout.Orientation.HORIZONTAL);
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(60.0);
        this.add(splitLayout);
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
           bean.setBean(e.getItem());
           materialSelect.setItems(bean.getBean().getListOfMaterials());
           bean.refreshFields();
        });

        materialDialog.getGrid().setItems(materialService.findAll());

        return  new VerticalLayout(buttons(), filterField, grid);
    }

    private Component addForm() {
        materialSelect.setLabel("Материал по умолчанию");

        materialDialog.addOpenedChangeListener(openedChangeEvent -> {
            if (openedChangeEvent.isOpened()) materialDialog.setSelectedMaterial(bean.getBean().getListOfMaterials());
            else {
                var oldValue = materialSelect.getOptionalValue();
                materialSelect.setItems(materialDialog.getGrid().getSelectedItems());
                oldValue.ifPresent(materialSelect::setValue);
            }
        });
        bean.forField(materialDialog.getGrid().asMultiSelect()).bind(Operation::getListOfMaterials, Operation::setListOfMaterials);
        bean.forField(materialSelect).asRequired().bind(Operation::getDefaultMaterial, Operation::setDefaultMaterial);

        var mButton = new Button("Материал", e->materialDialog.open());

        var name = new TextField("Название");
        bean.forField(name).asRequired().bind(Operation::getName, Operation::setName);
        Select<TypeOfOperation> typeOfWork = new Select<>();
        typeOfWork.setLabel("Тип работы");
        typeOfWork.setItems(typeOfOperationService.findAll());
        bean.forField(typeOfWork).asRequired().bind(Operation::getTypeOfOperation, Operation::setTypeOfOperation);

        var actionCheck = new Checkbox("Есть работа?");
        bean.forField(actionCheck).bind(Operation::haveAction, Operation::setHaveAction);
        Select<Formulas> actFormula = new Select<>();
        actFormula.setLabel("Формула");
        actFormula.setItems(formulasService.findAll());
        bean.forField(actFormula).asRequired().bind(Operation::getActionFormula, Operation::setActionFormula);
        var materialCheck = new Checkbox("Есть материал?");
        bean.forField(materialCheck).bind(Operation::haveMaterials, Operation::setHaveMaterial);
        Select<Formulas> mFormula = new Select<>();
        mFormula.setLabel("Формула");
        mFormula.setItems(formulasService.findAll());
        bean.forField(mFormula).asRequired().bind(Operation::getMaterialFormula, Operation::setMaterialFormula);

        var formLayout = new FormLayout();
        formLayout.add(name, typeOfWork, actionCheck, actFormula, materialCheck, mFormula, mButton, materialSelect);

        var save = new Button("Сохранить", buttonClickEvent -> save());
        var cancel = new Button("Отменить", buttonClickEvent -> cancel());
        var hl = new HorizontalLayout(save,cancel);
        hl.setJustifyContentMode(JustifyContentMode.END);
        formLayout.addFormItem(hl,"");
        return new VerticalLayout(formLayout);
    }

    public void populate(String str) {
        grid.setItems(service.populate(str));

    }

    private void save(){
        if (bean.isValid()) {
            service.save(bean.getBean());
            Notification.show("Сохранено");
            populate(null);
            cancel();
        }
        else Notification.show("Заполните требуемые поля!");
    }

    private void cancel(){
        bean.removeBean();
        bean.refreshFields();
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
            bean.setBean(new Operation());
            bean.refreshFields();
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
        service.delete(bean.getBean());
        bean.removeBean();
        bean.refreshFields();
        populate(filterField.getValue().trim());
    }

}
