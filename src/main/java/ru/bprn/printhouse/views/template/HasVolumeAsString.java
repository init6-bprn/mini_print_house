package ru.bprn.printhouse.views.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.bprn.printhouse.data.entity.WorkFlow;

public interface HasVolumeAsString {
    public String getVolumeAsString();

    public void setVolumeAsString(String str);
}
