package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.data.service.StandartSizeService;
import ru.bprn.printhouse.data.service.VariablesForMainWorksService;
import ru.bprn.printhouse.views.MainLayout;
import ru.bprn.printhouse.views.material.service.PrintSheetsMaterialService;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.operation.service.OperationService;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.entity.TemplatesMenuItem;
import ru.bprn.printhouse.views.templates.service.AbstractProductService;
import ru.bprn.printhouse.views.templates.service.TemplatesMenuItemService;
import ru.bprn.printhouse.views.templates.service.TemplatesService;

import java.util.List;

@PageTitle("Редактирование шаблонов")
@Route(value = "templates", layout = MainLayout.class)
@AnonymousAllowed
public class TemplatesView extends SplitLayout {

    private final UniversalEditorFactory universalEditorFactory;
    private final TemplatesService templatesService;
    private final AbstractProductService abstractProductService;
    private final OperationService operationService;
    private final PrintSheetsMaterialService printSheetsMaterialService;
    private final TemplatesMenuItemService menuItemService;

    private final BeanValidationBinder<Templates> templatesBinder;

    private final TreeGrid<Object> treeGrid = new TreeGrid<>();
    private final TextField filterField = new TextField();

    private Templates currentTemplate = null;
    private AbstractProductType currentProductType = null;

    private Object selectedRow = null;
    private final ConfirmDialog confirmDeleteDialog;
    private Object objToCopy = null;

    public TemplatesView(TemplatesService templatesService, AbstractProductService abstractProductService,
                         OperationService operationService, PrintSheetsMaterialService printSheetsMaterialService,
                         FormulasService formulasService, VariablesForMainWorksService variablesForMainWorksService,
                         StandartSizeService standartSizeService, TemplatesMenuItemService menuItemService){

        this.templatesService = templatesService;
        this.abstractProductService = abstractProductService;
        this.operationService = operationService;
        this.printSheetsMaterialService = printSheetsMaterialService;
        this.menuItemService = menuItemService;

        this.universalEditorFactory = new UniversalEditorFactory(
                printSheetsMaterialService, formulasService, variablesForMainWorksService, standartSizeService);

        templatesBinder = new BeanValidationBinder<>(Templates.class);
        templatesBinder.setChangeDetectionEnabled(true);

        confirmDeleteDialog = new ConfirmDialog("Внимание!" , "", "Да",
                confirmEvent -> {
                Object obj = treeGrid.asSingleSelect().getValue();
                Object parent;
                switch (obj) {
                    case Templates templates -> parent = null;
                    case AbstractProductType product -> parent = treeGrid.getTreeData().getParent(product);
                    case Operation operation -> parent  = treeGrid.getTreeData().getParent(operation);
                    default -> parent = null;
                }
                    deleteElement(obj, parent);
                    Notification.show("Элемент удален!");
                },
                "Нет", cancelEvent -> cancelEvent.getSource().close());

        this.setOrientation(Orientation.HORIZONTAL);
        this.setSplitterPosition(60.0);
        this.setSizeFull();
        this.addToPrimary((treeGrid()));
        this.addToSecondary(new VerticalLayout());

    }

    private Component treeGrid() {
        var hlay = new HorizontalLayout();
        hlay.setWidthFull();
        treeGrid.setWidthFull();

        var vl = new VerticalLayout();
        vl.setWidthFull();

        filterField.setWidth("50%");
        filterField.setPlaceholder("Поиск");
        filterField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        filterField.setValueChangeMode(ValueChangeMode.LAZY);
        filterField.addValueChangeListener(e -> populate(e.getValue().trim()));
        filterField.setClearButtonVisible(true);

        vl.add(filterField, createMenuBar(), treeGrid);

        treeGrid.addComponentHierarchyColumn(item -> {
            HorizontalLayout layout = new HorizontalLayout();
            layout.setAlignItems(FlexComponent.Alignment.CENTER);

            Component content = switch (item) {
                case Templates t -> {
                    Icon icon = VaadinIcon.FOLDER_OPEN.create();
                    Span label = new Span(t.getName());
                    label.getStyle().set("color", "darkblue");
                    yield new HorizontalLayout(icon, label);
                }
                case AbstractProductType pt -> {
                    Icon icon = VaadinIcon.CUBE.create();
                    Span label = new Span(pt.getName());
                    label.getStyle().set("color", "green");
                    yield new HorizontalLayout(icon, label);
                }
                case Operation op -> {
                    Icon icon = VaadinIcon.COG.create();
                    Span label = new Span(op.getName());
                    label.getStyle().set("color", "darkred");
                    yield new HorizontalLayout(icon, label);
                }
                default -> {
                    Icon icon = VaadinIcon.QUESTION.create();
                    Span label = new Span(item.toString());
                    label.getStyle().set("color", "gray");
                    yield new HorizontalLayout(icon, label);
                }
            };
            layout.add(content);

            ContextMenu menu = new ContextMenu();
            menu.setTarget(layout);
            menu.setOpenOnClick(false); // открытие по правому клику

            menu.addItem("🆕 Новый", e -> {
                Dialog dialog = new Dialog();
                dialog.add(new Span("Создать новый элемент?"));
                Button confirm = new Button("Создать", click -> {
                    Notification.show("Создан новый элемент");
                    dialog.close();
                });
                Button cancel = new Button("Отмена", click -> dialog.close());
                dialog.add(new HorizontalLayout(confirm, cancel));
                dialog.open();
            });

            menu.addItem("📄 Дублировать", e -> {
                Dialog dialog = new Dialog();
                dialog.add(new Span("Дублировать: " + item));
                Button confirm = new Button("Дублировать", click -> {
                    Notification.show("Дублирован: " + item);
                    dialog.close();
                });
                Button cancel = new Button("Отмена", click -> dialog.close());
                dialog.add(new HorizontalLayout(confirm, cancel));
                dialog.open();
            });

            menu.addItem("🗑️ Удалить", e -> {
                Dialog dialog = new Dialog();
                dialog.add(new Span("Удалить: " + item + "?"));
                Button confirm = new Button("Удалить", click -> {

                });
                Button cancel = new Button("Отмена", click -> dialog.close());
                dialog.add(new HorizontalLayout(confirm, cancel));
                dialog.open();
            });
            return layout;
        }).setHeader("Элементы шаблонов");

        treeGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        treeGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        populate(filterField.getValue().trim());

        treeGrid.asSingleSelect().addValueChangeListener(e->{
            selectedRow = e.getValue();
            setCurrent(selectedRow);
            if (selectedRow!= null) addEditor(universalEditorFactory.createEditor(selectedRow, this::save));
            else if (this.getSecondaryComponent()!=null) this.remove(this.getSecondaryComponent());
        });

        hlay.add(vl);
        return hlay;
    }

