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
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.data.service.StandartSizeService;
import ru.bprn.printhouse.views.MainLayout;
import ru.bprn.printhouse.views.material.service.AbstractMaterialService;
import ru.bprn.printhouse.views.material.service.PrintSheetsMaterialService;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.operation.service.OperationService;
import ru.bprn.printhouse.views.operation.entity.ProductOperation;
import ru.bprn.printhouse.views.operation.entity.TypeOfOperation;
import ru.bprn.printhouse.views.operation.service.TypeOfOperationService;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.*;
import ru.bprn.printhouse.views.templates.service.*;

import java.util.*;
import java.util.stream.Collectors;

@PageTitle("Редактирование шаблонов")
@Route(value = "templates", layout = MainLayout.class)
@AnonymousAllowed
public class TemplatesView extends SplitLayout {

    private final UniversalEditorFactory universalEditorFactory;
    private final TemplatesModuleService templatesModuleService;
    private final OperationService operationService;
    private final PrintSheetsMaterialService printSheetsMaterialService;
    private final TemplatesMenuItemService menuItemService;
    private final ProductTypeVariableService productTypeVariableService;
    private final TemplateVariableService templateVariableService;

    private final TreeGrid<Object> treeGrid = new TreeGrid<>();
    private final TextField filterField = new TextField();

    private Templates currentTemplate = null;
    private AbstractProductType currentProductType = null;

    private Object selectedRow = null;
    private final ConfirmDialog confirmDeleteDialog;
    private Object objToCopy = null;
    private Object newUnsavedItem = null; // Поле для хранения временного, несохраненного элемента

    private MenuItem moveUpItem;
    private MenuItem moveDownItem;

