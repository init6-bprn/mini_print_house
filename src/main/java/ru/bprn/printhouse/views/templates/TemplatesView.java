package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ru.bprn.printhouse.data.service.*;
import ru.bprn.printhouse.views.MainLayout;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.operation.service.OperationService;
import ru.bprn.printhouse.views.operation.service.TypeOfOperationService;
import ru.bprn.printhouse.views.material.service.MaterialService;
import ru.bprn.printhouse.views.templates.entity.AbstractProductType;
import ru.bprn.printhouse.views.templates.entity.Chains;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.service.TemplatesService;

@PageTitle("Редактирование шаблонов")
@Route(value = "templates", layout = MainLayout.class)
@AnonymousAllowed
public class TemplatesView extends SplitLayout {

    private final TemplatesService templatesService;
    private final TypeOfOperationService typeOfOperationService;
    private final OperationService worksBeanService;
    private final VariablesForMainWorksService variablesForMainWorksService;
    private final FormulasService formulasService;
    private final StandartSizeService standartSizeService;
    private final GapService gapService;
    private final MaterialService materialService;
    private final BeanValidationBinder<Templates> templatesBinder;

    private final TreeGrid<Object> treeGrid = new TreeGrid<>();
    private TreeData<Object> treeGridData = new TreeData<>();
    private final TextField filterField = new TextField();

    private Templates currentTemplate = null;
    private AbstractProductType currentProduct = null;
    private Operation currentOperation = null;

    private final TemplateEditor templateEditor;
    private final ProductEditor productEditor;
    private final OperationEditor operationEditor;
    private AddChainDialog addChainDialog;

