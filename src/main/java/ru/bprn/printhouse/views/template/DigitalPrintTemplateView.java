package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.Tabs;
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
    private TabSheet tabSheet = new TabSheet();

    public DigitalPrintTemplateView (DigitalPrintTemplateService digitalPrintTemplateService){
        this.digitalPrintTemplateService = digitalPrintTemplateService;
        Grid<DigitalPrintTemplate> grid = new Grid<>(DigitalPrintTemplate.class);
        grid.setItems(digitalPrintTemplateService.findAll());
        grid.setHeight("200px");

        //((Tabs) tabSheet.getChildren().filter(Tabs.class::isInstance).findFirst().get()).getComponentCount();

        Button closeAllButton = new Button("Close all");
        closeAllButton.addClickListener(e-> tabSheet.getChildren().forEach(tabSheet::remove));

        tabSheet.setPrefixComponent(closeAllButton);

        MenuBar menuBar = new MenuBar();
        MenuItem item = menuBar.addItem(new Icon(VaadinIcon.PLUS));
        SubMenu subMenu = item.getSubMenu();
        subMenu.addItem("Цифровая печать", menuItemClickEvent -> tabSheet.add(createTab("Цифровая печать"), new VerticalLayout()));
        subMenu.addItem("Резка", menuItemClickEvent -> tabSheet.add(createTab("Резка"), new VerticalLayout()));
        subMenu.addItem("Верстка", menuItemClickEvent -> tabSheet.add(createTab("Верстка"), new VerticalLayout()));
        tabSheet.setSuffixComponent(menuBar);

        add(grid,tabSheet);
    }

    private Tab createTab (String str){
        var tab = new Tab();
        var la = new HorizontalLayout();
        var vla = new VerticalLayout();
        vla.setAlignItems(Alignment.CENTER);
        vla.add(new Span(str));

        var leftBtn = new Button(VaadinIcon.CHEVRON_CIRCLE_LEFT_O.create());
        leftBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        leftBtn.addClickListener(e->{
            Tabs tabs = (Tabs) tabSheet.getChildren().filter(Tabs.class::isInstance).findFirst().get();
            int i =  tabSheet.getIndexOf(tab);
            tabs.setSelectedIndex(i);
            tab.setSelected(true);
            if (i>0) {
                Tab twoTab = tabSheet.getTabAt(i);
                tabs.setSelectedIndex(i-1);
                tabs.addTabAtIndex(i-1,twoTab);
                tabSheet.setSelectedTab(tab);
            }
        });

        var dialog = new ConfirmDialog("Вы хотите удалить "+tab.getLabel(), "Все изменения тоже удалятся...",
                "Да",confirmEvent -> tabSheet.remove(tab), "Нет", cancelEvent -> cancelEvent.getSource().close());

        var closeBtn = new Button(VaadinIcon.CLOSE_CIRCLE_O.create());
        closeBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        closeBtn.addClickListener(e->{
            tabSheet.setSelectedTab(tab);
            dialog.open();
        });

        var rightBtn = new Button(VaadinIcon.CHEVRON_CIRCLE_RIGHT_O.create());
        rightBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
        rightBtn.addClickListener(e->{
            Tabs tabs = (Tabs) tabSheet.getChildren().filter(Tabs.class::isInstance).findFirst().get();
            int j = tabs.getComponentCount();
            int i =  tabSheet.getIndexOf(tab);
            tabs.setSelectedIndex(i);
            tab.setSelected(true);
            if (i+1<j) {
                Tab twoTab = tabSheet.getTabAt(i+1);
                tabs.setSelectedIndex(i);
                tabs.addTabAtIndex(i,twoTab);
                tabSheet.setSelectedTab(tab);
            }
        });

        la.add(leftBtn, closeBtn, rightBtn);
        vla.add(la);
        tab.add(vla);
        return tab;
    }

}
