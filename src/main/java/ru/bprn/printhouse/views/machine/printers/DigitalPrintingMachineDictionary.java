package ru.bprn.printhouse.views.machine.printers;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
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
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ru.bprn.printhouse.data.entity.Gap;
import ru.bprn.printhouse.data.service.GapService;
import ru.bprn.printhouse.views.MainLayout;
import ru.bprn.printhouse.views.machine.entity.DigitalPrintingMachine;
import ru.bprn.printhouse.views.machine.service.DigitalPrintingMachineService;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.service.PrintingMaterialService;

@PageTitle("Цифровые печатные машины")
@Route(value = "digital_print_machine", layout = MainLayout.class)
@AnonymousAllowed
public class DigitalPrintingMachineDictionary extends VerticalLayout {
    private final TextField filterField = new TextField();
    private final Grid<DigitalPrintingMachine> grid = new Grid<>(DigitalPrintingMachine.class, false);
    private final DigitalPrintingMachineService service;
    private final GapService gapService;
    private final PrintingMaterialService materialService;
    private final BeanValidationBinder<DigitalPrintingMachine> bean = new BeanValidationBinder<>(DigitalPrintingMachine.class);
    private final MultiSelectComboBox<AbstractMaterials> materials = new MultiSelectComboBox<>("Материалы для использования в устройстве");

    public DigitalPrintingMachineDictionary(DigitalPrintingMachineService service,
                                            GapService gapService,
                                            PrintingMaterialService materialService) {
        this.service = service;
        this.gapService = gapService;
        this.materialService = materialService;
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

        grid.addColumn(DigitalPrintingMachine::getName,"Название" );
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        populate(null);

        grid.addItemClickListener(e->{
           bean.setBean(e.getItem());
           materials.setValue(bean.getBean().getAbstractMaterials());
           bean.refreshFields();
        });

        return  new VerticalLayout(buttons(), filterField, grid);
    }

    private Component addForm() {

        var name = new TextField("Название");
        bean.forField(name).asRequired().bind(DigitalPrintingMachine::getName, DigitalPrintingMachine::setName);

        var sizeX = new IntegerField("Максимальная длина материала в мм");
        bean.forField(sizeX).asRequired().bind(DigitalPrintingMachine::getMaxSizeX, DigitalPrintingMachine::setMaxSizeX);

        var sizeY = new IntegerField("Максимальная ширина материала в мм");
        bean.forField(sizeY).asRequired().bind(DigitalPrintingMachine::getMaxSizeY, DigitalPrintingMachine::setMaxSizeY);

        var sizeOfClick = new IntegerField("Размер клика в мм");
        bean.forField(sizeOfClick).asRequired().bind(DigitalPrintingMachine::getSizeOfClick, DigitalPrintingMachine::setSizeOfClick);

        Select<Gap> gapSelect1 = new Select<>();
        gapSelect1.setLabel("Отступы");
        gapSelect1.setItems(gapService.findAll());
        bean.forField(gapSelect1).asRequired().bind(DigitalPrintingMachine::getGap, DigitalPrintingMachine::setGap);

        materials.setSelectedItemsOnTop(true);
        materials.setItems(materialService.findAllAsAbstract());
        bean.forField(materials).bind(DigitalPrintingMachine::getAbstractMaterials, DigitalPrintingMachine::setAbstractMaterials);

        var formLayout = new FormLayout();
        formLayout.add(name, sizeX, sizeY, sizeOfClick, gapSelect1, materials);

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
            bean.setBean(new DigitalPrintingMachine());
            bean.refreshFields();
        });
        createTemplateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createTemplateButton.setTooltipText("Создать новый принтер");

        var duplicateButton = new Button(VaadinIcon.COPY_O.create(), event -> {
                service.duplicate(grid.asSingleSelect().getValue());
                populate(filterField.getValue().trim());
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
        service.delete(bean.getBean());
        bean.removeBean();
        bean.refreshFields();
        populate(filterField.getValue().trim());
    }

}
