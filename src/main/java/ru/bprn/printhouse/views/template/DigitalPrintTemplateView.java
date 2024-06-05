package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bprn.printhouse.data.entity.DigitalPrintTemplate;
import ru.bprn.printhouse.data.service.DigitalPrintTemplateService;
import ru.bprn.printhouse.views.MainLayout;

@Component
@PageTitle("Шаблон для цифровой печати")
@Route(value = "digital_print_templates", layout = MainLayout.class)
@AnonymousAllowed
public class DigitalPrintTemplateView extends VerticalLayout {

    @Autowired
    private DigitalPrintTemplateService digitalPrintTemplateService;

    public DigitalPrintTemplateView (DigitalPrintTemplateService digitalPrintTemplateService){
        this.digitalPrintTemplateService = digitalPrintTemplateService;
        Grid<DigitalPrintTemplate> grid = new Grid<>(DigitalPrintTemplate.class);
        grid.setItems(digitalPrintTemplateService.findAll());
        add(grid);
    }
}
