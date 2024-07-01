package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.SortOrderProvider;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ru.bprn.printhouse.data.entity.Template;
import ru.bprn.printhouse.data.service.*;
import ru.bprn.printhouse.views.MainLayout;

import java.util.Optional;

@PageTitle("Шаблон для цифровой печати")
@Route(value = "digital_print_templates", layout = MainLayout.class)
@AnonymousAllowed
public class DigitalPrintTemplateView extends SplitLayout {

    private MaterialService materialService;
    private final StandartSizeService standartSizeService;
    private final TypeOfMaterialService typeOfMaterialService;
    private final GapService gapService;
    private final TemplateService templateService;
    private final ImposeCaseService imposeCaseService;
    private final TabSheet tabSheet = new TabSheet();
    private Template template;
    private StartTemplateTabVerticalLayout startTab;

    private Grid<Template> templateGrid = new Grid<>(Template.class, false);

    public DigitalPrintTemplateView (StandartSizeService standartSizeService, TypeOfMaterialService typeOfMaterialService, MaterialService materialService, GapService gapService,
                                     TemplateService templateService, ImposeCaseService imposeCaseService){
        this.materialService = materialService;
        this.standartSizeService = standartSizeService;
        this.typeOfMaterialService = typeOfMaterialService;
        this.gapService = gapService;
        this.templateService = templateService;
        this.imposeCaseService = imposeCaseService;

        this.setOrientation(Orientation.VERTICAL);
        addToPrimary(addGridSection());
        addToSecondary(addTabSheetSection());
        this.getSecondaryComponent().getElement().setEnabled(false);
        this.setSizeFull();
        this.setSplitterPosition(35.0);

    }

