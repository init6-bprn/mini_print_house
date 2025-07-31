package ru.bprn.printhouse.views.operation.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.operation.entity.TypeOfOperation;
import ru.bprn.printhouse.views.operation.repository.TypeOfOperationRepository;

import java.util.List;

@Service
public class TypeOfOperationService {
    private final TypeOfOperationRepository repository;

    public TypeOfOperationService(TypeOfOperationRepository typeOfOperationRepository) {
        this.repository = typeOfOperationRepository;
    }

    public TypeOfOperation save(TypeOfOperation typeOfOperation) {return this.repository.save(typeOfOperation);}
    public List<TypeOfOperation> findAll() {return this.repository.findAll();}
    public void delete(TypeOfOperation typeOfOperation) {this.repository.delete(typeOfOperation);}

}
