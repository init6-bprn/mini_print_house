package ru.bprn.printhouse.data.entity;

public interface HasAction {
    String getActionFormula();
    String getDescription();
    String getActionName();
    boolean haveAction();
}
