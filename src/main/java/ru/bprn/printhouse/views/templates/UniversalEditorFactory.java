package ru.bprn.printhouse.views.templates;

import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.data.service.StandartSizeService;
import ru.bprn.printhouse.data.service.VariablesForMainWorksService;
import ru.bprn.printhouse.views.material.service.PrintSheetsMaterialService;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.entity.Templates;

import java.util.function.Consumer;

public class UniversalEditorFactory {

    private final PrintSheetsMaterialService printSheetsMaterialService;
    private final FormulasService formulasService;
    private final VariablesForMainWorksService variablesForMainWorksService;
    private final StandartSizeService standartSizeService;

    public UniversalEditorFactory(PrintSheetsMaterialService printSheetsMaterialService, FormulasService formulasService,
                                  VariablesForMainWorksService variablesForMainWorksService, StandartSizeService standartSizeService) {
        this.printSheetsMaterialService = printSheetsMaterialService;

        this.formulasService = formulasService;
        this.variablesForMainWorksService = variablesForMainWorksService;
        this.standartSizeService = standartSizeService;
    }

    public AbstractEditor<?> createEditor(
            Object productType,
            Consumer<Object> onSave) {
        return switch (productType) {
            case OneSheetDigitalPrintingProductType product -> new OneSheetDigitalPrintingProductTypeEditor(
                            product, onSave, printSheetsMaterialService, formulasService, variablesForMainWorksService, standartSizeService);
            case Operation operation -> new OperationEditor(operation, onSave);
            case Templates template ->  new TemplateEditor(template, onSave);
            default -> null;
        };
    }
}
