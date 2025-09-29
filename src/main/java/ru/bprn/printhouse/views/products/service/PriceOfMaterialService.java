package ru.bprn.printhouse.views.products.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.products.entity.PriceOfMaterial;
import ru.bprn.printhouse.views.products.repository.PriceOfMaterialRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    /**
     * Возвращает последнюю актуальную цену для указанного материала.
     * Если цена не найдена, возвращает 0.
     * @param material Материал, для которого ищется цена.
     * @return BigDecimal цена или BigDecimal.ZERO.
     */
    public BigDecimal getActualPriceFor(AbstractMaterials material) {
        return priceOfMaterialRepository.findTopByMaterialAndEffectiveDateLessThanEqualOrderByEffectiveDateDesc(material, LocalDate.now())
                .map(price -> BigDecimal.valueOf(price.getPrice()))
                .orElse(BigDecimal.ZERO);
    }
}