package ru.bprn.printhouse.views.products.entity;

import lombok.Getter;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;

import java.util.List;
import java.util.UUID;

@Getter
public class ComponentCalculation {
    private final UUID productTypeId;
    private final String name;

    // Физические величины
    private final double componentWeight; // в граммах

    // Стоимостные величины (в копейках)
    private final long mainMaterialCost;
    private final long operationsCost;
    private final long primeCost; // Себестоимость компонента

    // Справочная информация и детализация
    private final AbstractMaterials mainMaterial;
    private final List<OperationCalculation> operationCalculations;

    public ComponentCalculation(UUID productTypeId, String name, double componentWeight, long mainMaterialCost, List<OperationCalculation> operationCalculations, AbstractMaterials mainMaterial) {
        this.productTypeId = productTypeId;
        this.name = name;
        this.componentWeight = componentWeight;
        this.mainMaterialCost = mainMaterialCost;
        this.operationCalculations = operationCalculations;
        this.mainMaterial = mainMaterial;
        this.operationsCost = operationCalculations.stream().mapToLong(OperationCalculation::getTotalCost).sum();
        this.primeCost = mainMaterialCost + this.operationsCost;
    }
}
