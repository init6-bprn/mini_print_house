package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.material.entity.Material;
import ru.bprn.printhouse.data.entity.PriceOfMaterial;
import ru.bprn.printhouse.data.repository.PriceOfMaterialRepository;

import java.util.List;

@Service
public class PriceOfMaterialService {
    private PriceOfMaterialRepository repository;

    public PriceOfMaterialService (PriceOfMaterialRepository repository) {
        this.repository = repository;
    }

    public PriceOfMaterial save(PriceOfMaterial material) {
        return this.repository.save(material);
    }

    public void delete(PriceOfMaterial material) {
        this.repository.delete(material);
    }

    public PriceOfMaterial find(Material material) {
        return this.repository.findOneByMaterial(material);
    }

    public List<PriceOfMaterial> findAll() {
        return this.repository.findAll();
    }

}
