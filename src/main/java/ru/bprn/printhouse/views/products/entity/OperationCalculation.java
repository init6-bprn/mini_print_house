package ru.bprn.printhouse.views.products.entity;

import lombok.Getter;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;

import java.util.UUID;

@Getter
public class OperationCalculation {
    private final UUID productOperationId;
    private final String name;

    // Физические величины
    private final double machineTime; // в секундах
    private final double workerTime;  // в секундах
    private final double materialAmount; // в единицах расходника

    // Стоимостные величины (в копейках)
    private final long machineCost;
    private final long workerCost;
    private final long materialCost;
    private final long totalCost;

    // Справочная информация
    private final AbstractMaterials consumedMaterial;

    public OperationCalculation(UUID productOperationId, String name, double machineTime, double workerTime, double materialAmount, long machineCost, long workerCost, long materialCost, AbstractMaterials consumedMaterial) {
        this.productOperationId = productOperationId;
        this.name = name;
        this.machineTime = machineTime;
        this.workerTime = workerTime;
        this.materialAmount = materialAmount;
        this.machineCost = machineCost;
        this.workerCost = workerCost;
        this.materialCost = materialCost;
        this.totalCost = machineCost + workerCost + materialCost;
        this.consumedMaterial = consumedMaterial;
    }
}
