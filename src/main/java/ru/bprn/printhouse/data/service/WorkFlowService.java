package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.WorkFlow;
import ru.bprn.printhouse.data.repository.WorkFlowRepository;
import ru.bprn.printhouse.views.templates.ExtraLeaves;
import ru.bprn.printhouse.views.templates.Price;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.util.List;

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
/*
    public void calcWorkflowParameters(WorkFlow workFlow) {
        String orient = "";
        Integer bleeds = workFlow.getBleed().getGapLeft() * 2;
        var list = JSONToObjectsHelper.getListOfObjReqType(workFlow.getStrJSON(), IsMainPrintWork.class);
        if (list.isEmpty()) Notification.show("Empty!!!");
        for (IsMainPrintWork equipment : list) {
            workFlow.setPrintSizeX(equipment.getLeafSizeX().doubleValue());
            workFlow.setPrintSizeY(equipment.getLeafSizeY().doubleValue());
            workFlow.setPrintAreaX(equipment.getPrintAreaX().doubleValue());
            workFlow.setPrintAreaY(equipment.getPrintAreaY().doubleValue());
            //workFlow.set
            orient = equipment.getOrientation();
        }
        int[] mass = {1,1,1};


            var quantity = workFlow.getQuantityOfProduct();
            var quantityOfPrintLeaf = workFlow.getQuantityOfPrintLeaves();

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

            var mass1 = getQuantity(workFlow.getPrintAreaX(), workFlow.getPrintAreaY(),
                    workFlow.getSizeX()+bleeds, workFlow.getSizeY()+bleeds);
            var mass2 = getQuantity(workFlow.getPrintAreaX(), workFlow.getPrintAreaY(),
                    workFlow.getSizeY()+bleeds, workFlow.getSizeX()+bleeds);

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
            workFlow.setRowsOnLeaf(mass[0]);
            workFlow.setColumnsOnLeaf(mass[1]);
            workFlow.setQuantityProductionsOnLeaf(mass[2]);

            // Предварительный подсчет цены
            strVariables.delete(0, strVariables.length());
            fillVariables(workFlow);
            getOperationPrice(workFlow);



    }
*/
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
/*
    private void fillVariables(WorkFlow workFlow) {
        strVariables
                .append(addVar("columns", workFlow.getColumnsOnLeaf()))
                .append(addVar("rows", workFlow.getRowsOnLeaf()))
                .append(addVar("quantity", workFlow.getQuantityOfProduct()))
                .append(addVar("leaves", workFlow.getQuantityOfPrintLeaves()))
                .append(addVar("onleaf", workFlow.getQuantityProductionsOnLeaf()));
    }
*/
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
