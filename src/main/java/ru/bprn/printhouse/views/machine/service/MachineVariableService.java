package ru.bprn.printhouse.views.machine.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.machine.entity.AbstractMachine;
import ru.bprn.printhouse.views.machine.entity.DigitalPrintingMachine;
import ru.bprn.printhouse.views.templates.entity.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MachineVariableService {

    public List<Variable> getVariablesFor(Class<? extends AbstractMachine> machineClass) {
        if (DigitalPrintingMachine.class.isAssignableFrom(machineClass)) {
            return getDigitalPrintingMachineVariables();
        }
        return Collections.emptyList();
    }

    private List<Variable> getDigitalPrintingMachineVariables() {
        List<Variable> variables = new ArrayList<>();
        variables.add(new Variable("gap_top", "4", "Верхнее непечатное поле, мм", Variable.VariableType.DOUBLE, "0", "50", "1", null));
        variables.add(new Variable("gap_bottom", "4", "Нижнее непечатное поле, мм", Variable.VariableType.DOUBLE, "0", "50", "1", null));
        variables.add(new Variable("gap_left", "4", "Левое непечатное поле, мм", Variable.VariableType.DOUBLE, "0", "50", "1", null));
        variables.add(new Variable("gap_right", "4", "Правое непечатное поле, мм", Variable.VariableType.DOUBLE, "0", "50", "1", null));
        variables.add(new Variable("max_width", "330", "максимальная ширина листа, мм", Variable.VariableType.DOUBLE, "100", "330", "1", null));
        variables.add(new Variable("max_length", "488", "максимальная длина листа, мм", Variable.VariableType.DOUBLE, "100", "1200", "1", null));
        variables.add(new Variable("click_size", "225", "длина одного клика, мм", Variable.VariableType.DOUBLE, "210", "244", "1", null));
        return variables;
    }
}