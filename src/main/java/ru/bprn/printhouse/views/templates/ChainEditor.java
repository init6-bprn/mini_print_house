package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import lombok.Setter;
import ru.bprn.printhouse.data.entity.AdditionalWorksBean;
import ru.bprn.printhouse.data.entity.TypeOfWorks;
import ru.bprn.printhouse.data.entity.VariablesForMainWorks;
import ru.bprn.printhouse.data.service.*;
import ru.bprn.printhouse.views.templates.entity.AbstractTemplate;
import ru.bprn.printhouse.views.templates.entity.Chains;
import ru.bprn.printhouse.views.templates.entity.OneSheetPrinting;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.service.ChainsService;
import ru.bprn.printhouse.views.templates.service.TemplatesService;

import java.util.*;

public class ChainEditor extends VerticalLayout {

    private final TabSheet tabSheet = new TabSheet();
    private Chains chains;

    @Setter
    private Templates template;
    private final BeanValidationBinder<Chains> chainsBinder = new BeanValidationBinder<>(Chains.class);
    private final ChainsService service;
    private final TemplatesService templatesService;
    private final TreeGrid<AbstractTemplate> treeGrid;
    private final SplitLayout splitLayout;
    private final TypeOfWorksService typeOfWorksService;
    private final AdditionalWorksBeanService worksBeanService;
    private final VariablesForMainWorksService variablesForMainWorksService;
    private final FormulasService formulasService;
    private final StandartSizeService standartSizeService;
    private final GapService gapService;
    private final MaterialService materialService;

    public ChainEditor(SplitLayout splitLayout, TreeGrid<AbstractTemplate> treeGrid, ChainsService service, TemplatesService templatesService,
                       TypeOfWorksService typeOfWorksService, AdditionalWorksBeanService worksBeanService,
                       VariablesForMainWorksService variablesForMainWorksService, FormulasService formulasService,
                       StandartSizeService standartSizeService, GapService gapService,
                       MaterialService materialService){
        this.templatesService = templatesService;
        this.service = service;
        this.treeGrid = treeGrid;
        this.splitLayout = splitLayout;
        this.typeOfWorksService = typeOfWorksService;
        this.worksBeanService = worksBeanService;
        this.variablesForMainWorksService = variablesForMainWorksService;
        this.formulasService = formulasService;
        this.standartSizeService = standartSizeService;
        this.gapService = gapService;
        this.materialService = materialService;
        this.setSizeFull();

        var name = new TextField("Название цепочки:");
        name.setWidthFull();
        this.chainsBinder.bind(name, Chains::getName, Chains::setName);

        var saveButton = new Button("Save", o -> saveBean());
        var cancelButton = new Button("Cancel", o ->cancelBean());
        var hl = new HorizontalLayout(FlexComponent.Alignment.END, saveButton, cancelButton);

        addTabSheetSection();
        this.add(name, hl, tabSheet);
    }

    private void saveBean() {
        var optList = validateBean();
        optList.ifPresent(strings -> chainsBinder.getBean().setStrJSON(JSONToObjectsHelper.unionAllToOneString(strings)));
        if (chainsBinder.writeBeanIfValid(chains)) {
            service.save(chains);
            Notification.show("Цепочка сохранена!");

            var set = templatesService.getChainsForTemplate(template);
            boolean flag = true;
            for (Chains c : set)
                if (Objects.equals(c.getId(), chains.getId())) {
                    flag = false;
                    break;
                }
            if (flag) {
                set.add(chains);
                template.setChains(set);
                templatesService.save(template);
            }
            Notification.show("Шаблон сохранен!");
            removeTabs();
            showPrimary();
            treeGrid.setDataProvider(templatesService.populateGrid(null));
        }
    }

    private Optional<ArrayList<String[]>> validateBean() {
        var flag = true;
        var list = getListOfComponents(HasBinder.class);
        var listWorkflow = new ArrayList<String[]>();
        StringBuilder str = new StringBuilder();
        for (Component comp : list) {
            HasBinder hb = (HasBinder) comp;
            if (!hb.isValid()) {
                Notification.show("Заполните все требуемые поля!");
                tabSheet.setSelectedTab(tabSheet.getTab(comp));
                listWorkflow.clear();
                flag = false;
                break;
            } else {
                str.append(hb.getDescription()).append(" ");
                listWorkflow.add(hb.getBeanAsString());
            }
        }
        if (flag) {
            chainsBinder.getBean().setName(str.toString().trim());
            chainsBinder.refreshFields();
            return Optional.of(listWorkflow);
        }
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

    public Optional<List<Component>> getListOfTabs(){
        Optional<Component> component = tabSheet.getChildren().filter(Tabs.class::isInstance).findFirst();
        return component.map(value -> value.getChildren().filter(Tab.class::isInstance).toList());
    }

    private void cancelBean(){
        removeTabs();
        showPrimary();
    }

    private void showPrimary(){
        splitLayout.getPrimaryComponent().setVisible(true);
        splitLayout.getSecondaryComponent().getElement().setEnabled(false);
        splitLayout.setSplitterPosition(50);
    }

    public void setChains(Chains chains) {
        chainsBinder.removeBean();
        chainsBinder.refreshFields();
        this.chains = chains;
        chainsBinder.setBean(this.chains);
        populateTabSheet(JSONToObjectsHelper.getListOfObjects(this.chainsBinder.getBean().getStrJSON()));
    }

    private void addTabSheetSection(){
        var vel = new VerticalLayout();
        tabSheet.setWidthFull();

        MenuBar menuBar = new MenuBar();
        MenuItem item = menuBar.addItem(new Icon(VaadinIcon.PLUS));
        SubMenu subMenu = item.getSubMenu();
        MenuItem oneSheet = subMenu.addItem("Однолистовая печать");
        SubMenu oneSheetSubMenu = oneSheet.getSubMenu();
        oneSheetSubMenu.addItem("Однолистовая печать", menuItemClickEvent -> {
                    var oneSheetPrintingLayout = new OneSheetPrintingVerticalLayout(formulasService,
                            standartSizeService, gapService, variablesForMainWorksService, materialService);
                    
                    var oneSheetPrinting = new OneSheetPrinting();
                    oneSheetPrinting.setVariables(populateVariables(oneSheetPrinting.getClass().getSimpleName()));
                    oneSheetPrintingLayout.getTemplateBinder().setBean(oneSheetPrinting);

                    tabSheet.add(createTab("Однолистовая печать"), oneSheetPrintingLayout);
                    addDescriptionToName("Однолистовая печать", OneSheetPrinting.class.getSimpleName());

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
    }

    public void removeTabs(){
        var list = getListOfTabs();
        if (list.isPresent())
            for (Component comp: list.get())
                if (!(tabSheet.getComponent((Tab) comp) instanceof StartTabOfWorkFlowVerticalLayout))
                    tabSheet.remove((Tab) comp);
    }

    private void addDescriptionToName (String str, String type) {
        /*
        var bean = startTab.getTemplateBinder().getBean();
        if (startTab.getAutoNamed().getValue()) {
            bean.setName(bean.getName() + "-"+ str);
            startTab.getTemplateBinder().refreshFields();
        }
        bean.setType(type);

         */
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

    private void populateTabSheet(List<Object> list) {
        for (Object obj: list) {
            switch (obj) {
                case OneSheetPrinting dp -> {
                    var tabComp = new OneSheetPrintingVerticalLayout(formulasService,
                            standartSizeService, gapService, variablesForMainWorksService, materialService);
                    tabComp.getTemplateBinder().setBean(dp);
                    tabSheet.add(createTab("Однолистовая печать"), tabComp);
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
