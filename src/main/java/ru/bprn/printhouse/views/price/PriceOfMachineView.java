package ru.bprn.printhouse.views.price;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;

import ru.bprn.printhouse.views.price.entity.PriceOfMachine;
import ru.bprn.printhouse.views.MainLayout;
import ru.bprn.printhouse.views.machine.entity.AbstractMachine;
import ru.bprn.printhouse.views.machine.service.AbstractMachineService;
import ru.bprn.printhouse.views.price.service.PriceOfMachineService;

@PageTitle("Цены на оборудование (амортизация)")
@Route(value = "price-of-machine", layout = MainLayout.class)
@AnonymousAllowed
public class PriceOfMachineView extends VerticalLayout {

    public PriceOfMachineView(PriceOfMachineService priceService, AbstractMachineService machineService) {
        setSizeFull();

        // Filter
        ComboBox<AbstractMachine> machineFilter = new ComboBox<>("Фильтр по оборудованию");
        machineFilter.setItems(machineService.findAll());
        machineFilter.setItemLabelGenerator(AbstractMachine::getName);
        machineFilter.setClearButtonVisible(true);

        // CRUD
        GridCrud<PriceOfMachine> crud = new GridCrud<>(PriceOfMachine.class);
        crud.getCrudLayout().addFilterComponent(machineFilter);

        // Grid configuration
        crud.getGrid().setColumns("machine", "price", "currency", "effectiveDate");
        crud.getGrid().getColumnByKey("machine").setHeader("Оборудование");
        crud.getGrid().getColumnByKey("price").setHeader("Стоимость часа");
        crud.getGrid().getColumnByKey("currency").setHeader("Валюта");
        crud.getGrid().getColumnByKey("effectiveDate").setHeader("Дата установки");
        crud.getGrid().setColumnReorderingAllowed(true);

        // Form configuration
        crud.getCrudFormFactory().setUseBeanValidation(true);
        crud.getCrudFormFactory().setVisibleProperties("machine", "price", "currency", "effectiveDate");
        crud.getCrudFormFactory().setFieldProvider("machine", new ComboBoxProvider<>(machineService.findAll()));
        crud.getCrudFormFactory().setFieldCaptions("Оборудование", "Стоимость часа", "Валюта", "Дата установки");

        // Operations
        crud.setOperations(() -> priceService.findByMachine(machineFilter.getValue()), priceService::save, priceService::save, priceService::delete);
        machineFilter.addValueChangeListener(e -> crud.refreshGrid());

        add(crud);
    }
}