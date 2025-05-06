package ru.bprn.printhouse.data.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class JSONToObjectsHelper {

    // На вход JSON строка ИмяКласса@Данные-ИмяКласса@Данные-...-ИмяКласса@Данные
    // На выход Список строковых массивов [ИмяКласса][Данные]->[ИмяКласса][Данные]...->[ИмяКласса][Данные]
    private static List<String[]> getParseStringMap(String str) {
        var list = new ArrayList<String[]>();
        if (str!= null) {
            if (!str.isBlank()) {
                String[] arrayStr = str.split("-=+=-");
                for (String s : arrayStr) {
                    list.add(s.split("-=@=-"));
                }
            }
        }
        return list;
    }

    public static String unionAllToOneString(List<String[]> list) {
        var builder = new StringBuilder();
        boolean flag = true;

        for (String[] str: list) {
            if (flag) flag = false;
            else builder.append("-=+=-");
            builder.append(str[0]).append("-=@=-").append(str[1]);
        }
        return builder.toString();
    }

    // Сопоставляю с именем класса и распарсиваю данные в объект соответствующего класса
    public static List<Object> getListOfObjects(String str) {
        var objectMapper = new ObjectMapper();
        var listOfObjects = new ArrayList<>();
        var listOfStr = getParseStringMap(str);

        if (!listOfStr.isEmpty()) {
            for (String[] s : listOfStr)
                try {
                    //Notification.show("Class: "+Class.forName(s[0]));
                      listOfObjects.add(objectMapper.readValue(s[1], Class.forName(s[0])));
                    } catch (JsonProcessingException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
        }
        return listOfObjects;
    }

    // Берем класс, возвращаем массив [Имя класса][JSON]
    public static <T> String[] getBeanAsJSONStr(T bean) {
        var objectMapper = new ObjectMapper();
        String[] mass = new String[2];
        try {
            mass[0] = bean.getClass().getName();
            mass[1] = objectMapper.writeValueAsString(bean);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return mass;
    }

    // Берем JSON и имя класса, возвращаем объект класса
    public static <T> T setBeanFromJSONStr(String str, Class<T> clazz) {
        var objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(str, clazz);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T setBeanFromJSONStr(List<Object> list, Class<T> clazz) {
        T t = null;
        for (Object o : list) {
            t = clazz.isInstance(o) ? clazz.cast(o) : null;
        }
        return t;
    }

    public static <T> List<T> getListOfObjReqType(String str, Class<T> clazz) {
        var list = getListOfObjects(str).stream().filter(clazz::isInstance).toList();
        var output = new LinkedList<T>();

        for (Object obj : list) output.add(clazz.cast(obj));

        return output;
    }
}
