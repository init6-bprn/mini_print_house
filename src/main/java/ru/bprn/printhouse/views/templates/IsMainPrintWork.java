package ru.bprn.printhouse.views.templates;

public interface IsMainPrintWork {

    Integer getLeafSizeX();
    Integer getLeafSizeY();
    Integer getPrintAreaX();
    Integer getPrintAreaY();
    String getOrientation();
    Integer getQuantityOfExtraLeaves();


    //public Gap getMargins();


}
