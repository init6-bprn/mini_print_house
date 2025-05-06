package ru.bprn.printhouse.data.products;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ru.bprn.printhouse.data.calculate.OneSheetDigitalPrintingCalculateWorkView;
import ru.bprn.printhouse.data.entity.DigitalPrinting;
import ru.bprn.printhouse.data.entity.WorkFlow;
import ru.bprn.printhouse.data.service.JSONToObjectsHelper;
import ru.bprn.printhouse.data.service.WorkFlowService;
import ru.bprn.printhouse.views.MainLayout;

@PageTitle("Продукты")
@Route(value = "products", layout = MainLayout.class)
@AnonymousAllowed
public class ProductsView extends HorizontalLayout {

    private final WorkFlowService workFlowService;

    public ProductsView(WorkFlowService workFlowService){
        super();
        this.workFlowService = workFlowService;
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

        Button bookVacationButton = new Button("Заказать");
        bookVacationButton.addClickListener(buttonClickEvent -> {
            var form = new FormLayout();
            form.setSizeFull();
            form.add(getProductLayout(workFlow));
            workFlow.getName();
        });
        card.addToFooter(bookVacationButton);


        card.setHeight("240px");
        card.setMaxWidth("300px");
        add(card);

        return card;
    }

    private VerticalLayout getProductLayout(WorkFlow wf) {
        var vl = new VerticalLayout();
        var list = JSONToObjectsHelper.getListOfObjects(wf.getStrJSON());

        if (wf.getType().equals(DigitalPrinting.class.getSimpleName())) new OneSheetDigitalPrintingCalculateWorkView(list);

        return vl;
    }
}
