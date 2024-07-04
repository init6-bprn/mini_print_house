package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ru.bprn.printhouse.data.entity.WorkFlow;
import ru.bprn.printhouse.data.service.*;
import ru.bprn.printhouse.views.MainLayout;

import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.Optional;

@PageTitle("Создание и редактирование рабочих цепочек (WorkFlow)")
@Route(value = "workflows", layout = MainLayout.class)
@AnonymousAllowed
public class WorkFlowView extends SplitLayout {

    private final PrintMashineService printMashineService;
    private final MaterialService materialService;
    private final StandartSizeService standartSizeService;
    private final TypeOfMaterialService typeOfMaterialService;
    private final GapService gapService;
    private final WorkFlowService workFlowService;
    private final ImposeCaseService imposeCaseService;
    private final TabSheet tabSheet = new TabSheet();
    private WorkFlow workFlow;
    private StartTabOfWorkFlowVerticalLayout startTab;

    private Grid<WorkFlow> templateGrid = new Grid<>(WorkFlow.class, false);

    public WorkFlowView(PrintMashineService printMashineService, StandartSizeService standartSizeService, TypeOfMaterialService typeOfMaterialService, MaterialService materialService, GapService gapService,
                        WorkFlowService workFlowService, ImposeCaseService imposeCaseService){

        this.printMashineService = printMashineService;
        this.materialService = materialService;
        this.standartSizeService = standartSizeService;
        this.typeOfMaterialService = typeOfMaterialService;
        this.gapService = gapService;
        this.workFlowService = workFlowService;
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
                "Да", confirmEvent -> {if (workFlow !=null) {
            workFlowService.delete(workFlow);
                        templateGrid.getListDataView().removeItem(workFlow);
                }},
                "Нет", cancelEvent -> cancelEvent.getSource().close());

        var hl = new HorizontalLayout();
        var createButton = new Button(VaadinIcon.PLUS.create(), buttonClickEvent -> {
            this.getPrimaryComponent().setVisible(false);
            this.getSecondaryComponent().getElement().setEnabled(true);
            this.setSplitterPosition(0);
            workFlow = new WorkFlow();
            startTab.getTemplateBinder().setBean(workFlow);
        });
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var updateButton  = new Button(VaadinIcon.EDIT.create(), buttonClickEvent -> {
            var optTemp = templateGrid.getSelectedItems().stream().findFirst();
            if (optTemp.isPresent()) {
                workFlow = optTemp.get();
                startTab.getTemplateBinder().setBean(workFlow);
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
                workFlow = new WorkFlow();
                workFlow.setName(ishueTemplate.getName());
                workFlow.setStandartSize(ishueTemplate.getStandartSize());
                workFlow.setSizeX(ishueTemplate.getSizeX());
                workFlow.setSizeY(ishueTemplate.getSizeY());
                workFlow.setImposeCase(ishueTemplate.getImposeCase());
                workFlow.setMaterial(ishueTemplate.getMaterial());
                workFlow.setGap(ishueTemplate.getGap());
                workFlow.setQuantityOfLeaves(ishueTemplate.getQuantityOfLeaves());

                startTab.getTemplateBinder().setBean(workFlow);
                this.getPrimaryComponent().setVisible(false);
                this.getSecondaryComponent().getElement().setEnabled(true);
                this.setSplitterPosition(0);
            }
        });
        duplicateButton.addThemeVariants(ButtonVariant.LUMO_ICON);

        var deleteButton = new Button(VaadinIcon.CLOSE.create(), buttonClickEvent -> {
            var optTemp = templateGrid.getSelectedItems().stream().findFirst();
            if (optTemp.isPresent()) {
                workFlow = optTemp.get();
                dialog.open();
            }
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);


        hl.add(createButton, updateButton, duplicateButton, deleteButton);
        vl.add(hl);

        templateGrid.addColumn(WorkFlow::getName).setHeader("Имя");
        templateGrid.addColumn(WorkFlow::getStandartSize).setHeader("Размер");
        templateGrid.addColumn(WorkFlow::getMaterial).setHeader("Материал");

        templateGrid.setItems(this.workFlowService.findAll());
        templateGrid.setHeight("200px");
        templateGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        templateGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        vl.add(templateGrid);

        templateGrid.addSelectionListener(selectionEvent -> {
            if (selectionEvent.getFirstSelectedItem().isPresent())
                     workFlow = selectionEvent.getFirstSelectedItem().get();
        });

        templateGrid.addItemDoubleClickListener(__->updateButton.click());
        return vl;
    }

    private VerticalLayout addTabSheetSection(){

        var confirmButton = new Button("Сохранить");
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        confirmButton.addClickListener(this::validateAndSaveBean);

        var cancelButton = new Button("Отмена");
        cancelButton.addClickListener(buttonClickEvent -> {
           startTab.getTemplateBinder().removeBean();
           this.getPrimaryComponent().setVisible(true);
           this.getSecondaryComponent().getElement().setEnabled(false);
           this.setSplitterPosition(35.0);
        });


        tabSheet.setSuffixComponent(new HorizontalLayout(cancelButton,confirmButton));
        var vel = new VerticalLayout();
        tabSheet.setWidthFull();
        startTab = new StartTabOfWorkFlowVerticalLayout(standartSizeService,
                                                      typeOfMaterialService, materialService, gapService, imposeCaseService);

        tabSheet.add("Настройки", startTab);

        MenuBar menuBar = new MenuBar();
        MenuItem item = menuBar.addItem(new Icon(VaadinIcon.PLUS));
        SubMenu subMenu = item.getSubMenu();
        subMenu.addItem("Цифровая печать", menuItemClickEvent -> tabSheet.add(createTab("Цифровая печать"),
                new PrintingTabOfWorkFlowVerticalLayout(printMashineService)));
        subMenu.addItem("Резка", menuItemClickEvent -> tabSheet.add(createTab("Резка"), new VerticalLayout()));
        subMenu.addItem("Верстка", menuItemClickEvent -> tabSheet.add(createTab("Верстка"), new VerticalLayout()));
        tabSheet.setPrefixComponent(menuBar);

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

    private void validateAndSaveBean(ClickEvent<Button> e) {
        HasBinder hb;
        boolean flag = false;
        Optional<Component> component =tabSheet.getChildren().filter(Tabs.class::isInstance).findFirst();

        if (component.isPresent()) {
            var tabs = (Tabs) component.get();
            List<Component> list = tabs.getChildren().filter(Tab.class::isInstance).toList();

            if (!list.isEmpty()) {
                for (Component comp : list) {
                    hb = (HasBinder) tabSheet.getComponent((Tab) comp);
                    if (!hb.isValid()) {
                        Notification.show("Заполните все требуемые поля!");
                        tabSheet.setSelectedTab((Tab) comp);
                        flag = true;
                    }
                    if (flag) break;
                }

                if (!flag) {
                    workFlowService.save(startTab.getTemplateBinder().getBean());
                    this.getPrimaryComponent().setVisible(true);
                    this.getSecondaryComponent().getElement().setEnabled(false);
                    this.setSplitterPosition(35.0);
                    this.templateGrid.setItems(workFlowService.findAll());
                    Notification.show("Мимо!!");
                }
            }
        }
    }

}
