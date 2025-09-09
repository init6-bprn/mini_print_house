package ru.bprn.printhouse.views.products;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ru.bprn.printhouse.data.calculate.OneSheetDigitalPrintingCalculateWorkView;
import ru.bprn.printhouse.data.entity.WorkFlow;
import ru.bprn.printhouse.data.service.*;
import ru.bprn.printhouse.views.MainLayout;

@PageTitle("Продукты")
@Route(value = "products", layout = MainLayout.class)
@AnonymousAllowed
public class ProductsView extends HorizontalLayout {

    private final WorkFlowService workFlowService;
    private final CostOfPrintSizeLeafAndColorService costService;
    private final PriceOfMaterialService materialService;
    private final PrintSpeedMaterialDensityService speedMaterialDensityService;
    private final Dialog dialog = new Dialog();

    public ProductsView(WorkFlowService workFlowService, CostOfPrintSizeLeafAndColorService costService,
                        PriceOfMaterialService materialService, PrintSpeedMaterialDensityService speedMaterialDensityService){
        super();
        this.costService = costService;
        this.workFlowService = workFlowService;
        this.materialService = materialService;
        this.speedMaterialDensityService = speedMaterialDensityService;
        setWrap(true);
        setSizeFull();
        populateLayout();

    }

    private void populateLayout() {
        var list = workFlowService.findAll();
        for (WorkFlow wf: list) {
            this.add(addCard(wf));
        }
    }

    private Component addCard(WorkFlow workFlow) {
        var card = new Card();
        card.setTitle(new Div(workFlow.getName()));
        card.add(workFlow.getDescription());

        Button buyButton = new Button("Заказать");
        buyButton.addClickListener(buttonClickEvent -> {
            var form = new OneSheetDigitalPrintingCalculateWorkView(
                    JSONToObjectsHelper.getListOfObjects(
                            workFlow.getStrJSON()), costService, materialService, speedMaterialDensityService, "Заказ продукта "+workFlow.getName());
            form.setHeight("50%");
            form.setWidth("50%");
            form.setModal(true);
            form.open();
        });
        card.addToFooter(buyButton);


        card.setHeight("240px");
        card.setMaxWidth("200px");

        return card;
    }

}
