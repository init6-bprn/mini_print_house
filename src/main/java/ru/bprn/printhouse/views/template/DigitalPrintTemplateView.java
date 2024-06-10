package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bprn.printhouse.data.entity.DigitalPrintTemplate;
import ru.bprn.printhouse.data.service.DigitalPrintTemplateService;
import ru.bprn.printhouse.views.MainLayout;

@PageTitle("Шаблон для цифровой печати")
@Route(value = "digital_print_templates", layout = MainLayout.class)
@AnonymousAllowed
public class DigitalPrintTemplateView extends VerticalLayout {

    private DigitalPrintTemplateService digitalPrintTemplateService;

    public DigitalPrintTemplateView (DigitalPrintTemplateService digitalPrintTemplateService){
        this.digitalPrintTemplateService = digitalPrintTemplateService;
        Grid<DigitalPrintTemplate> grid = new Grid<>(DigitalPrintTemplate.class);
        grid.setItems(digitalPrintTemplateService.findAll());
        grid.setHeight("200px");

        TabSheet tabSheet = new TabSheet();

        Button closeAllButton = new Button("Close all");
        closeAllButton.addClickListener(e-> tabSheet.getChildren().forEach(tabSheet::remove));

        tabSheet.setPrefixComponent(closeAllButton);

        MenuBar menuBar = new MenuBar();
        MenuItem item = menuBar.addItem(new Icon(VaadinIcon.PLUS));
        SubMenu subMenu = item.getSubMenu();
        subMenu.addItem("Цифровая печать", menuItemClickEvent -> tabSheet.add("Цифровая печать", new VerticalLayout()));
        subMenu.addItem("Резка", menuItemClickEvent -> tabSheet.add("Резка", new VerticalLayout()));
        subMenu.addItem("Верстка", menuItemClickEvent -> tabSheet.add("Верстка", new VerticalLayout()));
        tabSheet.setSuffixComponent(menuBar);

        add(grid,tabSheet);
    }
}
