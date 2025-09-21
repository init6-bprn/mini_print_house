package ru.bprn.printhouse.views.operation.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.templates.entity.Variable;
import ru.bprn.printhouse.views.templates.entity.Variable.VariableType;

import java.util.ArrayList;
import java.util.List;

@Service
public class OperationVariableService {

    public List<Variable> getPredefinedVariables() {
        List<Variable> variables = new ArrayList<>();

        variables.add(new Variable("machineTimeFormula", "", "Формула времени оборудования", VariableType.STRING));
        variables.add(new Variable("actionFormula", "", "Формула времени работника", VariableType.STRING));
        variables.add(new Variable("materialFormula", "", "Формула расхода материала", VariableType.STRING));
        variables.add(new Variable("operationWasteFormula", "0", "Формула брака операции", VariableType.STRING));
        variables.add(new Variable("setupWasteFormula", "0", "Формула приладки", VariableType.STRING));

        return variables;
    }
}