    // Меню в шапке TemplateView, создание компонентов и операций добавляются автоматически на основании @MenuItem(context = "product") у класса-сущности
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_ICON);

        var create = menuBar.addItem(VaadinIcon.PLUS.create(), "Создать");
        var createSubMenu = create.getSubMenu();
        createSubMenu.addItem("Создать новый шаблон", e->addEditor(universalEditorFactory.createEditor(new Templates(), this::save)));
        var components = createSubMenu.addItem("Создать новый компонент");
        addComponentsToSubMenu(components.getSubMenu(), "product");

        var operations = createSubMenu.addItem("Создать новую работу");
        addComponentsToSubMenu(operations.getSubMenu(), "operation");

        menuBar.addItem(VaadinIcon.MENU.create(), "Дублировать", e->{
            Object obj = treeGrid.asSingleSelect().getValue();
            Object parent = this.treeGrid.getTreeData().getParent(obj);
            paste(obj, parent);
        });

        menuBar.addItem(VaadinIcon.COPY.create(), "Копировать", e->{
            objToCopy = treeGrid.asSingleSelect().getValue();
        });
        menuBar.addItem(VaadinIcon.PASTE.create(), "Вставить", e->{
            if (objToCopy != null) {
                Object obj = treeGrid.asSingleSelect().getValue();
                switch (objToCopy) {
                    case Templates templates -> {
                        paste(objToCopy, null);
                        objToCopy = null;
                    }
                    case AbstractProductType abstractProductType when obj instanceof Templates -> {
                        paste(objToCopy, obj);
                        objToCopy = null;
                    }
                    case Operation operation when obj instanceof AbstractProductType -> {
                        paste(objToCopy, obj);
                        objToCopy = null;
                    }
                    case null, default -> Notification.show("Выберите правильный элемент для вставки скопированного");
                }
            }
        });

        menuBar.addItem(VaadinIcon.DEL.create(), "Удалить", e->{
            if (!treeGrid.asSingleSelect().isEmpty()) {
                confirmDeleteDialog.setText("Вы уверены, что хотите удалить " + treeGrid.asSingleSelect().getValue() + " ?");
                confirmDeleteDialog.open();
            }
        });

        return menuBar;
    }

    private void addComponentsToSubMenu(SubMenu menu, String context) {
        List<TemplatesMenuItem> list = menuItemService.getMenuByContext(context);
        if (list != null && !list.isEmpty())
            for (TemplatesMenuItem item : list) {
                Object obj = EntityFactory.createEntity(item.getClassName());
                menu.addItem(item.getName(), e-> addEditor(universalEditorFactory.createEditor(obj, this::save)));
            }
    }

    private void setCurrent(Object select) {
        if (select!=null) {
            var temp = treeGrid.getTreeData().getParent(select);
            if (temp == null) {
                currentTemplate = (Templates) select;
                currentProductType = null;
            } else {
                var other = treeGrid.getTreeData().getParent(temp);
                if (other == null) {
                    currentTemplate = (Templates) temp;
                    currentProductType = (AbstractProductType) select;
                } else {
                    currentTemplate = (Templates) other;
                    currentProductType = (AbstractProductType) temp;
                }
            }
        }
    }

    private void addEditor(AbstractEditor<?> editor) {
        var obj = this.getSecondaryComponent();
        if (obj != null) this.remove(obj);
        this.addToSecondary(editor);
    }

    private void save(Object object) {
        var note = templatesService.save(object, currentTemplate);
        Notification.show(note);
        populate(filterField.getValue().trim());
        treeGrid.asSingleSelect().setValue(selectedRow);
    }


    private void deleteElement(Object object, Object parent){
        var note = templatesService.delete(object, parent);
        Notification.show(note);
        populate(filterField.getValue().trim());
    }

    private void paste(Object obj, Object parent) {
        if (obj!=null){
            switch (obj) {
                case Templates template-> templatesService.duplicateTemplate(template);
                case AbstractProductType productType-> templatesService.addProductToTemplate((Templates) parent, templatesService.duplicateProduct(productType));
                case Operation operation-> templatesService.duplicateOperation((AbstractProductType) parent, operation);
                default->{}
            }
            populate(filterField.getValue().trim());
            treeGrid.asSingleSelect().setValue(obj);
            treeGrid.expand(obj);
        }
        else Notification.show("Сначала выделите какой-либо элемент таблицы");
    }

    private void populate(String filter) {
        treeGrid.setDataProvider(templatesService.populateGrid(filter));
    }

    public static class EntityFactory {

        public static Object createEntity(String fullClassName) {
            try {
                Class<?> clazz = Class.forName(fullClassName);
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Не удалось создать экземпляр " + fullClassName, e);
            }
        }
    }
}
