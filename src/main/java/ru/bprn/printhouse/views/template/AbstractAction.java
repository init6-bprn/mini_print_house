package ru.bprn.printhouse.views.template;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractAction {

    private ObjectMapper objectMapper = new ObjectMapper();
    private Double priceOfMatter = .0;
    private Double priceOfperation = .0;
    private Double priceOfWork = .0;


    public <T> String[] getBeanAsJSONStr(T bean) {
        String[] mass = new String[1];
        try {
            String str = objectMapper.writeValueAsString(bean);
            mass[0] = this.getClass().getSimpleName();
            mass[1] = str;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return mass;
    }

    public <T> T setBeanFromJSONStr(String str, Class<T> clazz) {
        try {
            return objectMapper.readValue(str, clazz);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getFormula(){return "";}



}
