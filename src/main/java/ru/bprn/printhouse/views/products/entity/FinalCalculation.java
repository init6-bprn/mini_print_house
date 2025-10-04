package ru.bprn.printhouse.views.products.entity;

import lombok.Getter;

import java.util.List;

@Getter
public class FinalCalculation {
    // Итоговые финансовые показатели
    private final long finalPrice; // Итоговая цена для клиента в копейках
    private final long primeCost;  // Общая себестоимость в копейках

    // Итоговые физические показатели
    private final double totalWeight; // Общий вес в граммах
    private final double totalManufacturingTime; // Общее чистое время изготовления в секундах

    // Детализация для отчета
    private final List<ComponentCalculation> componentCalculations;
    private final List<String> errors;

    public FinalCalculation(long finalPrice, List<ComponentCalculation> componentCalculations, List<String> errors) {
        this.finalPrice = finalPrice;
        this.componentCalculations = componentCalculations;
        this.errors = errors;

        this.primeCost = componentCalculations.stream()
                .mapToLong(ComponentCalculation::getPrimeCost)
                .sum();

        this.totalWeight = componentCalculations.stream()
                .mapToDouble(ComponentCalculation::getComponentWeight)
                .sum();

        this.totalManufacturingTime = componentCalculations.stream()
                .flatMap(c -> c.getOperationCalculations().stream())
                .mapToDouble(op -> Math.max(op.getMachineTime(), op.getWorkerTime()))
                .sum();
    }

    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
}