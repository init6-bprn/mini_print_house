package ru.bprn.printhouse.views.operation.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.util.ArrayList;
import java.util.List;

@Service
public class OperationVariableService {

    public List<Variable> getPredefinedVariables() {
        List<Variable> variables = new ArrayList<>();
        // В будущем здесь могут быть другие предопределенные переменные для операций, например, "сложность_резки" и т.д.
        return variables;
    }
}