package ru.bprn.printhouse.data;

public abstract class AbstractWork {

    private String name;

    private TypeOfWork typeOfWork;
    private Integer time = 0;
    private Double Cost = 0d;

    public Integer calculateTime() {return time;};

    public Double calculateCost() {return Cost;};


}
