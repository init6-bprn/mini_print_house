package ru.bprn.printhouse.views.template;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import lombok.Getter;
import lombok.Setter;
import ru.bprn.printhouse.data.entity.WorkFlow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class CalculateWorkflowParameters {

    @Getter
    @Setter
    private BeanValidationBinder<WorkFlow> templateBinder;
    private final TabSheet tabSheet;

    public CalculateWorkflowParameters(TabSheet tabSheet) {
        this.tabSheet = tabSheet;

    }

    public void calculate() {
        int[] mass = {1,1,1};

        var bean = templateBinder.getBean();
        if(bean!= null) {
            var quantity = bean.getQuantityOfProduct();
            var quantityOfPrintLeaf = bean.getQuantityOfPrintLeaves();
            var printSizeX = bean.getPrintSizeX();
            var printSizeY = bean.getPrintSizeY();
            var orient = bean.getOrientation();
            var left = bean.getLeftGap();
            var right = bean.getRightGap();
            var top = bean.getTopGap();
            var bottom = bean.getBottomGap();

            var comp = getListOfComponents(HasMargins.class);
            for (Component c : comp) {
                HasMargins mg = (HasMargins) c;
                if (mg.getMargins() != null) {
                    if (left < mg.getMargins().getGapLeft()) left = mg.getMargins().getGapLeft();
                    if (right < mg.getMargins().getGapRight()) right = mg.getMargins().getGapRight();
                    if (top < mg.getMargins().getGapTop()) top = mg.getMargins().getGapTop();
                    if (bottom < mg.getMargins().getGapBottom()) bottom = mg.getMargins().getGapBottom();
                }
            }

            printSizeX = (double) (bean.getMaterial().getSizeOfPrintLeaf().getLength() - right - left);
            printSizeY = (double) (bean.getMaterial().getSizeOfPrintLeaf().getWidth() - top - bottom);

            var mass1 = getQuantity(printSizeX, printSizeY, bean.getFullProductSizeX(), bean.getFullProductSizeY());
            var mass2 = getQuantity(printSizeX, printSizeY, bean.getFullProductSizeY(), bean.getFullProductSizeX());

            switch (orient) {
                case "Автоматически":
                    if (mass1[2] >= mass2[2]) mass = mass1;
                    else mass = mass2;
                    break;
                case "Вертикальная":
                    mass = mass1;
                    break;
                case "Горизонтальная":
                    mass = mass2;
                    break;
            }

            if (quantity != 0) {
                quantityOfPrintLeaf = quantity / mass[2];
                if (quantity % mass[2] != 0) quantityOfPrintLeaf++;
            } else quantityOfPrintLeaf = 0;

            bean.setQuantityOfPrintLeaves(quantityOfPrintLeaf);
            bean.setListRows(mass[0]);
            bean.setListColumns(mass[1]);
            bean.setQuantityProductionsOnLeaf(mass[2]);
            bean.setRightGap(right);
            bean.setLeftGap(left);
            bean.setTopGap(top);
            bean.setBottomGap(bottom);
            bean.setPrintSizeX(printSizeX);
            bean.setPrintSizeY(printSizeY);

            templateBinder.refreshFields();
        }
    }

    public Optional<List<Component>> getListOfTabs(){
        Optional<Component> component = tabSheet.getChildren().filter(Tabs.class::isInstance).findFirst();
        return component.map(value -> value.getChildren().filter(Tab.class::isInstance).toList());
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

    private int[] getQuantity(double sizeLeafX, double sizeLeafY, Double sizeElementX, Double sizeElementY) {
        int[] mass = new int[3];
        mass[0] = (int) (sizeLeafX/sizeElementX);
        mass[1] = (int) (sizeLeafY/sizeElementY);
        mass[2] = mass[1]*mass[0];
        return mass;
    }

}
