package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import lombok.Setter;
import ru.bprn.printhouse.data.service.*;
import ru.bprn.printhouse.views.material.service.MaterialService;
import ru.bprn.printhouse.views.operation.service.OperationService;
import ru.bprn.printhouse.views.operation.service.TypeOfOperationService;
import ru.bprn.printhouse.views.templates.entity.Chains;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.service.TemplatesService;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class OneSheetDigitalPrintingProductTypeEditor extends VerticalLayout {

    @Setter
    private Templates template;
    private final BeanValidationBinder<OneSheetDigitalPrintingProductType> validationBinder = new BeanValidationBinder<>(OneSheetDigitalPrintingProductType.class);
    private final TemplatesService templatesService;
    private final TreeGrid<Object> treeGrid;
    private final SplitLayout splitLayout;
    private final TypeOfOperationService typeOfOperationService;
    private final OperationService worksBeanService;
    private final VariablesForMainWorksService variablesForMainWorksService;
    private final FormulasService formulasService;
    private final StandartSizeService standartSizeService;
    private final GapService gapService;
    private final MaterialService materialService;

    public OneSheetDigitalPrintingProductTypeEditor(SplitLayout splitLayout, TreeGrid<Object> treeGrid, TemplatesService templatesService,
                                                    TypeOfOperationService typeOfOperationService, OperationService worksBeanService,
                                                    VariablesForMainWorksService variablesForMainWorksService, FormulasService formulasService,
                                                    StandartSizeService standartSizeService, GapService gapService, MaterialService materialService){

        this.templatesService = templatesService;
        this.treeGrid = treeGrid;
        this.splitLayout = splitLayout;
        this.typeOfOperationService = typeOfOperationService;
        this.worksBeanService = worksBeanService;
        this.variablesForMainWorksService = variablesForMainWorksService;
        this.formulasService = formulasService;
        this.standartSizeService = standartSizeService;
        this.gapService = gapService;
        this.materialService = materialService;
        this.setSizeFull();

        var name = new TextField("Название цепочки:");
        name.setWidthFull();
        this.validationBinder.bind(name, OneSheetDigitalPrintingProductType::getName, OneSheetDigitalPrintingProductType::setName);

        var saveButton = new Button("Save", o -> saveBean());
        var cancelButton = new Button("Cancel", o -> cancelBean());
        var hl = new HorizontalLayout(FlexComponent.Alignment.END, saveButton, cancelButton);

    }

}
