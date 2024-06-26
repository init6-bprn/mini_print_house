package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ru.bprn.printhouse.data.entity.Template;
import ru.bprn.printhouse.data.service.*;
import ru.bprn.printhouse.views.MainLayout;

@PageTitle("Шаблон для цифровой печати")
@Route(value = "digital_print_templates", layout = MainLayout.class)
@AnonymousAllowed
public class DigitalPrintTemplateView extends VerticalLayout {

    private MaterialService materialService;
    private final StandartSizeService standartSizeService;
    private final TypeOfMaterialService typeOfMaterialService;
    private final GapService gapService;
    private final TemplateService templateService;
    private final ImposeCaseService imposeCaseService;
    private final TabSheet tabSheet = new TabSheet();
    private Template template = new Template();

    public DigitalPrintTemplateView (StandartSizeService standartSizeService, TypeOfMaterialService typeOfMaterialService, MaterialService materialService, GapService gapService,
                                     TemplateService templateService, ImposeCaseService imposeCaseService){
        //this.printerService = printerService;
        this.materialService = materialService;
        this.standartSizeService = standartSizeService;
        this.typeOfMaterialService = typeOfMaterialService;
        this.gapService = gapService;
        this.templateService = templateService;
        this.imposeCaseService = imposeCaseService;

        addGridSection();
        addTabSheetSection();

    }

    private void addGridSection(){
        Grid<Template> grid = new Grid<>(Template.class);
        grid.setItems(this.templateService.findAll());
        grid.setHeight("200px");
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        this.add(grid);
        grid.addSelectionListener(selectionEvent -> {
            if (selectionEvent.getFirstSelectedItem().isPresent())
                     template = selectionEvent.getFirstSelectedItem().get();
        });

    }

    private void addTabSheetSection(){
        Button closeAllButton = new Button("Close all");
        closeAllButton.addClickListener(e-> tabSheet.getChildren().forEach(tabSheet::remove));

        tabSheet.setPrefixComponent(closeAllButton);
        tabSheet.setWidthFull();
        StartTemplateTabVerticalLayout startTab = new StartTemplateTabVerticalLayout(template, standartSizeService,
                                                      typeOfMaterialService, materialService, gapService, imposeCaseService);

        tabSheet.add("Настройки", startTab);

        MenuBar menuBar = new MenuBar();
        MenuItem item = menuBar.addItem(new Icon(VaadinIcon.PLUS));
        SubMenu subMenu = item.getSubMenu();
        subMenu.addItem("Цифровая печать", menuItemClickEvent -> tabSheet.add(createTab("Цифровая печать"),
                new VerticalLayout()));
                //new TemplateView(startTab.getMaterial(), startTab.getSize(), startTab.getBleed(), printerService)));
        subMenu.addItem("Резка", menuItemClickEvent -> tabSheet.add(createTab("Резка"), new VerticalLayout()));
        subMenu.addItem("Верстка", menuItemClickEvent -> tabSheet.add(createTab("Верстка"), new VerticalLayout()));
        tabSheet.setSuffixComponent(menuBar);

        tabSheet.addSelectedChangeListener(selectedChangeEvent -> {
            Tab tabb = selectedChangeEvent.getPreviousTab();
            VerticalLayout vl = (VerticalLayout) selectedChangeEvent.getSource().getComponent(tabb);

            Notification.show("Надо проверить "+vl.getClass().toString());
        });
        this.add(tabSheet);
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
            if (tabSheet.getChildren().anyMatch(Tabs.class::isInstance)) {
                Tabs tabs = (Tabs) tabSheet.getChildren().filter(Tabs.class::isInstance).findFirst().get();
                int i = tabSheet.getIndexOf(tab);
                tabs.setSelectedIndex(i);
                tab.setSelected(true);
                if (i > 0) {
                    Tab twoTab = tabSheet.getTabAt(i);
                    tabs.setSelectedIndex(i - 1);
                    tabs.addTabAtIndex(i - 1, twoTab);
                    tabs.setSelectedTab(tab);
                }
            }
        });

        var dialog = new ConfirmDialog("Удалить вкладку " + str + "?", "Все изменения в этой вкладке тоже удалятся...",
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
            if (tabSheet.getChildren().anyMatch(Tabs.class::isInstance)) {
                Tabs tabs = (Tabs) tabSheet.getChildren().filter(Tabs.class::isInstance).findFirst().get();
                int j = tabs.getComponentCount();
                int i = tabSheet.getIndexOf(tab);
                tabs.setSelectedIndex(i);
                tab.setSelected(true);
                if (i + 1 < j) {
                    Tab twoTab = tabSheet.getTabAt(i + 1);
                    tabs.setSelectedIndex(i);
                    tabs.addTabAtIndex(i, twoTab);
                    tabs.setSelectedTab(tab);
                }
            }
        });

        la.add(leftBtn, closeBtn, rightBtn);
        vla.add(la);
        tab.add(vla);
        return tab;
    }

}
