package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.Material;
import ru.bprn.printhouse.data.repository.MaterialRepository;

import java.util.List;

@Service
public class MaterialService {

    private MaterialRepository materialRepository;

    public MaterialService (MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    public List<Material> findAll() {
        return this.materialRepository.findAll();
    }

    public Material save(Material material) {
        return  this.materialRepository.save(material);
    }

    public void delete(Material material) {
        this.materialRepository.delete(material);
    }
}
