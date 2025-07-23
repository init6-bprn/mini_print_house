package ru.bprn.printhouse.views.templates;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import com.vaadin.flow.component.Component;
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
import ru.bprn.printhouse.data.entity.*;
import ru.bprn.printhouse.data.service.*;
import ru.bprn.printhouse.views.MainLayout;
import ru.bprn.printhouse.views.additionalWorks.entity.AdditionalWorksBean;
import ru.bprn.printhouse.views.additionalWorks.entity.TypeOfWorks;
import ru.bprn.printhouse.views.additionalWorks.service.AdditionalWorksBeanService;
import ru.bprn.printhouse.views.additionalWorks.service.TypeOfWorksService;

import java.io.IOException;
import java.util.*;

@PageTitle("Создание и редактирование рабочих цепочек (WorkFlow)")
@Route(value = "workflows", layout = MainLayout.class)
@AnonymousAllowed
public class WorkFlowView extends SplitLayout {

    private final DigitalPrintingMachineService printMashineService;
    private final StandartSizeService standartSizeService;
    private final GapService gapService;
    private final WorkFlowService workFlowService;
    private final CostOfPrintSizeLeafAndColorService costOfPrintSizeLeafAndColorService;
    private final FormulasService formulasService;
    private final VariablesForMainWorksService variablesForMainWorksService;
    private final TypeOfWorksService typeOfWorksService;
    private final AdditionalWorksBeanService worksBeanService;

    private final TabSheet tabSheet = new TabSheet();
    private StartTabOfWorkFlowVerticalLayout startTab;
    private ComputeAnyElementsDialog calc;

    private final Grid<WorkFlow> templateGrid = new Grid<>(WorkFlow.class, false);

