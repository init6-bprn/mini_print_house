package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.TypeOfMaterial;
import ru.bprn.printhouse.data.repository.TypeOfMaterialRepository;

import java.util.List;

@Service
public class TypeOfMaterialService {
    private TypeOfMaterialRepository typeOfMaterialRepository;

    public TypeOfMaterialService(TypeOfMaterialRepository typeOfMaterialRepository) {
        this.typeOfMaterialRepository = typeOfMaterialRepository;
    }

    public List<TypeOfMaterial> findAll() {
        return this.typeOfMaterialRepository.findAll();
    }

    public void delete(TypeOfMaterial typeOfMaterial) {
        this.typeOfMaterialRepository.delete(typeOfMaterial);
    }

    public TypeOfMaterial save(TypeOfMaterial typeOfMaterial) {
        return this.typeOfMaterialRepository.save(typeOfMaterial);
    }
}
