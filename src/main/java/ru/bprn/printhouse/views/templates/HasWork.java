package ru.bprn.printhouse.views.templates;

import ru.bprn.printhouse.data.entity.User;

public interface HasWork {

    public User getWorker();

    public void setUser(User user);

    public void getTimeOfPreOperationWork();

    public boolean hasPreOperation();

    public String getCalculateTimeOfWorkFormula();


}
