package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
import ru.bprn.printhouse.views.templates.entity.OneSheetDigitalPrintingProductType;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.entity.TemplatesMenuItem;
import ru.bprn.printhouse.views.templates.service.AbstractProductService;
import ru.bprn.printhouse.views.templates.service.TemplatesMenuItemService;
import ru.bprn.printhouse.views.templates.service.TemplatesService;

import java.util.function.Consumer;

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

        var confirmDeleteDialog = new ConfirmDialog("Внимание!" , "", "Да",
                confirmEvent -> {
                    deleteElement(treeGrid.asSingleSelect().getValue());
                    Notification.show("Элемент удален!");
                },
                "Нет", cancelEvent -> cancelEvent.getSource().close());

        var hl = new HorizontalLayout();
        var createTemplateButton = new Button(VaadinIcon.PLUS.create(), event ->{
            addEditor(universalEditorFactory.createEditor(new Templates(), this::save));
        });

        createTemplateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createTemplateButton.setTooltipText("Создать новый шаблон");

        var createButton = new Button(VaadinIcon.PLUS.create(), event -> {
            switch (treeGrid.asSingleSelect().getValue()){
                case Templates templates: addEditor(universalEditorFactory.createEditor(new OneSheetDigitalPrintingProductType(), this::save));
                    break;
                case AbstractProductType productType:
                    addEditor(universalEditorFactory.createEditor(productType, this::save));
                    break;
                case Operation operation:
                    addEditor(universalEditorFactory.createEditor(operation, this::save));
                    break;
                default: Notification.show("Неизвестный тип !!!");
            }
        });
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.setTooltipText("Создать новую рабочую цепочку");

        var duplicateButton = new Button(VaadinIcon.COPY_O.create(), event -> {
            var obj = treeGrid.asSingleSelect().getValue();
            Object parent = this.treeGrid.getTreeData().getParent(obj);
            if (obj!=null){
                switch (obj) {
                    case Templates template: templatesService.duplicateTemplate(template);
                        break;
                    case AbstractProductType productType:
                        templatesService.addProductToTemplate((Templates) parent, templatesService.duplicateProduct(productType));
                        break;
                    case Operation operation: templatesService.duplicateOperation((AbstractProductType) parent, operation);
                    default:
                }
                populate(filterField.getValue().trim());
                treeGrid.asSingleSelect().setValue(obj);
            }
            else Notification.show("Сначала выделите какой-либо элемент таблицы");
        });
        duplicateButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        duplicateButton.setTooltipText("Создать дубликат шаблона/цепочки");


        var deleteButton = new Button(VaadinIcon.CLOSE.create(), event -> {
            if (!treeGrid.asSingleSelect().isEmpty()) {
                confirmDeleteDialog.setText("Вы уверены, что хотите удалить " + treeGrid.asSingleSelect().getValue() + " ?");
                confirmDeleteDialog.open();
            }
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.setTooltipText("Удалить элемент");

        hl.add(createTemplateButton, createButton, duplicateButton, deleteButton);
        vl.add(filterField, hl, treeGrid);

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
                    deleteElement(item);
                    Notification.show("Удалён: " + item);
                    dialog.close();
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
        switch (object) {
            case Templates templates-> templatesService.save(templates);
            case AbstractProductType productType -> templatesService.addProductToTemplateAndSaveAll(currentTemplate, productType);
            case Operation operation -> operationService.save(operation);
            default -> Notification.show("Не сохранено");
        }
        populate(filterField.getValue().trim());
        treeGrid.asSingleSelect().setValue(selectedRow);
    }


    private void deleteElement(Object abstractTemplate){
        switch (abstractTemplate) {
            case Templates template -> templatesService.delete(template);
            case AbstractProductType entity -> abstractProductService.delete(entity);
            case Operation operation -> operationService.delete(operation);
            default -> Notification.show("Не знаю, что удалить!");
        }
        populate(filterField.getValue().trim());
    }

    private void populate(String filter) {
        treeGrid.setDataProvider(templatesService.populateGrid(filter));
    }

    public static class ProductTypeFactory {

        public static AbstractProductType createInstance(String className) {
            try {
                Class<?> clazz = Class.forName(className);
                if (!AbstractProductType.class.isAssignableFrom(clazz)) {
                    throw new IllegalArgumentException("Класс не является наследником AbstractProductType");
                }
                return (AbstractProductType) clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Ошибка при создании экземпляра: " + className, e);
            }
        }
    }

    public MenuItem addToContextMenu(ContextMenu menu, TemplatesMenuItem item, UniversalEditorFactory factory, Consumer<Object> saveCallback) {
        return menu.addItem(item.getName(), e -> {
            try {
                AbstractProductType product = ProductTypeFactory.createInstance(item.getClassName());
                factory.createEditor(product, saveCallback);
            } catch (Exception ex) {
                Notification.show("Ошибка: " + ex.getMessage());
            }
        });
    }
}
