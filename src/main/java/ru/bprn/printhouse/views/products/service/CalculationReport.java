package ru.bprn.printhouse.views.products.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CalculationReport {
    private long totalPriceInKopecks;
    private String description;
}