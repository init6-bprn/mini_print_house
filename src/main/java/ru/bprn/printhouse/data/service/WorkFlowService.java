package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.WorkFlow;
import ru.bprn.printhouse.data.repository.WorkFlowRepository;
import ru.bprn.printhouse.views.template.ExtraLeaves;
import ru.bprn.printhouse.views.template.HasMargins;
import ru.bprn.printhouse.views.template.Price;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.*;

@Service
public class WorkFlowService {

    private final WorkFlowRepository workFlowRepository;
    private final StringBuilder strVariables = new StringBuilder();


    public WorkFlowService(WorkFlowRepository workFlowRepository){
        this.workFlowRepository = workFlowRepository;
    }

    public List<WorkFlow> findAll() {return this.workFlowRepository.findAll();}

    public WorkFlow save(WorkFlow workFlow) {return this.workFlowRepository.save(workFlow);}

    public void delete (WorkFlow workFlow) {this.workFlowRepository.delete(workFlow);}

    public void calculate(WorkFlow workFlow) {
        int[] mass = {1,1,1};

        if(workFlow!= null) {
            var quantity = workFlow.getQuantityOfProduct();
            var quantityOfPrintLeaf = workFlow.getQuantityOfPrintLeaves();
            var printSizeX = workFlow.getPrintSizeX();
            var printSizeY = workFlow.getPrintSizeY();
            var orient = workFlow.getOrientation();
            var left = workFlow.getLeftGap();
            var right = workFlow.getRightGap();
            var top = workFlow.getTopGap();
            var bottom = workFlow.getBottomGap();

            List<Object> comp = JSONToObjectsHelper.getListOfObjects(workFlow.getStrJSON())
                    .stream().filter(HasMargins.class::isInstance).toList();

            for (Object c : comp) {
                HasMargins mg = (HasMargins) c;
                if (mg.getMargins() != null) {
                    if (left < mg.getMargins().getGapLeft()) left = mg.getMargins().getGapLeft();
                    if (right < mg.getMargins().getGapRight()) right = mg.getMargins().getGapRight();
                    if (top < mg.getMargins().getGapTop()) top = mg.getMargins().getGapTop();
                    if (bottom < mg.getMargins().getGapBottom()) bottom = mg.getMargins().getGapBottom();
                }
            }

            printSizeX = (double) (workFlow.getMaterial().getSizeOfPrintLeaf().getLength() - right - left);
            printSizeY = (double) (workFlow.getMaterial().getSizeOfPrintLeaf().getWidth() - top - bottom);

            var mass1 = getQuantity(printSizeX, printSizeY, workFlow.getFullProductSizeX(), workFlow.getFullProductSizeY());
            var mass2 = getQuantity(printSizeX, printSizeY, workFlow.getFullProductSizeY(), workFlow.getFullProductSizeX());

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

            var extraLeavesQuantity = getExtraLeaves(workFlow);

            if (quantity != 0) {
                quantityOfPrintLeaf = quantity / mass[2];
                if (quantity % mass[2] != 0) quantityOfPrintLeaf++;
                quantityOfPrintLeaf += extraLeavesQuantity;
            } else quantityOfPrintLeaf = 0;

            workFlow.setQuantityOfPrintLeaves(quantityOfPrintLeaf);
            workFlow.setListRows(mass[0]);
            workFlow.setListColumns(mass[1]);
            workFlow.setQuantityProductionsOnLeaf(mass[2]);
            workFlow.setRightGap(right);
            workFlow.setLeftGap(left);
            workFlow.setTopGap(top);
            workFlow.setBottomGap(bottom);
            workFlow.setPrintSizeX(printSizeX);
            workFlow.setPrintSizeY(printSizeY);

            // Предварительный подсчет цены
            strVariables.delete(0, strVariables.length());
            fillVariables(workFlow);
            getOperationPrice(workFlow);
        }
    }

    private int getExtraLeaves(WorkFlow workFlow) {
        var extraLeaves = JSONToObjectsHelper.getListOfObjects(workFlow.getStrJSON())
                .stream().filter(ExtraLeaves.class::isInstance).toList();;
        int i = 0;
        for (Object c : extraLeaves)
            if (((ExtraLeaves) c).getExtraLeaves()>i)
                i = ((ExtraLeaves) c).getExtraLeaves();
        return i;
    }

    private double getOperationPrice(WorkFlow workFlow) {
        double total = 0;
        String expression ;
        var listOfPrice = JSONToObjectsHelper.getListOfObjects(workFlow.getStrJSON())
                .stream().filter(Price.class::isInstance).toList();

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

        for (Object c : listOfPrice){
            Price cost = (Price) c;
            expression = strVariables + addVar("price", cost.getPriceOfOperation()) + ((Price) c).getFormula() +";";
            try {
                total += (Double) engine.eval(expression);
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }

        }
        return total;
    }

    private void fillVariables(WorkFlow workFlow) {
        strVariables
                .append(addVar("columns", workFlow.getListColumns()))
                .append(addVar("rows", workFlow.getListRows()))
                .append(addVar("quantity", workFlow.getQuantityOfProduct()))
                .append(addVar("leaves", workFlow.getQuantityOfPrintLeaves()))
                .append(addVar("onleaf", workFlow.getQuantityProductionsOnLeaf()));
    }

    private String addVar(String name, int i) {
        return name + "=" + i + "; ";
    }

    private String addVar(String name, double i) {
        return name + "=" + i + "; ";
    }

    private int[] getQuantity(double sizeLeafX, double sizeLeafY, Double sizeElementX, Double sizeElementY) {
        int[] mass = new int[3];
        mass[0] = (int) (sizeLeafX/sizeElementX);
        mass[1] = (int) (sizeLeafY/sizeElementY);
        mass[2] = mass[1]*mass[0];
        return mass;
    }

}
