package ru.bprn.printhouse.views.templates.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.entity.Variable;
import ru.bprn.printhouse.views.templates.entity.Variable.VariableType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class TemplateVariableService {

    public List<Variable> getVariablesFor(Class<? extends Templates> templateClass) {
        if (templateClass.equals(Templates.class)) {
            return getTemplatesVariables();
        }
        return Collections.emptyList();
    }

    private List<Variable> getTemplatesVariables() {
        List<Variable> variables = new ArrayList<>();
        variables.add(new Variable("quantity", 1, "Тираж по умолчанию", Variable.VariableType.INTEGER, "1", "100000", "1", null));
        variables.add(new Variable("round", false, "Математическое округление", Variable.VariableType.BOOLEAN));
        variables.add(new Variable("roundMask",
                "// Пример гибкого округления в зависимости от тиража\n" +
                "if (quantity <= 100) return '#'; // до рублей\n" +
                "if (quantity <= 1000) return '#.#'; // до десятков копеек\n" +
                "return '#.##'; // до копеек",
                "Маска/формула округления", Variable.VariableType.STRING));
        return variables;
    }
}