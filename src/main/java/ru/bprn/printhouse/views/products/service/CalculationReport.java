package ru.bprn.printhouse.views.products.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Объект, содержащий полные результаты расчета стоимости продукта.
 */
@Getter
@AllArgsConstructor
public class CalculationReport {

    private final long finalPrice; // Итоговая цена в копейках
    private final double totalWeight; // Общий вес в граммах
    private final double totalManufacturingTime; // Общее чистое время изготовления в секундах
    private final String report; // Детальный текстовый отчет о ходе расчета

}