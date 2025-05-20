package ru.bprn.printhouse.data.entity;

public interface HasAction {
    String getActionFormula();
    void setActionFormula(String formula);
    String getActionName();
    boolean haveAction();
}
