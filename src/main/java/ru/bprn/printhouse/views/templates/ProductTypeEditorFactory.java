package ru.bprn.printhouse.views.templates;

import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.data.service.GapService;
import ru.bprn.printhouse.data.service.StandartSizeService;
import ru.bprn.printhouse.data.service.VariablesForMainWorksService;
import ru.bprn.printhouse.views.material.service.MaterialService;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;

import java.util.function.Consumer;

public class ProductTypeEditorFactory {
    private final VariablesForMainWorksService variablesForMainWorksService;
    private final FormulasService formulasService;
    private final StandartSizeService standartSizeService;
    private final GapService gapService;
    private final MaterialService materialService;

    public ProductTypeEditorFactory(VariablesForMainWorksService variablesForMainWorksService, FormulasService formulasService,
                                    StandartSizeService standartSizeService, GapService gapService, MaterialService materialService) {

        this.variablesForMainWorksService = variablesForMainWorksService;
        this.formulasService = formulasService;
        this.standartSizeService = standartSizeService;
        this.gapService = gapService;
        this.materialService = materialService;
    }

    public AbstractEditor<? extends AbstractProductType> createEditor(
            AbstractProductType productType,
            Consumer<AbstractProductType> onSave) {
        return switch (productType) {
            case OneSheetDigitalPrintingProductType product -> new OneSheetDigitalPrintingProductTypeEditor(product, onSave, variablesForMainWorksService, formulasService, standartSizeService, gapService, materialService);

        };
    }
}
