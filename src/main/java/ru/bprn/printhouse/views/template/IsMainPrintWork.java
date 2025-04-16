package ru.bprn.printhouse.views.template;

public interface IsMainPrintWork {

    Integer getLeafSizeX();
    Integer getLeafSizeY();
    Integer getPrintAreaX();
    Integer getPrintAreaY();
    String getOrientation();
    Integer getQuantityOfExtraLeaves();


    //public Gap getMargins();


}