    public TemplatesView(TemplatesService templatesService,
                         TypeOfOperationService typeOfOperationService, OperationService operationService,
                         VariablesForMainWorksService variablesForMainWorksService, FormulasService formulasService,
                         StandartSizeService standartSizeService, GapService gapService,
                         MaterialService materialService){
        this.templatesService = templatesService;
        this.typeOfOperationService = typeOfOperationService;
        this.worksBeanService = operationService;
        this.variablesForMainWorksService = variablesForMainWorksService;
        this.formulasService = formulasService;
        this.standartSizeService = standartSizeService;
        this.gapService = gapService;
        this.materialService = materialService;
        templatesBinder = new BeanValidationBinder<>(Templates.class);
        templatesBinder.setChangeDetectionEnabled(true);


        templateEditor = new TemplateEditor(this, treeGrid, templatesService);
        templateEditor.setVisible(false);
        templateEditor.setEnabled(false);

        productEditor = new ProductEditor();
        operationEditor = new OperationEditor(operationService::save);


        this.setOrientation(Orientation.HORIZONTAL);
        this.setSplitterPosition(60.0);
        this.setSizeFull();
        this.addToPrimary((treeGrid()));
        this.addToSecondary(templateEditor);

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
                    deleteElement();
                    Notification.show("Элемент удален!");
                },
                "Нет", cancelEvent -> cancelEvent.getSource().close());

        var hl = new HorizontalLayout();
        var createTemplateButton = new Button(VaadinIcon.PLUS.create(), event -> {
            currentTemplate = new Templates();
            templateEditor.setTemplate(currentTemplate);
            hideTemplateAndShowChain(false);
        });
        createTemplateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createTemplateButton.setTooltipText("Создать новый шаблон");

        var createChainButton = new Button(VaadinIcon.PLUS.create(), event -> {
            switch (treeGrid.asSingleSelect().getValue()){
                case Templates templates:
                    break;
                case AbstractProductType productType:
                    break;
                case Operation operation:
                    break;
                default: Notification.show("Сначала выделите шаблон");
            }

            if (abs!=null) {
                if (abs instanceof Templates) currentTemplate = (Templates) abs;
                else currentTemplate = (Templates) treeGrid.getDataCommunicator().getParentItem(abs);
                //chainEditor.setTemplate(currentTemplate);
                //chainEditor.setChains(new Chains());
                hideTemplateAndShowChain(true);
            }
            else
        });
        createChainButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createChainButton.setTooltipText("Создать новую рабочую цепочку");

        var updateButton  = new Button(VaadinIcon.EDIT.create(), event -> {
            if (!treeGrid.asSingleSelect().isEmpty()) {
                switch (treeGrid.asSingleSelect().getValue()) {
                    case Templates template:
                        currentTemplate = template;
                        templateEditor.setTemplate(template);
                        hideTemplateAndShowChain(false);
                        break;
                    case Chains chain:
                        currentTemplate =(Templates) treeGrid.getDataCommunicator().getParentItem(chain);
                        //chainEditor.setTemplate(currentTemplate);
                        //chainEditor.removeTabs();
                        //chainEditor.setChains(chain);
                        hideTemplateAndShowChain(true);
                        break;
                    default:
                }
            }
        });
        updateButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        updateButton.setTooltipText("Изменить шаблон/цепочку");

        var duplicateButton = new Button(VaadinIcon.COPY_O.create(), event -> {
            if (!treeGrid.asSingleSelect().isEmpty()) {
                switch (treeGrid.asSingleSelect().getValue()) {
                    case Templates template: templatesService.duplicateTemplate(template);
                        break;
                    case AbstractProductType productType: templatesService.addProductToTemplate(currentTemplate, templatesService.duplicateProduct(productType));
                        break;
                    case Operation operation: templatesService.duplicateOperation(currentProduct, operation);
                    default:
                }
                populate(filterField.getValue().trim());
            }
            else Notification.show("Сначала выделите какой-либо элемент таблицы");
        });
        duplicateButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        duplicateButton.setTooltipText("Создать дубликат шаблона/цепочки");

        var addChainButton = new Button(VaadinIcon.ADD_DOCK.create(), event -> {
            if (!treeGrid.asSingleSelect().isEmpty()) {
                switch (treeGrid.asSingleSelect().getValue()) {
                    case Templates template:
                        this.addToSecondary(templateEditor);
                        currentTemplate = template;
                        break;
                    case Chains chain:
                        currentTemplate =(Templates) treeGrid.getDataCommunicator().getParentItem(chain);
                        break;
                    default:
                }

                if (addChainDialog == null) {
                    addChainDialog = new AddChainDialog(currentTemplate, templatesService);
                    addChainDialog.addOpenedChangeListener(closeEvent -> {
                        if (closeEvent.isOpened()) {
                            addChainDialog.populate();
                        }
                        else populate(filterField.getValue().trim());
                    });
                    addChainDialog.open();
                }
                else {
                    addChainDialog.setTemplate(currentTemplate);
                    addChainDialog.open();
                }
            }
            else Notification.show("Сначала выделите шаблон");
        });
        addChainButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        addChainButton.setTooltipText("Добавить существующую цепочку");

        var deleteButton = new Button(VaadinIcon.CLOSE.create(), event -> {
            if (!treeGrid.asSingleSelect().isEmpty()) {
                confirmDeleteDialog.setText("Вы уверены, что хотите удалить " + treeGrid.asSingleSelect().getValue() + " ?");
                confirmDeleteDialog.open();
            }
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.setTooltipText("Удалить элемент");

        hl.add(createTemplateButton, createChainButton, updateButton, duplicateButton, addChainButton, deleteButton);
        vl.add(filterField, hl, treeGrid);

        treeGrid.addHierarchyColumn(Templates::getName);
        treeGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        treeGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        populate(filterField.getValue().trim());

        treeGrid.asSingleSelect().addValueChangeListener(e->{
            setCurrentEditor(e.getValue());
            if (e.getValue()!= null) {
                switch (e.getValue()) {
                    case Templates template:
                        currentTemplate = template;
                        templateEditor.setTemplate(template);
                        chainEditor.setVisible(false);
                        templateEditor.setVisible(true);
                        break;
                    case AbstractProductType product:
                        if (currentTemplate ==null) currentTemplate = (Templates) treeGrid.getDataCommunicator().getParentItem(product);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + e.getValue());
                }
            }
        });


        hlay.add(vl);
        return hlay;
    }

    private void setCurrentEditor(Object value) {
        switch (value) {
            case Templates template:
                currentTemplate = template;
                currentProduct = null;
                currentOperation = null;
                break;
            case AbstractProductType product:
                currentTemplate = (Templates) treeGrid.getDataCommunicator().getParentItem(product);
                currentProduct = product;
                currentOperation = null;
                break;
            case Operation operation:
                currentProduct = (AbstractProductType) treeGrid.getDataCommunicator().getParentItem(operation);
                currentTemplate = (Templates) treeGrid.getDataCommunicator().getParentItem(currentProduct);
                currentOperation = operation;
                break;
            default:
                currentTemplate = null;
                currentProduct = null;
                currentOperation = null;
        }
    }

    private void hideSecondary(){
        this.getPrimaryComponent().setVisible(false);
        this.getSecondaryComponent().getElement().setEnabled(true);
        this.setSplitterPosition(0);
    }

    private void hideTemplateAndShowChain(boolean aBoolean) {
            chainEditor.setVisible(aBoolean);
            templateEditor.setVisible(!aBoolean);
            chainEditor.setEnabled(aBoolean);
            templateEditor.setEnabled(!aBoolean);
            hideSecondary();
    }

    private void deleteElement(){
        var abstractTemplate = treeGrid.asSingleSelect().getValue();
        switch (abstractTemplate) {
            case Templates template:
                templatesService.delete(template);
                break;
            case Chains chain:
                currentTemplate = (Templates) treeGrid.getDataCommunicator().getParentItem(chain);
                currentTemplate.getChains().remove(chain);
                templatesService.save(currentTemplate);
                break;
            default:
        }
        populate(filterField.getValue().trim());
    }

    private void populate(String filter) {
        treeGrid.setDataProvider(templatesService.populateGrid(filter));
    }
}
