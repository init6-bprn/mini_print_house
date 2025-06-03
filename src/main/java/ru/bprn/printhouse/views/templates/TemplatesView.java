package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ru.bprn.printhouse.views.MainLayout;
import ru.bprn.printhouse.views.templates.entity.AbstractTemplate;
import ru.bprn.printhouse.views.templates.entity.Chains;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.service.TemplatesService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@PageTitle("Редактирование шаблонов")
@Route(value = "templates", layout = MainLayout.class)
@AnonymousAllowed
public class TemplatesView extends SplitLayout {

    private final TemplatesService templatesService;
    private final BeanValidationBinder<Templates> templatesBinder;
    private final BeanValidationBinder<Chains> chainBinder;

    private final Grid<Templates> templateGrid = new Grid<>(Templates.class, false);

    private final TreeGrid<AbstractTemplate> chainGrid = new TreeGrid<>(AbstractTemplate.class, false);

    private final ConfirmDialog confirm;

    private Templates beanForTempl;

    private final SplitLayout split = new SplitLayout(Orientation.VERTICAL);

    public TemplatesView(TemplatesService templatesService){
        this.templatesService = templatesService;
        templatesBinder = new BeanValidationBinder<>(Templates.class);
        templatesBinder.setChangeDetectionEnabled(true);

        chainBinder = new BeanValidationBinder<>(Chains.class);

        confirm = new ConfirmDialog("Шаблон был изменен",
                "Вы хотите сохранить изменения?",
                "Сохранить", s->saveBean(),
                "Отменить", c->{
            cancelBean();
            c.getSource().close();
        });

        this.setOrientation(Orientation.HORIZONTAL);
        this.setSplitterPosition(20.0);
        this.setSizeFull();
        this.addToPrimary(templatesGrid());
        this.addToSecondary(templatesView());

        split.setSplitterPosition(50.0);
    }

    private Component templatesView() {
        split.setSplitterPosition(40.0);
        split.setSizeFull();
        split.addToPrimary(formLayout());
        //split.remove(split.getSecondaryComponent());
        return split;
    }

    private Component formLayout() {
        var vl = new VerticalLayout();
        var name = new TextField("Название шаблона:");
        name.setWidthFull();
        templatesBinder.bind(name, Templates::getName, Templates::setName);

        var description = new TextArea("Краткое описание:");
        description.setWidthFull();
        description.setMaxRows(5);
        templatesBinder.bind(description, Templates::getDescription, Templates::setDescription);

        var saveButton = new Button("Save", o -> saveBean());
        var cancelButton = new Button("Cancel", o ->cancelBean());
        var hl = new HorizontalLayout(FlexComponent.Alignment.END, saveButton, cancelButton);

        vl.add(name, description, chainGrid(), hl);
        return vl;
    }

