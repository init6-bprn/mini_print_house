package ru.bprn.printhouse.views.template;

public interface HasBinder {

    public Boolean isValid();

    public String getBeanAsString();

    public void setBeanFromString(String str);
}
