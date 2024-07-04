package ru.bprn.printhouse.views.template;

public interface HasBinder {

    public Boolean isValid();

    public String getVolumeAsString();

    public void setVolumeAsString(String str);
}
