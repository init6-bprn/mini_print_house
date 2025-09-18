package ru.bprn.printhouse.views.templates.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Variable {

    public enum VariableType {
        STRING("Текст"),
        INTEGER("Целое число"),
        DOUBLE("Дробное число"),
        BOOLEAN("Да/Нет");

        private final String description;

        VariableType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
    
    private UUID id = UUID.randomUUID();
    private String key;
    private String value; // Храним значение как строку для универсальности
    private String description;
    private VariableType type = VariableType.STRING; // Тип по умолчанию
    private boolean show = true; // Показывать ли в карточке продукта для редактирования

    // Поля для ограничений
    private String minValue; // Для чисел: min value; Для строк: min length
    private String maxValue; // Для чисел: max value; Для строк: max length
    private String step;     // Для чисел: step
    private String pattern;  // Для строк: regex-шаблон (допустимые символы)

    public Variable(String key, Object value, String description, VariableType type) {
        this.key = key;
        this.description = description;
        this.type = type;
        this.show = true; // По умолчанию показываем
        this.setValue(value); // Используем сеттер для корректной установки значения
    }

    // Конструктор для обратной совместимости, если где-то используется
    public Variable(String key, Double value, String description) {
        this.key = key;
        this.description = description;
        this.type = VariableType.DOUBLE;
        this.setValue(value);
        this.show = true; // По умолчанию показываем
    }

    /**
     * Конструктор для создания переменной сразу с ограничениями.
     * @param key Ключ переменной
     * @param value Значение
     * @param description Описание
     * @param type Тип
     * @param minValue Минимальное значение (или длина)
     * @param maxValue Максимальное значение (или длина)
     * @param step Шаг для числовых полей
     * @param pattern Regex-шаблон для строковых полей
     */
    public Variable(String key, Object value, String description, VariableType type, String minValue, String maxValue, String step, String pattern) {
        this(key, value, description, type); // Вызов основного конструктора
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.step = step;
        this.pattern = pattern;
    }


    // Конструктор копирования для глубокого копирования
    public Variable(Variable original) {
        this.id = UUID.randomUUID(); // Новый ID для новой переменной
        this.key = original.key;
        this.value = original.value;
        this.description = original.description;
        this.type = original.type;
        this.show = original.show;
        this.minValue = original.minValue;
        this.maxValue = original.maxValue;
        this.step = original.step;
        this.pattern = original.pattern;
    }

    @JsonIgnore
    public Object getValueAsObject() {
        if (value == null || value.isBlank()) {
            return switch (type) {
                case INTEGER -> 0;
                case DOUBLE -> 0.0;
                case BOOLEAN -> false;
                case STRING -> value; // Can be null or empty string
            };
        }
        try {
            return switch (type) {
                case STRING -> value;
                case INTEGER -> Integer.parseInt(value);
                case DOUBLE -> Double.parseDouble(value.replace(',', '.'));
                case BOOLEAN -> Boolean.parseBoolean(value);
            };
        } catch (NumberFormatException e) {
            return getValueAsObjectOnParseError();
        }
    }

    public void setValue(Object value) {
        if (value == null) {
            this.value = null;
        } else {
            this.value = String.valueOf(value);
        }
    }

    @JsonIgnore
    private Object getValueAsObjectOnParseError() {
        // Возвращаем значение по умолчанию для типа, если парсинг не удался
        return switch (type) {
            case INTEGER -> 0;
            case DOUBLE -> 0.0;
            default -> null; // For BOOLEAN and STRING, this path is unlikely but safe.
        };
    }
}
