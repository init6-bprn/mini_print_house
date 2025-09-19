package ru.bprn.printhouse.views.machine.printers;

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
import ru.bprn.printhouse.views.machine.entity.DigitalPrintingMachine;
import ru.bprn.printhouse.views.machine.service.DigitalPrintingMachineService;
import ru.bprn.printhouse.views.machine.service.MachineVariableService;
import ru.bprn.printhouse.views.material.service.PrintingMaterialService;

@PageTitle("Цифровые печатные машины")
@Route(value = "digital_print_machine", layout = MainLayout.class)
@AnonymousAllowed
public class DigitalPrintingMachineView extends VerticalLayout {
    private final TextField filterField = new TextField();
    private final Grid<DigitalPrintingMachine> grid = new Grid<>(DigitalPrintingMachine.class, false);
    private final DigitalPrintingMachineService service;
    private final PrintingMaterialService materialService;
    private final MachineVariableService machineVariableService;
    private DigitalPrintingMachineEditor editor;

    public DigitalPrintingMachineView(DigitalPrintingMachineService service,
                                      PrintingMaterialService materialService,
                                      MachineVariableService machineVariableService) {
        this.service = service;
        this.materialService = materialService;
        this.machineVariableService = machineVariableService;
        this.setSizeFull();
        this.editor = new DigitalPrintingMachineEditor(null, this::save, materialService);
        editor.setVisible(false);
        var splitLayout = new SplitLayout(addGrid(), editor, SplitLayout.Orientation.HORIZONTAL);
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
            if (!grid.asSingleSelect().isEmpty()) {
                editor.setVisible(true);
                editor.edit(e.getItem());
            } else cancel();
        });

        grid.addSelectionListener(e->{
           if (e.getFirstSelectedItem().isEmpty()) cancel();
        });

        return  new VerticalLayout(buttons(), filterField, grid);
    }

    public void populate(String str) {
        grid.setItems(service.populate(str));
    }

    private void save(Object entity){
        if (entity instanceof DigitalPrintingMachine machine) {
            service.save(machine);
            Notification.show("Сохранено");
            populate(null);
            cancel();
        }
    }

    private void cancel(){
        editor.setVisible(false);
        grid.asSingleSelect().clear();
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
            DigitalPrintingMachine newMachine = new DigitalPrintingMachine();
            newMachine.initializeVariables(machineVariableService);
            editor.setVisible(true);
            editor.edit(newMachine);
        });
        createTemplateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createTemplateButton.setTooltipText("Создать новый принтер");

        var duplicateButton = new Button(VaadinIcon.COPY_O.create(), event -> {
            boolean notSelect = grid.getSelectedItems().isEmpty();
            if (!notSelect) {
                service.duplicate(grid.asSingleSelect().getValue());
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
        service.delete(grid.asSingleSelect().getValue());
        populate(filterField.getValue().trim());
        cancel();
    }

}
