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
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ru.bprn.printhouse.views.MainLayout;
import ru.bprn.printhouse.views.templates.entity.Chains;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.service.TemplatesService;

import java.util.Optional;

@PageTitle("Редактирование шаблонов")
@Route(value = "templates", layout = MainLayout.class)
@AnonymousAllowed
public class TemplatesView extends SplitLayout {

    private final TemplatesService templatesService;
    private final BeanValidationBinder<Templates> templatesBean;

    private final Grid<Templates> templateGrid = new Grid<>(Templates.class, false);

    private final Grid<Chains> chainGrid = new Grid<>(Chains.class, false);

    private final ConfirmDialog confirm;

    private Templates bean;

    public TemplatesView(TemplatesService templatesService){
        this.templatesService = templatesService;
        templatesBean = new BeanValidationBinder<>(Templates.class);
        templatesBean.setChangeDetectionEnabled(true);

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
    }

    private Component templatesView() {
        var split = new SplitLayout(Orientation.HORIZONTAL);
        split.setSplitterPosition(40.0);
        split.setSizeFull();
        split.addToPrimary(formLayout());
        return split;
    }

    private Component formLayout() {
        var vl = new VerticalLayout();
        var name = new TextField("Название шаблона:");
        name.setWidthFull();
        templatesBean.bind(name, Templates::getName, Templates::setName);

        var description = new TextArea("Краткое описание:");
        description.setWidthFull();
        description.setMaxRows(5);
        templatesBean.bind(description, Templates::getDescription, Templates::setDescription);

        chainGrid.setWidthFull();
        chainGrid.addColumn(Chains::getName).setHeader("Название цепочки");


        var saveButton = new Button("Save", o -> saveBean());
        var cancelButton = new Button("Cancel", o ->cancelBean());
        var hl = new HorizontalLayout(FlexComponent.Alignment.END, saveButton, cancelButton);

        vl.add(name, description, chainGrid, hl);
        return vl;
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
             bean = new Templates();
             templatesBean.readBean(bean);

        });
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var updateButton  = new Button(VaadinIcon.EDIT.create(), event -> {
            if (!templateGrid.asSingleSelect().isEmpty()) {
                var opt = templatesService.findById(templateGrid.asSingleSelect().getValue().getId());
                if (opt.isPresent()) {
                    bean = opt.get();
                    templatesBean.readBean(bean);
                }
            }
        });
        updateButton.addThemeVariants(ButtonVariant.LUMO_ICON);

        var duplicateButton = new Button(VaadinIcon.COPY_O.create(), event -> {

        });
        duplicateButton.addThemeVariants(ButtonVariant.LUMO_ICON);

        var deleteButton = new Button(VaadinIcon.CLOSE.create(), event -> {
            dialog.setText("Вы уверены, что хотите удалить шаблон " + bean.getName()+" ?");
            dialog.open();
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
            if (templatesBean.hasChanges()) confirm.open();
            else {
                if (listener.getValue() != null) {
                    Optional<Templates> templFromBackend = templatesService.findById(listener.getValue().getId());
                    if (templFromBackend.isPresent()) {
                        bean = templFromBackend.get();
                        templatesBean.readBean(bean);
                        chainGrid.setItems(templFromBackend.get().getChains());
                    } else Notification.show("Не найдено такого шаблона!");
                }
            }
        });

        templateGrid.addItemDoubleClickListener(__->updateButton.click());
        return vl;
    }

    private void saveBean() {
        if (bean != null) {
            try {
                templatesBean.writeBean(bean);
                templatesService.save(bean);
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
        templatesBean.removeBean();
        templatesBean.refreshFields();
        bean = null;
        templateGrid.select(null);
    }
}
