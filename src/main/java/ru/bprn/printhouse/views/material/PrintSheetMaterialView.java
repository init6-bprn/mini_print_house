package ru.bprn.printhouse.views.material;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ru.bprn.printhouse.data.entity.TypeOfMaterial;
import ru.bprn.printhouse.data.service.TypeOfMaterialService;
import ru.bprn.printhouse.views.MainLayout;
import ru.bprn.printhouse.views.machine.entity.AbstractMachine;
import ru.bprn.printhouse.views.machine.service.DigitalPrintingMachineService;
import ru.bprn.printhouse.views.material.entity.PrintSheetsMaterial;
import ru.bprn.printhouse.views.material.service.PrintSheetsMaterialService;

@PageTitle("Бумага листовая")
@Route(value = "paper_sheet_materials", layout = MainLayout.class)
@AnonymousAllowed

public class PrintSheetMaterialView extends VerticalLayout {

    private final PrintSheetsMaterialService materialService;
    private final TextField filterField = new TextField();
    private final Grid<PrintSheetsMaterial> grid = new Grid<>(PrintSheetsMaterial.class, false);
    private final DigitalPrintingMachineService service;
    private final TypeOfMaterialService typeOfMaterialService;
    private final BeanValidationBinder<PrintSheetsMaterial> bean = new BeanValidationBinder<>(PrintSheetsMaterial.class);
    private final MultiSelectComboBox<AbstractMachine> machines = new MultiSelectComboBox<>("Принтеры");
    private final VerticalLayout form;

    public PrintSheetMaterialView(PrintSheetsMaterialService materialService, DigitalPrintingMachineService service, TypeOfMaterialService typeOfMaterialService){
        this.materialService = materialService;

        this.service = service;
        this.typeOfMaterialService = typeOfMaterialService;
        this.setSizeFull();
        form = addForm();
        form.setEnabled(false);
        machines.setItemLabelGenerator(AbstractMachine::getName);

        var splitLayout = new SplitLayout(addGrid(), form, SplitLayout.Orientation.HORIZONTAL);
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

        grid.addColumn(PrintSheetsMaterial::getName,"Название" );
        grid.addColumn(PrintSheetsMaterial::getTypeOfMaterial);
        grid.addColumn(PrintSheetsMaterial::getThickness);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        populate(null);

        grid.addItemClickListener(e->{
            if (!grid.asSingleSelect().isEmpty()) {
                form.setEnabled(true);
                bean.setBean(e.getItem());
                machines.setValue(bean.getBean().getAbstractMachines());
                bean.refreshFields();
            } else cancel();
        });

        grid.addSelectionListener(e->{
            if (e.getFirstSelectedItem().isEmpty()) form.setEnabled(false);
        });

        return  new VerticalLayout(buttons(), filterField, grid);
    }

    private VerticalLayout addForm() {

        var name = new TextField("Название");
        bean.forField(name).asRequired().bind(PrintSheetsMaterial::getName, PrintSheetsMaterial::setName);

        var sizeX = new IntegerField("Длина в мм");
        bean.forField(sizeX).asRequired().bind(PrintSheetsMaterial::getSizeX, PrintSheetsMaterial::setSizeX);

        var sizeY = new IntegerField("Ширина в мм");
        bean.forField(sizeY).asRequired().bind(PrintSheetsMaterial::getSizeY, PrintSheetsMaterial::setSizeY);

        var density = new IntegerField("Плотность");
        bean.forField(density).bind(PrintSheetsMaterial::getThickness, PrintSheetsMaterial::setThickness);

        var units = new TextField("Единицы измерения");
        bean.forField(units).bind(PrintSheetsMaterial::getUnitsOfMeasurement, PrintSheetsMaterial::setUnitsOfMeasurement);

        machines.setSelectedItemsOnTop(true);
        machines.setItems(service.findAllAsAbstract());
        bean.forField(machines).bind(PrintSheetsMaterial::getAbstractMachines, PrintSheetsMaterial::setAbstractMachines);

        ComboBox<TypeOfMaterial> typeOfMaterialCombo = new ComboBox<>("Тип материала");
        typeOfMaterialCombo.setItems(typeOfMaterialService.findAll());
        bean.forField(typeOfMaterialCombo).bind(PrintSheetsMaterial::getTypeOfMaterial, PrintSheetsMaterial::setTypeOfMaterial);


        var formLayout = new FormLayout();
        formLayout.add(name, typeOfMaterialCombo, sizeX, sizeY, density, units, machines);

        var save = new Button("Сохранить", buttonClickEvent -> save());
        var cancel = new Button("Отменить", buttonClickEvent -> cancel());
        var hl = new HorizontalLayout(save,cancel);
        hl.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        formLayout.addFormItem(hl,"");
        return new VerticalLayout(formLayout);
    }

    public void populate(String str) {
        grid.setItems(materialService.populate(str));
    }

    private void save(){
        if (bean.isValid()) {
            materialService.save(bean.getBean());
            Notification.show("Сохранено");
            populate(null);
            cancel();
        }
        else Notification.show("Заполните требуемые поля!");
    }

    private void cancel(){
        bean.removeBean();
        bean.refreshFields();
        form.setEnabled(false);
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
            form.setEnabled(true);
            bean.setBean(new PrintSheetsMaterial());
            bean.refreshFields();
        });
        createTemplateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createTemplateButton.setTooltipText("Создать новый принтер");

        var duplicateButton = new Button(VaadinIcon.COPY_O.create(), event -> {
            boolean notSelect = grid.getSelectedItems().isEmpty();
            if (!notSelect) {
                materialService.duplicate(grid.asSingleSelect().getValue());
                populate(filterField.getValue().trim());
                cancel();
            } else Notification.show("Сперва выберите элемент из списка!");
        });
        duplicateButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        duplicateButton.setTooltipText("Дублировать принтер");

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
        materialService.delete(bean.getBean());
        bean.removeBean();
        bean.refreshFields();
        populate(filterField.getValue().trim());
    }

}
