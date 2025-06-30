package ru.bprn.printhouse.views.material.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.repository.AbstractMaterialRepository;

import java.util.List;

@Service
public class AbstractMaterialService {

    private final AbstractMaterialRepository repository;

    public AbstractMaterialService(AbstractMaterialRepository repository) {
        this.repository = repository;
    }

    public List<AbstractMaterials> findAll() {
        return this.repository.findAll();
    }

    public List<AbstractMaterials> populate (String str){
        if (str == null) return findAll();
        else return this.repository.search(str);
    }
}
