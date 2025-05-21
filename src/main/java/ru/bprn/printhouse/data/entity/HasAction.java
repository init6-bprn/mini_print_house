package ru.bprn.printhouse.data.entity;

public interface HasAction {
    Formulas getActionFormula();
    void setActionFormula(Formulas formula);
    String getActionName();
    boolean haveAction();
}