    public WorkFlowView(DigitalPrintingMachineService printMashineService,
                        StandartSizeService standartSizeService,
                        GapService gapService,
                        WorkFlowService workFlowService,
                        CostOfPrintSizeLeafAndColorService costOfPrintSizeLeafAndColorService,
                        FormulasService formulasService,
                        VariablesForMainWorksService variablesForMainWorksService,
                        TypeOfWorksService typeOfWorksService,
                        AdditionalWorksBeanService worksBeanService){

        this.printMashineService = printMashineService;
        this.standartSizeService = standartSizeService;
        this.gapService = gapService;
        this.workFlowService = workFlowService;
        this.costOfPrintSizeLeafAndColorService = costOfPrintSizeLeafAndColorService;
        this.formulasService = formulasService;
        this.variablesForMainWorksService = variablesForMainWorksService;
        this.typeOfWorksService = typeOfWorksService;
        this.worksBeanService = worksBeanService;

        startTab = new StartTabOfWorkFlowVerticalLayout();

        calc = new ComputeAnyElementsDialog(workFlowService);

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

        var dialog = new ConfirmDialog("Внимание!" , "Вы уверены, что хотите удалить этот workflow?", "Да", confirmEvent ->
            {
                var workflw = templateGrid.getSelectedItems().stream().findFirst();
                if (workflw.isPresent()) {
                        templateGrid.getListDataView().removeItem(workflw.get());
                        workFlowService.delete(workflw.get());
                }
            },
            "Нет", cancelEvent -> cancelEvent.getSource().close());

        var hl = new HorizontalLayout();
        var createButton = new Button(VaadinIcon.PLUS.create(), event -> {
            this.getPrimaryComponent().setVisible(false);
            this.getSecondaryComponent().getElement().setEnabled(true);
            this.setSplitterPosition(0);
            startTab.getTemplateBinder().setBean(new WorkFlow());
            //calc.setTemplateBinder(startTab.getTemplateBinder());
        });
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var updateButton  = new Button(VaadinIcon.EDIT.create(), event -> {
            var optTemp = templateGrid.getSelectedItems().stream().findFirst();
            if (optTemp.isPresent()) {
                startTab.getTemplateBinder().setBean(optTemp.get());
                //calc.setTemplateBinder(startTab.getTemplateBinder());
                populateTabSheet(JSONToObjectsHelper.getListOfObjects(optTemp.get().getStrJSON()));

                this.getPrimaryComponent().setVisible(false);
                this.getSecondaryComponent().getElement().setEnabled(true);
                this.setSplitterPosition(0);
            }
        });
        updateButton.addThemeVariants(ButtonVariant.LUMO_ICON);

        var duplicateButton = new Button(VaadinIcon.COPY_O.create(), event -> {
            var optTemp = templateGrid.getSelectedItems().stream().findFirst();
            if (optTemp.isPresent()) {
                var issueTemplate = optTemp.get();
                var workFlow = new WorkFlow();
                var id = workFlow.getId();
                var name = workFlow.getName();
                var objMapper = new ObjectMapper();
                TokenBuffer tb = new TokenBuffer(objMapper, false);
                try {
                    objMapper.writeValue(tb, issueTemplate);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    workFlow = objMapper.readValue(tb.asParser(), WorkFlow.class);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                workFlow.setId(id);
                workFlow.setName(name);

                startTab.getTemplateBinder().setBean(workFlow);
                //calc.setTemplateBinder(startTab.getTemplateBinder());
                populateTabSheet(JSONToObjectsHelper.getListOfObjects(optTemp.get().getStrJSON()));

                this.getPrimaryComponent().setVisible(false);
                this.getSecondaryComponent().getElement().setEnabled(true);
                this.setSplitterPosition(0);
            }
        });
        duplicateButton.addThemeVariants(ButtonVariant.LUMO_ICON);

        var deleteButton = new Button(VaadinIcon.CLOSE.create(), event -> {
            var optTemp = templateGrid.getSelectedItems().stream().findFirst();
            if (optTemp.isPresent()) {
                dialog.open();
            }
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);


        hl.add(createButton, updateButton, duplicateButton, deleteButton);
        vl.add(hl);

        templateGrid.addColumn(WorkFlow::getName).setHeader("Имя");

        templateGrid.setItems(this.workFlowService.findAll());
        templateGrid.setHeight("200px");
        templateGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        templateGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        vl.add(templateGrid);

        templateGrid.addItemClickListener(workFlowItemClickEvent ->{
           startTab.getTemplateBinder().setBean(workFlowItemClickEvent.getItem());
           //populateTabSheet(getParseStringMap(workFlowItemClickEvent.getItem().getStrJSON()));
        });

        templateGrid.addItemDoubleClickListener(__->updateButton.click());
        return vl;
    }

    private void removeTabs(){
        var list = getListOfTabs();
        if (list.isPresent())
            for (Component comp: list.get())
                if (!(tabSheet.getComponent((Tab) comp) instanceof StartTabOfWorkFlowVerticalLayout))
                    tabSheet.remove((Tab) comp);
    }

    public Optional<List<Component>> getListOfTabs(){
        Optional<Component> component = tabSheet.getChildren().filter(Tabs.class::isInstance).findFirst();
        return component.map(value -> value.getChildren().filter(Tab.class::isInstance).toList());
    }

    private VerticalLayout addTabSheetSection(){

        var confirmButton = new Button("Сохранить");
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        confirmButton.addClickListener(e->{
            var optList = validateBean();
            if (optList.isPresent()) {
                saveBean(optList.get());
                removeTabs();
                this.getPrimaryComponent().setVisible(true);
                this.getSecondaryComponent().getElement().setEnabled(false);
                this.setSplitterPosition(35.0);
                this.templateGrid.setItems(workFlowService.findAll());
            };
        });

        var cancelButton = new Button("Отмена");
        cancelButton.addClickListener(buttonClickEvent -> {
           removeTabs();
           this.getPrimaryComponent().setVisible(true);
           this.getSecondaryComponent().getElement().setEnabled(false);
           this.setSplitterPosition(35.0);
        });

        var calculateButton = new Button("Calculate!");
        calculateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        calculateButton.addClickListener(buttonClickEvent -> {
            var valid = validateBean();
            if (valid.isPresent()) {
                calc.setWorkFlow(startTab.getTemplateBinder().getBean());
                calc.open();
                //workFlowService.calculate(startTab.getTemplateBinder().getBean());
            }
        });


        tabSheet.setSuffixComponent(new HorizontalLayout(cancelButton, calculateButton, confirmButton));
        var vel = new VerticalLayout();
        tabSheet.setWidthFull();
        tabSheet.add("Настройки", startTab);

        MenuBar menuBar = new MenuBar();
        MenuItem item = menuBar.addItem(new Icon(VaadinIcon.PLUS));
        SubMenu subMenu = item.getSubMenu();
        MenuItem oneSheet = subMenu.addItem("Однолистовая печать");
        SubMenu oneSheetSubMenu = oneSheet.getSubMenu();
        oneSheetSubMenu.addItem("Однолистовая цифровая печать", menuItemClickEvent -> {
                var digitalPrinting = new PrintingTabOfWorkFlowVerticalLayout(printMashineService,
                        costOfPrintSizeLeafAndColorService, formulasService, standartSizeService,gapService, variablesForMainWorksService, typeOfWorksService);
                var dp = new DigitalPrinting();
                dp.setVariables(populateVariables(dp.getClass().getSimpleName()));
                digitalPrinting.getTemplateBinder().setBean(dp);
                tabSheet.add(createTab("Однолистовая цифровая печать"), digitalPrinting);
                addDescriptionToName("Однолистовая цифровая печать", DigitalPrinting.class.getSimpleName());

            }
        );

        for (TypeOfWorks tow : typeOfWorksService.findAll()) {
            // Узел меню доп.работ
            MenuItem item1 = subMenu.addItem(tow.getName());
            SubMenu subMenu1 = item1.getSubMenu();

            for (AdditionalWorksBean work : worksBeanService.findAllByType(tow)) {
                // Пункты доп.работ
                subMenu1.addItem(work.getName(), menuItemClickEvent -> {
                    tabSheet.add(createTab(work.getName()), new AdditionalWorksLayout(work, worksBeanService));
                    addDescriptionToName(work.getName(), "Cutting");
                });
            }
        }

        tabSheet.setPrefixComponent(menuBar);

        vel.add(tabSheet);
        return vel;
    }

    private void addDescriptionToName (String str, String type) {
        var bean = startTab.getTemplateBinder().getBean();
        if (startTab.getAutoNamed().getValue()) {
            bean.setName(bean.getName() + "-"+ str);
            startTab.getTemplateBinder().refreshFields();
        }
        bean.setType(type);
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
                if (i > 1) {
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
                int j = tabs.getTabCount();
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

    private void saveBean(ArrayList<String[]> str){
        var wf = startTab.getTemplateBinder().getBean();
        wf.setStrJSON(JSONToObjectsHelper.unionAllToOneString(str));
        workFlowService.save(wf);
    }

    private Optional<ArrayList<String[]>> validateBean() {
        var flag = true;
        var list = getListOfComponents(HasBinder.class);
        var listWorkflow = new ArrayList<String[]>();
        for (Component comp : list) {
            HasBinder hb = (HasBinder) comp;
            if (!hb.isValid()) {
                Notification.show("Заполните все требуемые поля!");
                tabSheet.setSelectedTab(tabSheet.getTab(comp));
                listWorkflow.clear();
                flag = false;
                break;
            } else if (!(comp instanceof StartTabOfWorkFlowVerticalLayout))
                       listWorkflow.add(hb.getBeanAsString());
        }
        if (flag) return Optional.of(listWorkflow);
        else return Optional.empty();
    }

    public List<Component> getListOfComponents(Class<?> clazz) {
        var listWorkflow = new ArrayList<Component>();
        var list = getListOfTabs();
        if (list.isPresent()) {
            for (Component comp : list.get()) {
                Component layout = tabSheet.getComponent((Tab) comp);
                if (Arrays.stream(layout.getClass().getInterfaces()).toList().contains(clazz)) {
                    listWorkflow.add(layout);
                }
            }
        }
        return listWorkflow;
    }

    private void populateTabSheet(List<Object> list) {
        for (Object obj: list) {
            switch (obj) {
                case DigitalPrinting dp -> {
                    var tabComp = new PrintingTabOfWorkFlowVerticalLayout(printMashineService,
                            costOfPrintSizeLeafAndColorService, formulasService, standartSizeService, gapService, variablesForMainWorksService, typeOfWorksService);
                    //dp.setVariables(populateVariables(dp.getClass().getSimpleName()));
                    tabComp.getTemplateBinder().setBean(dp);
                    tabSheet.add(createTab("Цифровая печать"), tabComp);
                }
                case AdditionalWorksBean wb ->  tabSheet.add(createTab(wb.getName()), new AdditionalWorksLayout(wb, worksBeanService));
                default -> throw new IllegalStateException("Unexpected value: " + obj);
            }
        }

    }

    private Map<String, Number> populateVariables(String name) {
        Map<String, Number> map = new HashMap<>();
        for (VariablesForMainWorks variables: variablesForMainWorksService.findAllClazz(name))
                map.put(variables.getName(), 1d);
        return map;
    }

}
