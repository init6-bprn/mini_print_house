package ru.bprn.printhouse.views.templates.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class Variable {
    private final UUID id = UUID.randomUUID();
    private String key = "Название переменной";
    private Double value = .0;
    private String description = "Описание";
}