    private VerticalLayout addGridSection(){
        var vl = new VerticalLayout();
        vl.setSizeUndefined();

        var dialog = new ConfirmDialog("Вы уверены, что хотите удалить этот workflow?" , "",
                "Да",confirmEvent -> {if (template!=null) templateService.delete(template);},
                "Нет", cancelEvent -> cancelEvent.getSource().close());

        var hl = new HorizontalLayout();
        var createButton = new Button(VaadinIcon.PLUS.create(), buttonClickEvent -> {
            this.getPrimaryComponent().setVisible(false);
            this.getSecondaryComponent().getElement().setEnabled(true);
            this.setSplitterPosition(0);
            template= new Template();
            startTab.getTemplateBinder().setBean(template);
        });
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var updateButton  = new Button(VaadinIcon.EDIT.create(), buttonClickEvent -> {
            var optTemp = templateGrid.getSelectedItems().stream().findFirst();
            if (optTemp.isPresent()) {
                template = optTemp.get();
                startTab.getTemplateBinder().readBean(template);
                this.getPrimaryComponent().setVisible(false);
                this.getSecondaryComponent().getElement().setEnabled(true);
                this.setSplitterPosition(0);
            }
        });
        updateButton.addThemeVariants(ButtonVariant.LUMO_ICON);

        var duplicateButton = new Button(VaadinIcon.COPY_O.create(), buttonClickEvent -> {
            var optTemp = templateGrid.getSelectedItems().stream().findFirst();
            if (optTemp.isPresent()) {
                var ishueTemplate = optTemp.get();
                template = new Template();
                template.setName(ishueTemplate.getName());
                template.setStandartSize(ishueTemplate.getStandartSize());
                template.setSizeX(ishueTemplate.getSizeX());
                template.setSizeY(ishueTemplate.getSizeY());
                template.setImposeCase(ishueTemplate.getImposeCase());
                template.setMaterial(ishueTemplate.getMaterial());
                template.setGap(ishueTemplate.getGap());
                template.setQuantityOfLeaves(ishueTemplate.getQuantityOfLeaves());

                startTab.getTemplateBinder().readBean(template);
                this.getPrimaryComponent().setVisible(false);
                this.getSecondaryComponent().getElement().setEnabled(true);
                this.setSplitterPosition(0);
            }
        });
        duplicateButton.addThemeVariants(ButtonVariant.LUMO_ICON);

        var deleteButton = new Button(VaadinIcon.CLOSE.create(), buttonClickEvent -> {
            var optTemp = templateGrid.getSelectedItems().stream().findFirst();
            if (optTemp.isPresent()) {
                template = optTemp.get();
                dialog.open();
            }
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);


        hl.add(createButton, updateButton, duplicateButton, deleteButton);
        vl.add(hl);

        templateGrid.addColumn(Template::getName).setHeader("Имя");
        templateGrid.addColumn(Template::getStandartSize).setHeader("Размер");
        templateGrid.addColumn(Template::getMaterial).setHeader("Материал");

        templateGrid.setItems(this.templateService.findAll());
        templateGrid.setHeight("200px");
        templateGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        templateGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        vl.add(templateGrid);
        templateGrid.addSelectionListener(selectionEvent -> {
            if (selectionEvent.getFirstSelectedItem().isPresent())
                     template = selectionEvent.getFirstSelectedItem().get();
        });
        return vl;
    }

    private VerticalLayout addTabSheetSection(){

        var confirmButton = new Button("Сохранить");
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        confirmButton.addClickListener(e-> {
            var binder = startTab.getTemplateBinder();
            Notification.show("готовимся к записи");
            binder.validate();
            if (binder.isValid()) {
                Notification.show("Бин валидный!");
                try {
                    binder.writeBean(template);
                    Notification.show("Записали!");
                    binder.readBean(template);
                } catch (ValidationException ex) {
                    Notification.show("Запись не удалась!");
                    throw new RuntimeException(ex);

                }
                this.getPrimaryComponent().setVisible(true);
                this.getSecondaryComponent().getElement().setEnabled(false);
                this.setSplitterPosition(35.0);
                this.templateGrid.getDataProvider().refreshAll();
            } else Notification.show("Заполните все требуемые поля!");
        });

        var cancelButton = new Button("Отмена");
        cancelButton.addClickListener(buttonClickEvent -> {
           // startTab.getTemplateBinder()
           this.getPrimaryComponent().setVisible(true);
           this.getSecondaryComponent().getElement().setEnabled(false);
           this.setSplitterPosition(35.0);
        });


        tabSheet.setSuffixComponent(new HorizontalLayout(cancelButton,confirmButton));
        var vel = new VerticalLayout();
        tabSheet.setWidthFull();
        startTab = new StartTemplateTabVerticalLayout(standartSizeService,
                                                      typeOfMaterialService, materialService, gapService, imposeCaseService);

        tabSheet.add("Настройки", startTab);

        MenuBar menuBar = new MenuBar();
        MenuItem item = menuBar.addItem(new Icon(VaadinIcon.PLUS));
        SubMenu subMenu = item.getSubMenu();
        subMenu.addItem("Цифровая печать", menuItemClickEvent -> tabSheet.add(createTab("Цифровая печать"),
                new VerticalLayout()));
        subMenu.addItem("Резка", menuItemClickEvent -> tabSheet.add(createTab("Резка"), new VerticalLayout()));
        subMenu.addItem("Верстка", menuItemClickEvent -> tabSheet.add(createTab("Верстка"), new VerticalLayout()));
        tabSheet.setPrefixComponent(menuBar);

        tabSheet.addSelectedChangeListener(selectedChangeEvent -> {
            Tab tabb = selectedChangeEvent.getPreviousTab();
            VerticalLayout vl = (VerticalLayout) selectedChangeEvent.getSource().getComponent(tabb);

            Notification.show("Надо проверить "+vl.getClass().toString());
        });
        vel.add(tabSheet);
        return vel;
    }

    private Tab createTab (String str){
        var tab = new Tab();
        var la = new HorizontalLayout();
        var vla = new VerticalLayout();
        vla.setAlignItems(FlexComponent.Alignment.CENTER);
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
