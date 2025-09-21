package ru.bprn.printhouse.views.price.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.price.entity.PriceOfMaterial;
import ru.bprn.printhouse.views.price.repository.PriceOfMaterialRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class PriceOfMaterialService {
    private final PriceOfMaterialRepository priceOfMaterialRepository;

    public List<PriceOfMaterial> findAll() {
        return priceOfMaterialRepository.findAll();
    }

    public List<PriceOfMaterial> findByMaterial(AbstractMaterials material) {
        if (material == null) {
            return findAll();
        }
        return priceOfMaterialRepository.findByMaterial(material);
    }

    public PriceOfMaterial save(PriceOfMaterial priceOfMaterial) {
        return priceOfMaterialRepository.save(priceOfMaterial);
    }

    public void delete(PriceOfMaterial priceOfMaterial) {
        priceOfMaterialRepository.delete(priceOfMaterial);
    }
}