    private Component chainGrid() {
        var hlay = new HorizontalLayout();
        hlay.setWidthFull();
        chainGrid.setWidthFull();

        var vl = new VerticalLayout();
        vl.setWidthFull();

        var dialogChain = new ConfirmDialog("Внимание!" , "", "Да", confirmEvent ->
        {
            if (!chainGrid.asSingleSelect().isEmpty()) {
                var opt = chainGrid.asSingleSelect().getValue();
                beanForTempl.getChains().remove(opt);
                //chainGrid.setItems(beanForTempl.getChains());
                Notification.show("Рабочая цепочка удалена!");
            }
        },
                "Нет", cancelEvent -> cancelEvent.getSource().close());

        var hl = new HorizontalLayout();
        var createTemplateButton = new Button(VaadinIcon.PLUS.create(), event -> {

            chainBinder.setBean(new Chains());
        });
        createTemplateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var createChainButton = new Button(VaadinIcon.PLUS.create(), event -> {
            chainBinder.setBean(new Chains());
        });
        createChainButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var updateButton  = new Button(VaadinIcon.EDIT.create(), event -> {
            if (!chainGrid.asSingleSelect().isEmpty()) {
                //chainBinder.setBean(chainGrid.asSingleSelect().getValue());
                Notification.show("Рабочая цепочка удалена!");
            }
        });
        updateButton.addThemeVariants(ButtonVariant.LUMO_ICON);

        var duplicateButton = new Button(VaadinIcon.COPY_O.create(), event -> {

        });
        duplicateButton.addThemeVariants(ButtonVariant.LUMO_ICON);

        var deleteButton = new Button(VaadinIcon.CLOSE.create(), event -> {

            if (!chainGrid.asSingleSelect().isEmpty()) {
                dialogChain.setText("Вы уверены, что хотите удалить цепочку " + chainBinder.getBean().getName() + " ?");
                dialogChain.open();
            }
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        hl.add(createTemplateButton, createChainButton, updateButton, duplicateButton, deleteButton);
        vl.add(hl, chainGrid);

        chainGrid.addColumn(AbstractTemplate::getName).setHeader("Название цепочки");
        chainGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        chainGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        chainGrid.setItems(templatesService.findAllAsAbstractTemplates(), this::getChains);

        chainGrid.asSingleSelect().addValueChangeListener(e->{
            if (e.getValue()!= null) {
                var component = split.getSecondaryComponent();
                if (component != null) split.remove(component);
                switch (e.getValue()) {
                    case Templates template:
                        split.addToSecondary(new TemplateEditor(template, templatesService));

                        break;
                    case Chains chain:
                        split.addToSecondary(new ChainEditor());

                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + e.getValue());
                }
            }
        });


        hlay.add(vl);
        return hlay;
    }

    private List<AbstractTemplate> getChains(AbstractTemplate abstractTemplate) {
        var template = templatesService.findById(abstractTemplate.getId());
        List<AbstractTemplate> list = new ArrayList<>();
        template.ifPresent(templates -> list.addAll(templates.getChains()));
        return list;
    }

    private Component templatesGrid() {
        var vl = new VerticalLayout();
        vl.setSizeUndefined();

        var dialog = new ConfirmDialog("Внимание!" , "", "Да", confirmEvent ->
        {
            if (!templateGrid.asSingleSelect().isEmpty()) {
                var opt = templatesService.findById(templateGrid.asSingleSelect().getValue().getId());
                if (opt.isPresent()) {
                    templatesService.delete(opt.get());
                    templateGrid.setItems(templatesService.findAll());
                    cancelBean();
                    Notification.show("Шаблон удален!");
                }
            }
        },
                "Нет", cancelEvent -> cancelEvent.getSource().close());

        var hl = new HorizontalLayout();
        var createButton = new Button(VaadinIcon.PLUS.create(), event -> {
             beanForTempl = new Templates();
             templatesBinder.readBean(beanForTempl);

        });
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var updateButton  = new Button(VaadinIcon.EDIT.create(), event -> {
            if (!templateGrid.asSingleSelect().isEmpty()) {
                var opt = templatesService.findById(templateGrid.asSingleSelect().getValue().getId());
                if (opt.isPresent()) {
                    beanForTempl = opt.get();
                    templatesBinder.readBean(beanForTempl);
                }
            }
        });
        updateButton.addThemeVariants(ButtonVariant.LUMO_ICON);

        var duplicateButton = new Button(VaadinIcon.COPY_O.create(), event -> {

        });
        duplicateButton.addThemeVariants(ButtonVariant.LUMO_ICON);

        var deleteButton = new Button(VaadinIcon.CLOSE.create(), event -> {
            if (beanForTempl != null) {
                dialog.setText("Вы уверены, что хотите удалить шаблон " + beanForTempl.getName() + " ?");
                dialog.open();
            }
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);


        hl.add(createButton, updateButton, duplicateButton, deleteButton);
        vl.add(hl);

        templateGrid.addColumn(Templates::getName).setHeader("Название шаблона");

        templateGrid.setItems(this.templatesService.findAll());
        templateGrid.setHeightFull();
        templateGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        templateGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);

        vl.add(templateGrid);

        templateGrid.asSingleSelect().addValueChangeListener(listener ->{
            if (templatesBinder.hasChanges()) confirm.open();
            else {
                if (listener.getValue() != null) {
                    Optional<Templates> templFromBackend = templatesService.findById(listener.getValue().getId());
                    if (templFromBackend.isPresent()) {
                        beanForTempl = templFromBackend.get();
                        templatesBinder.readBean(beanForTempl);
                        //chainGrid.setItems(templFromBackend.get().getChains());
                    } else Notification.show("Не найдено такого шаблона!");
                }
            }
        });

        templateGrid.addItemDoubleClickListener(__->updateButton.click());
        return vl;
    }



    private void saveBean() {
        if (beanForTempl != null) {
            try {
                templatesBinder.writeBean(beanForTempl);
                templatesService.save(beanForTempl);
                templateGrid.setItems(templatesService.findAll());
                templateGrid.select(null);
                Notification.show("Сохранено!");
            } catch (ValidationException e) {
                Notification.show("Есть невалидные значения. Не сохранено!");
            }
        }
        else Notification.show("Нечего сохранять!");
    }

    private void cancelBean(){
        templatesBinder.removeBean();
        templatesBinder.refreshFields();
        beanForTempl = null;
        templateGrid.select(null);
    }
}
