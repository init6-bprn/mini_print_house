package ru.bprn.printhouse.views.templates;

import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.data.service.StandartSizeService;
import ru.bprn.printhouse.views.material.service.AbstractMaterialService;
import ru.bprn.printhouse.views.material.service.PrintSheetsMaterialService;
import ru.bprn.printhouse.views.operation.entity.ProductOperation;
import ru.bprn.printhouse.views.operation.service.OperationService;
import ru.bprn.printhouse.views.operation.service.TypeOfOperationService;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.service.ProductTypeVariableService;
import ru.bprn.printhouse.views.templates.service.FormulaValidationService;

import java.util.function.Consumer;

public class UniversalEditorFactory {

    private final PrintSheetsMaterialService printSheetsMaterialService;
    private final FormulasService formulasService;
    private final FormulaValidationService formulaValidationService;
    private final ProductTypeVariableService productTypeVariableService;
    private final StandartSizeService standartSizeService;
    private final TypeOfOperationService typeOfOperationService;
    private final AbstractMaterialService abstractMaterialService;
    private final TemplatesView templatesView;
    private final OperationService operationService;

    public UniversalEditorFactory(PrintSheetsMaterialService printSheetsMaterialService, FormulasService formulasService, ProductTypeVariableService productTypeVariableService,
                                  FormulaValidationService formulaValidationService, StandartSizeService standartSizeService,
                                  TypeOfOperationService typeOfOperationService, AbstractMaterialService abstractMaterialService,
                                  OperationService operationService,
                                  TemplatesView templatesView) {
        this.printSheetsMaterialService = printSheetsMaterialService;

        this.formulasService = formulasService;
        this.formulaValidationService = formulaValidationService;
        this.templatesView = templatesView;
        this.productTypeVariableService = productTypeVariableService;
        this.standartSizeService = standartSizeService;
        this.typeOfOperationService = typeOfOperationService;
        this.abstractMaterialService = abstractMaterialService;
        this.operationService = operationService;
    }

    public AbstractEditor<?> createEditor(
            Object productType,
            Consumer<Object> onSave) {
        return switch (productType) {
            case OneSheetDigitalPrintingProductType product -> new OneSheetDigitalPrintingProductTypeEditor(
                            product, onSave, printSheetsMaterialService, formulasService, standartSizeService,
                            typeOfOperationService, formulaValidationService, productTypeVariableService);
            case Templates template ->  new TemplateEditor(template, onSave);
            case ProductOperation productOperation -> new ProductOperationEditor(productOperation, onSave, abstractMaterialService);
            default -> null;
        };
    }
}
