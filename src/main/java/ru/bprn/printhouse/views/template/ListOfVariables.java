package ru.bprn.printhouse.views.template;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ListOfVariables {

    private List<VariablesRecord> list;

    public ListOfVariables(){
        list = new ArrayList<>();
        populate();
    }

    private void populate() {
        list.add(new VariablesRecord("columns","Колонок на листе"));
        list.add(new VariablesRecord("rows","Строк на листе"));
        list.add(new VariablesRecord("quantity","Тираж"));
        list.add(new VariablesRecord("leaves","Листаж"));
        list.add(new VariablesRecord("onleaf","Изделий на листе"));
    }

}
