package ru.bprn.printhouse.views.templates;

import ru.bprn.printhouse.data.entity.Formulas;
@FunctionalInterface
public interface FormulaSelectionListener {
        void onFormulaSelected(Formulas formula);
}
