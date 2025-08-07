package ru.bprn.printhouse.views.templates;

import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.data.service.GapService;
import ru.bprn.printhouse.data.service.StandartSizeService;
import ru.bprn.printhouse.data.service.VariablesForMainWorksService;
import ru.bprn.printhouse.views.material.service.MaterialService;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.entity.Templates;

import java.util.function.Consumer;

public class UniversalEditorFactory {
    private final VariablesForMainWorksService variablesForMainWorksService;
    private final FormulasService formulasService;
    private final StandartSizeService standartSizeService;
    private final GapService gapService;
    private final MaterialService materialService;

    public UniversalEditorFactory(VariablesForMainWorksService variablesForMainWorksService, FormulasService formulasService,
                                  StandartSizeService standartSizeService, GapService gapService, MaterialService materialService) {

        this.variablesForMainWorksService = variablesForMainWorksService;
        this.formulasService = formulasService;
        this.standartSizeService = standartSizeService;
        this.gapService = gapService;
        this.materialService = materialService;
    }

    public AbstractEditor<?> createEditor(
            Object productType,
            Consumer<?> onSave) {
        return switch (productType) {
            case OneSheetDigitalPrintingProductType product ->
                    new OneSheetDigitalPrintingProductTypeEditor(product, (Consumer<AbstractProductType>) onSave, variablesForMainWorksService, formulasService, standartSizeService, gapService, materialService);
            case Operation operation ->
                    new OperationEditor(operation, (Consumer<Operation>) onSave);
            case Templates template ->
                new TemplateEditor(template, (Consumer<Templates>) onSave);
            default -> null;
        };
    }
}
