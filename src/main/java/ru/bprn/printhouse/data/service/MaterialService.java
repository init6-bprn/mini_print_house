package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.Material;
import ru.bprn.printhouse.data.entity.SizeOfPrintLeaf;
import ru.bprn.printhouse.data.entity.Thickness;
import ru.bprn.printhouse.data.entity.TypeOfMaterial;
import ru.bprn.printhouse.data.repository.MaterialRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MaterialService {

    private MaterialRepository materialRepository;

    public MaterialService (MaterialRepository materialRepository) {
        this.materialRepository = materialRepository;
    }

    public List<Material> findAll() {
        return this.materialRepository.findAll();
    }

    public  List<Thickness> findAllThicknessByTypeOfMaterial(TypeOfMaterial typeOfMaterial){
        List<Material> list = findAllByType(typeOfMaterial);
        Set<Thickness> newList = new HashSet<>();
        for (Material material:list) {
            if (!newList.contains(material)) newList.add(material.getThickness());
        }
        return newList.stream().toList();
    }

    public List<Material> findAllByType(TypeOfMaterial typeOfMaterial) {
        return this.materialRepository.findAllByTypeOfMaterial(typeOfMaterial);
    }

    public List<Material> findByFilters(TypeOfMaterial typeOfMaterial, SizeOfPrintLeaf sizeOfPrintLeaf, Thickness thickness){
        return this.materialRepository.findAllByTypeOfMaterialAndSizeOfPrintLeafAndThickness(typeOfMaterial, sizeOfPrintLeaf, thickness);
    };

    public List<SizeOfPrintLeaf> findAllSizeOfPrintLeafByTypeOfMaterial(TypeOfMaterial typeOfMaterial) {
        List<Material> list = findAllByType(typeOfMaterial);
        Set<SizeOfPrintLeaf> newList = new HashSet<>();
        for (Material material:list) {
            if (!newList.contains(material)) newList.add(material.getSizeOfPrintLeaf());
        }
        return newList.stream().toList();
    }

    public Material save(Material material) {
        return  this.materialRepository.save(material);
    }

    public void delete(Material material) {
        this.materialRepository.delete(material);
    }
}
