package ru.bprn.printhouse.views.dictionary;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ru.bprn.printhouse.data.entity.AdditionalWorksBean;
import ru.bprn.printhouse.data.entity.Formulas;
import ru.bprn.printhouse.data.entity.Material;
import ru.bprn.printhouse.data.entity.TypeOfWorks;
import ru.bprn.printhouse.data.service.AdditionalWorksBeanService;
import ru.bprn.printhouse.data.service.FormulasService;
import ru.bprn.printhouse.data.service.MaterialService;
import ru.bprn.printhouse.data.service.TypeOfWorksService;
import ru.bprn.printhouse.views.MainLayout;
import ru.bprn.printhouse.views.templates.SelectMaterailsDialog;

@PageTitle("Создание дополнительных работ")
@Route(value = "additional_works", layout = MainLayout.class)
@AnonymousAllowed
public class AdditionalWorksBeanDictionary extends VerticalLayout {
    private final TextField filterField = new TextField();
    private final Grid<AdditionalWorksBean> grid = new Grid<>(AdditionalWorksBean.class, false);
    private final AdditionalWorksBeanService service;
    private final FormulasService formulasService;
    private final TypeOfWorksService typeOfWorksService;
    private final MaterialService materialService;
    private final BeanValidationBinder<AdditionalWorksBean> bean = new BeanValidationBinder<>(AdditionalWorksBean.class);
    private final SelectMaterailsDialog materialDialog = new SelectMaterailsDialog("Выберите материалы");
    private final Select<Material> materialSelect = new Select<>();

    public AdditionalWorksBeanDictionary(AdditionalWorksBeanService service,
                                         FormulasService formulasService,
                                         TypeOfWorksService typeOfWorksService,
                                         MaterialService materialService) {
        this.service = service;
        this.formulasService = formulasService;
        this.typeOfWorksService = typeOfWorksService;
        this.materialService = materialService;
        this.setSizeFull();
        var splitLayout = new SplitLayout(addGrid(), addForm(), SplitLayout.Orientation.HORIZONTAL);
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(60.0);
        this.add(splitLayout);
    }

    private Component addGrid() {
        filterField.setWidth("50%");
        filterField.setPlaceholder("Поиск");
        filterField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        filterField.setValueChangeMode(ValueChangeMode.LAZY);
        filterField.addValueChangeListener(e -> populate(e.getValue().trim()));
        filterField.setClearButtonVisible(true);

        grid.addColumn(AdditionalWorksBean::getName,"Название" );
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        populate(null);

        grid.addItemClickListener(e->{
           bean.setBean(e.getItem());
           materialSelect.setItems(bean.getBean().getListOfMaterials());
           bean.refreshFields();
        });

        materialDialog.getGrid().setItems(materialService.findAll());

        return  new VerticalLayout(buttons(), filterField, grid);
    }

    private Component addForm() {
        materialSelect.setLabel("Материал по умолчанию");

        materialDialog.addOpenedChangeListener(openedChangeEvent -> {
            if (openedChangeEvent.isOpened()) materialDialog.setSelectedMaterial(bean.getBean().getListOfMaterials());
            else {
                var oldValue = materialSelect.getOptionalValue();
                materialSelect.setItems(materialDialog.getGrid().getSelectedItems());
                oldValue.ifPresent(materialSelect::setValue);
            }
        });
        bean.forField(materialDialog.getGrid().asMultiSelect()).bind(AdditionalWorksBean::getListOfMaterials, AdditionalWorksBean::setListOfMaterials);
        bean.forField(materialSelect).asRequired().bind(AdditionalWorksBean::getDefaultMaterial, AdditionalWorksBean::setDefaultMaterial);

        var mButton = new Button("Материал", e->materialDialog.open());

        var name = new TextField("Название");
        bean.forField(name).asRequired().bind(AdditionalWorksBean::getName, AdditionalWorksBean::setName);
        Select<TypeOfWorks> typeOfWork = new Select<>();
        typeOfWork.setLabel("Тип работы");
        typeOfWork.setItems(typeOfWorksService.findAll());
        bean.forField(typeOfWork).asRequired().bind(AdditionalWorksBean::getTypeOfWorks, AdditionalWorksBean::setTypeOfWorks);

        var actionCheck = new Checkbox("Есть работа?");
        bean.forField(actionCheck).bind(AdditionalWorksBean::haveAction, AdditionalWorksBean::setHaveAction);
        Select<Formulas> actFormula = new Select<>();
        actFormula.setLabel("Формула");
        actFormula.setItems(formulasService.findAll());
        bean.forField(actFormula).asRequired().bind(AdditionalWorksBean::getActionFormula, AdditionalWorksBean::setActionFormula);
        var materialCheck = new Checkbox("Есть материал?");
        bean.forField(materialCheck).bind(AdditionalWorksBean::haveMaterials, AdditionalWorksBean::setHaveMaterial);
        Select<Formulas> mFormula = new Select<>();
        mFormula.setLabel("Формула");
        mFormula.setItems(formulasService.findAll());
        bean.forField(mFormula).asRequired().bind(AdditionalWorksBean::getMaterialFormula, AdditionalWorksBean::setMaterialFormula);

        var formLayout = new FormLayout();
        formLayout.add(name, typeOfWork, actionCheck, actFormula, materialCheck, mFormula, mButton, materialSelect);

        var save = new Button("Сохранить", buttonClickEvent -> save());
        var cancel = new Button("Отменить", buttonClickEvent -> cancel());
        var hl = new HorizontalLayout(save,cancel);
        hl.setJustifyContentMode(JustifyContentMode.END);
        formLayout.addFormItem(hl,"");
        return new VerticalLayout(formLayout);
    }

    public void populate(String str) {
        grid.setItems(service.populate(str));

    }

    private void save(){
        if (bean.isValid()) {
            service.save(bean.getBean());
            Notification.show("Сохранено");
            populate(null);
            cancel();
        }
        else Notification.show("Заполните требуемые поля!");
    }

    private void cancel(){
        bean.removeBean();
        bean.refreshFields();
    }

    private HorizontalLayout buttons() {
        var dialogChain = new ConfirmDialog("Внимание!" , "", "Да",
                confirmEvent -> {
                    deleteElement();
                    Notification.show("Элемент удален!");
                },
                "Нет", cancelEvent -> cancelEvent.getSource().close());

        var hl = new HorizontalLayout();

        var createTemplateButton = new Button(VaadinIcon.PLUS.create(), event -> {
            bean.setBean(new AdditionalWorksBean());
            bean.refreshFields();
        });
        createTemplateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createTemplateButton.setTooltipText("Создать новую работу");

        var duplicateButton = new Button(VaadinIcon.COPY_O.create(), event -> {
                service.duplicate(grid.asSingleSelect().getValue());
                populate(filterField.getValue().trim());
        });
        duplicateButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        duplicateButton.setTooltipText("Создать дубликат работы");

        var deleteButton = new Button(VaadinIcon.CLOSE.create(), event -> {
            if (!grid.asSingleSelect().isEmpty()) {
                dialogChain.setText("Вы уверены, что хотите удалить " + grid.asSingleSelect().getValue().getName() + " ?");
                dialogChain.open();
            }
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deleteButton.setTooltipText("Удалить элемент");

        hl.add(createTemplateButton, duplicateButton, deleteButton);
        return hl;
    }

    private void deleteElement() {
        service.delete(bean.getBean());
        bean.removeBean();
        bean.refreshFields();
        populate(filterField.getValue().trim());
    }

}
