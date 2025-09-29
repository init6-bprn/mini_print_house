package ru.bprn.printhouse.views.operation.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.operation.entity.TypeOfOperation;
import ru.bprn.printhouse.views.operation.repository.OperationRepository;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@Service
public class OperationService {
    private final OperationRepository repository;

    private OperationService(OperationRepository repository) {
        this.repository = repository;
    }

    public List<Operation> findAll() {return this.repository.findAll();}
    public List<Operation> findAllByType(TypeOfOperation type) {return this.repository.findAllByTypeOfOperation(type);}
    public Operation save(Operation bean) {return this.repository.save(bean);}
    public void delete(Operation bean) {this.repository.delete(bean);}
    public List<Operation> populate (String str){
        if (str == null) return findAll();
        else return this.repository.search(str);
    }

    public Optional<Operation> duplicate(Operation bean) {
        if (bean!= null){
            var work = new Operation();
            work.setName(bean.getName() + " (копия)");
            work.setTypeOfOperation(bean.getTypeOfOperation());
            work.setAbstractMachine(bean.getAbstractMachine());
            work.setListOfMaterials(bean.getListOfMaterials());
            work.setDefaultMaterial(bean.getDefaultMaterial());
            work.setVariables(new ArrayList<>(bean.getVariables())); // Глубокое копирование переменных

            work.setMachineTimeFormulaTemplate(bean.getMachineTimeFormulaTemplate());
            work.setMachineTimeExpression(bean.getMachineTimeExpression());
            work.setActionTimeFormulaTemplate(bean.getActionTimeFormulaTemplate());
            work.setActionTimeExpression(bean.getActionTimeExpression());
            work.setMaterialAmountFormulaTemplate(bean.getMaterialAmountFormulaTemplate());
            work.setMaterialAmountExpression(bean.getMaterialAmountExpression());
            work.setWasteFormulaTemplate(bean.getWasteFormulaTemplate());
            work.setWasteExpression(bean.getWasteExpression());
            work.setSetupFormulaTemplate(bean.getSetupFormulaTemplate());
            work.setSetupExpression(bean.getSetupExpression());

            return Optional.of(this.repository.save(work));
        }
        else return Optional.empty();
    }
}