    public TemplatesView(TemplatesModuleService templatesModuleService,
                         OperationService operationService, PrintSheetsMaterialService printSheetsMaterialService,
                         FormulasService formulasService, FormulaValidationService formulaValidationService, ProductTypeVariableService productTypeVariableService,
                         StandartSizeService standartSizeService, TemplatesMenuItemService menuItemService, TemplateVariableService templateVariableService, TypeOfOperationService typeOfOperationService,
                         AbstractMaterialService abstractMaterialService){


        this.templatesModuleService = templatesModuleService;
        this.operationService = operationService;
        this.printSheetsMaterialService = printSheetsMaterialService;
        this.menuItemService = menuItemService;
        this.productTypeVariableService = productTypeVariableService;
        this.templateVariableService = templateVariableService;

        this.universalEditorFactory = new UniversalEditorFactory(
                printSheetsMaterialService, formulasService, productTypeVariableService, formulaValidationService, standartSizeService, typeOfOperationService, abstractMaterialService, templateVariableService);

        confirmDeleteDialog = new ConfirmDialog("Внимание!" , "", "Да",
                confirmEvent -> {
                Object obj = treeGrid.asSingleSelect().getValue();
                Object parent;
                switch (obj) {
                    case Templates templates -> parent = null;
                    case AbstractProductType product -> parent = treeGrid.getTreeData().getParent(product);
                    case ProductOperation productOperation -> parent  = treeGrid.getTreeData().getParent(productOperation);
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
                case ProductOperation po -> {
                    Icon icon;
                    if (po.isSwitchOff()) {
                        icon = VaadinIcon.BAN.create();
                        icon.setColor("gray");
                    } else {
                        icon = VaadinIcon.COG_O.create();
                    }
                    Span label = new Span(po.getOperation().getName()+" - "+ po.getName());
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

        treeGrid.asSingleSelect().addValueChangeListener(this::handleSelectionChange);

        hlay.add(vl);
        return hlay;
    }

    // Меню в шапке TemplateView, создание компонентов и операций добавляются автоматически на основании @MenuItem(context = "product") у класса-сущности
    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.addThemeVariants(MenuBarVariant.LUMO_ICON);

        var create = menuBar.addItem(VaadinIcon.PLUS.create(), "Создать");
        var createSubMenu = create.getSubMenu();
        createSubMenu.addItem("Создать новый шаблон", e -> createNewItem(Templates.class, null));
        var components = createSubMenu.addItem("Создать новый компонент");
        addComponentsToSubMenu(components.getSubMenu());

        var operations = createSubMenu.addItem("Добавить операцию в продукт", e -> {
            if (currentProductType == null) {
                Notification.show("Сначала выберите продукт в дереве");
            }
        });
        operations.setEnabled(false); // Активируется при выборе продукта
        treeGrid.addSelectionListener(event -> {
            Object selectedItem = event.getFirstSelectedItem().orElse(null);
            operations.setEnabled(selectedItem instanceof AbstractProductType);
            updateButtonStates(selectedItem);
        });

        addOperationTemplatesToSubMenu(operations.getSubMenu());

        menuBar.addItem(VaadinIcon.COPY.create(), "Копировать", e->{
            objToCopy = treeGrid.asSingleSelect().getValue();
            if (objToCopy != null) {
                Notification.show("Скопировано: " + objToCopy.toString());
            }
        });
        menuBar.addItem(VaadinIcon.PASTE.create(), "Вставить", e->{
            handlePaste();
        });

        menuBar.addItem(VaadinIcon.MENU.create(), "Дублировать", e->{
            handleDuplicate();
        });

        moveUpItem = menuBar.addItem(VaadinIcon.ARROW_UP.create(), "Выше", e -> moveSelectedItem(true));
        moveDownItem = menuBar.addItem(VaadinIcon.ARROW_DOWN.create(), "Ниже", e -> moveSelectedItem(false));

        moveUpItem.setEnabled(false);
        moveDownItem.setEnabled(false);

        menuBar.addItem(VaadinIcon.DEL.create(), "Удалить", e->{
            if (!treeGrid.asSingleSelect().isEmpty()) {
                confirmDeleteDialog.setText("Вы уверены, что хотите удалить " + treeGrid.asSingleSelect().getValue() + " ?");
                confirmDeleteDialog.open();
            }
        });

        return menuBar;
    }

    private void addComponentsToSubMenu(SubMenu menu) {
        List<TemplatesMenuItem> list = menuItemService.getMenuByContext("product");
        if (list != null && !list.isEmpty())
            for (TemplatesMenuItem item : list) {
                menu.addItem(item.getName(), e -> createNewItem(item.getClassName(), currentTemplate));
            }
    }

    private void addOperationTemplatesToSubMenu(SubMenu menu) {
        List<Operation> operationTemplates = operationService.findAll();
        if (operationTemplates == null || operationTemplates.isEmpty()) {
            return;
        }

        // Группируем операции по их типу
        Map<TypeOfOperation, List<Operation>> groupedOperations = operationTemplates.stream()
                .filter(op -> op.getTypeOfOperation() != null)
                .collect(Collectors.groupingBy(Operation::getTypeOfOperation));

        // Создаем подменю для каждого типа, отсортировав их по имени
        groupedOperations.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(TypeOfOperation::getName)))
                .forEach(entry -> {
                    TypeOfOperation type = entry.getKey();
                    List<Operation> opsForType = entry.getValue();

                    MenuItem typeMenuItem = menu.addItem(type.getName());
                    SubMenu typeSubMenu = typeMenuItem.getSubMenu();

                    // Добавляем операции в подменю их типа
                    opsForType.stream()
                            .sorted(Comparator.comparing(Operation::getName))
                            .forEach(opTemplate -> addOperationToMenu(typeSubMenu, opTemplate));
                });

        // Обрабатываем операции без назначенного типа
        List<Operation> operationsWithoutType = operationTemplates.stream()
                .filter(op -> op.getTypeOfOperation() == null)
                .sorted(Comparator.comparing(Operation::getName))
                .toList();

        if (!operationsWithoutType.isEmpty()) {
            if (!groupedOperations.isEmpty()) menu.addSeparator();
            MenuItem othersMenuItem = menu.addItem("Прочие");
            SubMenu othersSubMenu = othersMenuItem.getSubMenu();
            operationsWithoutType.forEach(opTemplate -> addOperationToMenu(othersSubMenu, opTemplate));
        }
    }

    private void addOperationToMenu(SubMenu menu, Operation opTemplate) {
        menu.addItem(opTemplate.getName(), e -> {
            createNewItem(ProductOperation.class, currentProductType, opTemplate);
        });
    }

    private void handleSelectionChange(AbstractField.ComponentValueChangeEvent<Grid<Object>, Object> event) {
        // Если есть несохраненный элемент и пользователь выбрал другой элемент (или ничего),
        // удаляем временный элемент из грида.
        if (newUnsavedItem != null && event.getValue() != newUnsavedItem) {
            treeGrid.getTreeData().removeItem(newUnsavedItem);
            treeGrid.getDataProvider().refreshAll();
            newUnsavedItem = null;
        }

        selectedRow = event.getValue();
        setCurrent(selectedRow);
        updateButtonStates(selectedRow);

        if (selectedRow != null) {
            // Открываем редактор только если это не наш временный элемент
            // (для него редактор уже открыт при создании).
            if (selectedRow != newUnsavedItem) {
                AbstractEditor<?> editor = universalEditorFactory.createEditor(selectedRow, this::save);
                addEditor(editor);
            }
        } else {
            // Если ничего не выбрано, очищаем правую панель
            if (this.getSecondaryComponent() != null) this.remove(this.getSecondaryComponent());
        }
    }

    private void createNewItem(Class<?> itemClass, Object parent, Object... args) {
        Object newItem = EntityFactory.createEntity(itemClass, productTypeVariableService, templateVariableService, args);
        if (newItem == null) return;

        // Если уже есть несохраненный элемент, удаляем его
        if (newUnsavedItem != null) {
            treeGrid.getTreeData().removeItem(newUnsavedItem);
        }

        // Добавляем новый элемент в грид и обновляем
        treeGrid.getTreeData().addItem(parent, newItem);
        treeGrid.getDataProvider().refreshAll();

        // Запоминаем его как временный
        newUnsavedItem = newItem;

        // Открываем редактор и выбираем элемент в гриде
        addEditor(universalEditorFactory.createEditor(newItem, this::save));
        selectAndExpand(newItem);
    }

    private void createNewItem(String className, Object parent) {
        createNewItem(EntityFactory.getClassForName(className), parent);
    }

    private void setCurrent(Object select) {
        if (select!=null) {
            var temp = treeGrid.getTreeData().getParent(select);
            if (temp == null) {
                currentTemplate = (Templates) select;
                currentProductType = null;
            } else if (temp instanceof Templates) {
                currentTemplate = (Templates) temp;
                currentProductType = (AbstractProductType) select;
            } else if (temp instanceof AbstractProductType) {
                currentTemplate = (Templates) treeGrid.getTreeData().getParent(temp);
                currentProductType = (AbstractProductType) temp;
            }
        }
    }

    private void addEditor(Component editor) {
        var obj = this.getSecondaryComponent();
        if (obj != null) this.remove(obj);
        this.addToSecondary(editor);
    }

    private void moveSelectedItem(boolean up) {
        if (selectedRow instanceof ProductOperation currentSelection) {
            var treeData = treeGrid.getTreeData();
            AbstractProductType parent = (AbstractProductType) treeData.getParent(currentSelection);
            var childrenList  = new ArrayList<>(treeData.getChildren(parent));
            
            if (parent != null) {
                templatesModuleService.swapProductOperations(currentSelection, up);
                for (var child:childrenList) treeData.removeItem(child);
                for (ProductOperation po : parent.getProductOperations()) treeData.addItem(parent, po);
                treeGrid.getDataProvider().refreshItem(parent, true);
            }
            treeGrid.select(currentSelection); // Восстанавливаем выделение.
        }
    }

    private void updateButtonStates(Object selected) {
        if (selected instanceof ProductOperation po) {
            AbstractProductType parent = po.getProduct();
            if (parent != null) {
                List<ProductOperation> siblings = parent.getProductOperations();
                int index = siblings.indexOf(po);
                moveUpItem.setEnabled(index > 0);
                moveDownItem.setEnabled(index < siblings.size() - 1);
                return;
            }
        }
        moveUpItem.setEnabled(false);
        moveDownItem.setEnabled(false);
    }

    private void save(Object object) {
        // Определяем, новый ли это элемент, по наличию ID.
        boolean isNew = switch (object) {
            case Templates t -> t.getId() == null;
            case AbstractProductType apt -> apt.getId() == null;
            case ProductOperation po -> po.getId() == null;
            default -> false;
        };

        Object parent = isNew ? (object instanceof ProductOperation ? currentProductType : currentTemplate) : treeGrid.getTreeData().getParent(object);

        Object savedItem = templatesModuleService.save(object, parent);
        Notification.show("Сохранено");

        // Если мы сохраняли временный элемент, сбрасываем его
        if (isNew && object == newUnsavedItem) {
            newUnsavedItem = null;
        }

        if (isNew) {
            // Если это новый элемент, самый надежный способ - перезагрузить все дерево
            populate(filterField.getValue());
            // И выбрать только что созданный элемент
            selectAndExpand(savedItem);
        } else {
            // Это обновление существующего элемента
            // Здесь refreshItem работает, т.к. элемент уже был в DataProvider
            refreshAndSelect(savedItem, parent);
        }
    }


    private void deleteElement(Object object, Object parent) {
        // Если удаляемый элемент - это наш временный, просто сбрасываем его
        if (object == newUnsavedItem) {
            newUnsavedItem = null;
        }
        templatesModuleService.delete(object, parent);
        Notification.show("Удалено");
        treeGrid.getTreeData().removeItem(object);
        refreshAndSelect(null, parent);
    }

    private void handleDuplicate() {
        Object selected = treeGrid.asSingleSelect().getValue();
        if (selected == null) {
            Notification.show("Сначала выберите элемент для дублирования");
            return;
        }
        Object parent = treeGrid.getTreeData().getParent(selected);
        paste(selected, parent);
    }

    private void handlePaste() {
        if (objToCopy == null) {
            Notification.show("Буфер обмена пуст. Сначала скопируйте элемент.");
            return;
        }

        Object target = treeGrid.asSingleSelect().getValue();
        Object parentForPasting = null;

        // Определяем родителя для вставки согласно правилам
        if (objToCopy instanceof Templates) {
            parentForPasting = null; // Шаблоны всегда в корне
        } else if (objToCopy instanceof AbstractProductType) {
            if (target instanceof Templates t) {
                parentForPasting = t;
            } else if (target instanceof AbstractProductType apt) {
                parentForPasting = treeGrid.getTreeData().getParent(apt);
            } else {
                Notification.show("Компонент можно вставить только в шаблон");
                return;
            }
        } else if (objToCopy instanceof ProductOperation) {
            if (target instanceof AbstractProductType apt) {
                parentForPasting = apt;
            } else if (target instanceof ProductOperation po) {
                parentForPasting = treeGrid.getTreeData().getParent(po);
            } else {
                Notification.show("Операцию можно вставить только в компонент продукта");
                return;
            }
        }

        // Проверяем, что родитель найден (для подстраховки)
        if (objToCopy instanceof AbstractProductType && parentForPasting == null) {
             Notification.show("Не удалось определить шаблон для вставки компонента");
             return;
        }
        if (objToCopy instanceof ProductOperation && parentForPasting == null) {
            Notification.show("Не удалось определить компонент для вставки операции");
            return;
        }

        paste(objToCopy, parentForPasting);
    }

    private void paste(Object obj, Object parent) {
        if (obj!=null){
            Object newEntity = templatesModuleService.duplicate(obj, parent);
            // После дублирования (которое сохраняет в БД), мы полностью перезагружаем грид
            populate(filterField.getValue());
            // И выбираем новый элемент, чтобы он появился в редакторе
            selectAndExpand(newEntity);
        }
        else Notification.show("Сначала выделите какой-либо элемент таблицы");
    }

    private void populate(String filter) {
        treeGrid.setDataProvider(templatesModuleService.getTreeDataProvider(filter));
    }

    private void refreshAndSelect(Object itemToSelect, Object parentToRefresh) {
        if (itemToSelect != null && parentToRefresh == null) { // Обновление корневого элемента (Templates)
            treeGrid.getDataProvider().refreshItem(itemToSelect);
        } else if (itemToSelect != null && parentToRefresh != null) { // Обновление дочернего элемента
            treeGrid.getDataProvider().refreshItem(itemToSelect);
        } else if (parentToRefresh != null) { // Удаление элемента, нужно обновить родителя
            treeGrid.getDataProvider().refreshItem(parentToRefresh, true); // true - для рекурсивного обновления
        } else {
            populate(filterField.getValue()); // Полная перезагрузка, если все остальное не подошло
        }
        if (itemToSelect != null) {
            selectAndExpand(itemToSelect);
        }
    }

    private void selectAndExpand(Object item) {
        if (item == null) return;
        // Рекурсивно разворачиваем всех родителей
        Object parent = treeGrid.getTreeData().getParent(item);
        while (parent != null) {
            treeGrid.expand(parent);
            parent = treeGrid.getTreeData().getParent(parent);
        }
        treeGrid.select(item);
    }

    public static class EntityFactory {

        public static Class<?> getClassForName(String className) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Класс не найден: " + className, e);
            }
        }

        public static Object createEntity(Class<?> clazz, ProductTypeVariableService productTypeVariableService, TemplateVariableService templateVariableService, Object... args) {
            try {
                Object entity;
                if (clazz == ProductOperation.class && args.length > 0 && args[0] instanceof Operation) {
                    // Специальный конструктор для ProductOperation из шаблона Operation
                    entity = new ProductOperation((Operation) args[0]);
                } else {
                    // Стандартный конструктор без аргументов
                    entity = clazz.getDeclaredConstructor().newInstance();
                }

                // Инициализация переменных, если это необходимо
                if (entity instanceof AbstractProductType apt) {
                    apt.initializeVariables(productTypeVariableService);
                }
                if (entity instanceof Templates t) {
                    t.initializeVariables(templateVariableService);
                }

                return entity;
            } catch (Exception e) {
                throw new RuntimeException("Не удалось создать экземпляр " + clazz.getName(), e);
            }
        }
    }
}
