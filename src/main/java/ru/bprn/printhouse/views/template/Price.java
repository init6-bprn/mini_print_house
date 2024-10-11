package ru.bprn.printhouse.views.template;

public interface Price {
    public double getPriceOfOperation();

    public double getPriceOfWork();

    public double getPriceOfAmmo();

    public int getTimeOfOperationPerSec();

    public String getFormula();
}
