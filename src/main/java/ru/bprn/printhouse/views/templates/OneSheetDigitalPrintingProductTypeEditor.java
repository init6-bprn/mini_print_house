package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import lombok.Setter;
import ru.bprn.printhouse.data.service.*;
import ru.bprn.printhouse.views.material.service.MaterialService;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.entity.Templates;

import java.util.function.Consumer;

public class OneSheetDigitalPrintingProductTypeEditor extends AbstractEditor<AbstractProductType> {

    @Setter
    private Templates template;
    private final BeanValidationBinder<OneSheetDigitalPrintingProductType> validationBinder = new BeanValidationBinder<>(OneSheetDigitalPrintingProductType.class);
    private final VariablesForMainWorksService variablesForMainWorksService;
    private final FormulasService formulasService;
    private final StandartSizeService standartSizeService;
    private final GapService gapService;
    private final MaterialService materialService;

    public OneSheetDigitalPrintingProductTypeEditor(OneSheetDigitalPrintingProductType product, Consumer<AbstractProductType> onSave, VariablesForMainWorksService variablesForMainWorksService, FormulasService formulasService,
                                                    StandartSizeService standartSizeService, GapService gapService, MaterialService materialService){
        super(onSave);
        this.variablesForMainWorksService = variablesForMainWorksService;
        this.formulasService = formulasService;
        this.standartSizeService = standartSizeService;
        this.gapService = gapService;
        this.materialService = materialService;
        this.setSizeFull();

        var name = new TextField("Название цепочки:");
        name.setWidthFull();
        this.validationBinder.bind(name, OneSheetDigitalPrintingProductType::getName, OneSheetDigitalPrintingProductType::setName);

        //var saveButton = new Button("Save", o -> saveBean());
        //var cancelButton = new Button("Cancel", o -> cancelBean());
        //var hl = new HorizontalLayout(FlexComponent.Alignment.END, saveButton, cancelButton);

    }

    @Override
    protected Component buildForm() {
        return null;
    }
}
