package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;

public abstract class WorkChainView extends SplitLayout {

    private Grid<? extends WorkChain> templateGrid;
    private final Class<? extends WorkChain> clazz;

    public WorkChainView(Class<? extends WorkChain> clazz){
        super();
        this.clazz = clazz;
        templateGrid = new Grid<>(clazz, false);
        this.setOrientation(Orientation.VERTICAL);
        addToPrimary(addGridSection());
        addToSecondary(addTabSheetSection());

    }

    private VerticalLayout addGridSection(){
        var vl = new VerticalLayout();
        vl.setSizeUndefined();

        var dialog = new ConfirmDialog("Внимание!" , "Вы уверены, что хотите удалить эту цепочку?", "Да", confirmEvent ->
        {
            var workflw = templateGrid.getSelectedItems().stream().findFirst();

            //workFlowService.delete(workflw.get());
            //workflw.ifPresent(workChain -> templateGrid.getListDataView().removeItem(workChain));
        },
                "Нет", cancelEvent -> cancelEvent.getSource().close());
/*
        var hl = new HorizontalLayout();
        var createButton = new Button(VaadinIcon.PLUS.create(), buttonClickEvent -> {
            this.getPrimaryComponent().setVisible(false);
            this.getSecondaryComponent().getElement().setEnabled(true);
            this.setSplitterPosition(0);
            startTab.getTemplateBinder().setBean(new WorkFlow());
            //calc.setTemplateBinder(startTab.getTemplateBinder());
        });
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var updateButton  = new Button(VaadinIcon.EDIT.create(), buttonClickEvent -> {
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

        var duplicateButton = new Button(VaadinIcon.COPY_O.create(), buttonClickEvent -> {
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

        var deleteButton = new Button(VaadinIcon.CLOSE.create(), buttonClickEvent -> {
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
        */
        return vl;
    }

    private Component addTabSheetSection() {
        return new VerticalLayout();
    }




}
