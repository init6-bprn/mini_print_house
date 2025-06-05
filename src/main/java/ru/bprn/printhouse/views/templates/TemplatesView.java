package ru.bprn.printhouse.views.templates;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.provider.hierarchy.TreeData;
import com.vaadin.flow.data.provider.hierarchy.TreeDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ru.bprn.printhouse.views.MainLayout;
import ru.bprn.printhouse.views.templates.entity.AbstractTemplate;
import ru.bprn.printhouse.views.templates.entity.Chains;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.service.ChainsService;
import ru.bprn.printhouse.views.templates.service.TemplatesService;

import java.util.ArrayList;
import java.util.Collection;

@PageTitle("Редактирование шаблонов")
@Route(value = "templates", layout = MainLayout.class)
@AnonymousAllowed
public class TemplatesView extends SplitLayout {

    private final TemplatesService templatesService;
    private final ChainsService chainsService;
    private final BeanValidationBinder<Templates> templatesBinder;
    private final BeanValidationBinder<Chains> chainBinder;

    private final TreeGrid<AbstractTemplate> chainGrid = new TreeGrid<>(AbstractTemplate.class, false);

    private Templates beanForTempl;

    private final TemplateEditor templateEditor;
    private final ChainEditor chainEditor;

    public TemplatesView(TemplatesService templatesService, ChainsService chainsService){
        this.templatesService = templatesService;
        this.chainsService = chainsService;
        templatesBinder = new BeanValidationBinder<>(Templates.class);
        templatesBinder.setChangeDetectionEnabled(true);

        chainBinder = new BeanValidationBinder<>(Chains.class);

        templateEditor = new TemplateEditor(this, chainGrid, templatesService);
        templateEditor.setVisible(false);
        templateEditor.setEnabled(false);

        chainEditor = new ChainEditor(this, chainGrid, chainsService, templatesService);
        chainEditor.setVisible(false);
        chainEditor.setEnabled(false);

        this.setOrientation(Orientation.VERTICAL);
        this.setSplitterPosition(50.0);
        this.setSizeFull();
        this.addToPrimary(chainGrid());
        this.addToSecondary(templateEditor, chainEditor);


    }

    private Component chainGrid() {
        var hlay = new HorizontalLayout();
        hlay.setWidthFull();
        chainGrid.setWidthFull();

        var vl = new VerticalLayout();
        vl.setWidthFull();

        var dialogChain = new ConfirmDialog("Внимание!" , "", "Да",
                confirmEvent -> {
                    deleteElement();
                    Notification.show("Элемент удален!");
                },
                "Нет", cancelEvent -> cancelEvent.getSource().close());

        var hl = new HorizontalLayout();
        var createTemplateButton = new Button(VaadinIcon.PLUS.create(), event -> {
            beanForTempl = new Templates();
            templateEditor.setTemplate(beanForTempl);
            hideTemplateAndShowChain(false);
        });
        createTemplateButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createTemplateButton.setTooltipText("Создать новый шаблон");

        var createChainButton = new Button(VaadinIcon.PLUS.create(), event -> {
            var abs = chainGrid.asSingleSelect().getValue();
            if (abs!=null) {
                if (abs instanceof Templates) beanForTempl = (Templates) abs;
                else beanForTempl = (Templates) chainGrid.getDataCommunicator().getParentItem(abs);
                chainEditor.setTemplate(beanForTempl);
                chainEditor.setChains(new Chains());
                hideTemplateAndShowChain(true);
            }
            else Notification.show("Сначала выделите шаблон");
        });
        createChainButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createChainButton.setTooltipText("Создать новую рабочую цепочку");

        var updateButton  = new Button(VaadinIcon.EDIT.create(), event -> {
            if (!chainGrid.asSingleSelect().isEmpty()) {
                switch (chainGrid.asSingleSelect().getValue()) {
                    case Templates template:
                        beanForTempl = template;
                        templateEditor.setTemplate(template);
                        hideTemplateAndShowChain(false);
                        break;
                    case Chains chain:
                        beanForTempl=(Templates) chainGrid.getDataCommunicator().getParentItem(chain);
                        chainEditor.setTemplate(beanForTempl);
                        chainEditor.setChains(chain);
                        hideTemplateAndShowChain(true);
                        break;
                    default:
                }
            }
        });
        updateButton.addThemeVariants(ButtonVariant.LUMO_ICON);

        var duplicateButton = new Button(VaadinIcon.COPY_O.create(), event -> {

        });
        duplicateButton.addThemeVariants(ButtonVariant.LUMO_ICON);

        var deleteButton = new Button(VaadinIcon.CLOSE.create(), event -> {
            if (!chainGrid.asSingleSelect().isEmpty()) {
                dialogChain.setText("Вы уверены, что хотите удалить " + chainGrid.asSingleSelect().getValue().getName() + " ?");
                dialogChain.open();
            }
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        hl.add(createTemplateButton, createChainButton, updateButton, duplicateButton, deleteButton);
        vl.add(hl, chainGrid);

        chainGrid.addHierarchyColumn(AbstractTemplate::getName);
        chainGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        chainGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        populateGrid();

        chainGrid.asSingleSelect().addValueChangeListener(e->{
            if (e.getValue()!= null) {
                switch (e.getValue()) {
                    case Templates template:
                        beanForTempl = template;
                        templateEditor.setTemplate(template);
                        chainEditor.setVisible(false);
                        templateEditor.setVisible(true);
                        break;
                    case Chains chain:
                        if (beanForTempl==null) beanForTempl= (Templates) chainGrid.getDataCommunicator().getParentItem(chain);
                        chainEditor.setChains(chain);
                        chainEditor.setTemplate(beanForTempl);
                        templateEditor.setVisible(false);
                        chainEditor.setVisible(true);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + e.getValue());
                }
            }
        });


        hlay.add(vl);
        return hlay;
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

    private void populateGrid() {
        Collection<AbstractTemplate> collection = templatesService.findAllAsAbstractTemplates();
        TreeData<AbstractTemplate> data = new TreeData<>();
        data.addItems(null, collection);
        for (AbstractTemplate temp : collection) {
            if (temp instanceof Templates) {
                Templates t = (Templates) temp;
                Collection<AbstractTemplate> c = new ArrayList<>(t.getChains());
                data.addItems(temp, c);
            }
        }
        TreeDataProvider<AbstractTemplate> treeData = new TreeDataProvider<>(data);
        chainGrid.setDataProvider(treeData);
    }


    private void deleteElement(){
        var abstractTemplate = chainGrid.asSingleSelect().getValue();
        switch (abstractTemplate) {
            case Templates template:
                templatesService.delete(template);
                break;
            case Chains chain:
                beanForTempl= (Templates) chainGrid.getDataCommunicator().getParentItem(chain);
                var data = beanForTempl.getChains();
                data.remove(chain);
                beanForTempl.setChains(data);
                templatesService.save(beanForTempl);
                break;
            default:
        }
        populateGrid();
    }
}
