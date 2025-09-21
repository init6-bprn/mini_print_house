package ru.bprn.printhouse.views.products.service;

import lombok.Getter;
import lombok.Setter;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.entity.PrintSheetsMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CalculationResult {

    private PrintSheetsMaterial mainMaterial;
    private int finalSheets;
    private List<OperationResult> operationResults = new ArrayList<>();
    private List<String> errors = new ArrayList<>();

    public void addOperationResult(UUID operationId, double machineTime, double actionTime, double materialAmount, AbstractMaterials operationMaterial) {
        operationResults.add(new OperationResult(operationId, machineTime, actionTime, materialAmount, operationMaterial));
    }

    public void addError(String error) {
        errors.add(error);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    @Getter
    public static class OperationResult {
        private final UUID operationId;
        private final double machineTime; // в секундах
        private final double actionTime; // в секундах
        private final double materialAmount; // в единицах
        private final AbstractMaterials operationMaterial;

        public OperationResult(UUID operationId, double machineTime, double actionTime, double materialAmount, AbstractMaterials operationMaterial) {
            this.operationId = operationId;
            this.machineTime = machineTime;
            this.actionTime = actionTime;
            this.materialAmount = materialAmount;
            this.operationMaterial = operationMaterial;
        }

        @Override
        public String toString() {
            return "OperationResult{" +
                    "operationId=" + operationId +
                    ", machineTime=" + machineTime +
                    ", actionTime=" + actionTime +
                    ", materialAmount=" + materialAmount +
                    ", operationMaterial=" + (operationMaterial != null ? operationMaterial.getName() : "null") +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "CalculationResult{" + "mainMaterial=" + mainMaterial.getName() + ", finalSheets=" + finalSheets + ", operationResults=" + operationResults + ", errors=" + errors + '}';
    }
}