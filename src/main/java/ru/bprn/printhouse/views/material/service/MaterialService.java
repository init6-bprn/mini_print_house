package ru.bprn.printhouse.views.material.service;

import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.material.entity.Material;
import ru.bprn.printhouse.data.entity.SizeOfPrintLeaf;
import ru.bprn.printhouse.data.entity.Thickness;
import ru.bprn.printhouse.data.entity.TypeOfMaterial;
import ru.bprn.printhouse.views.material.repository.MaterialRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@SpringComponent
@UIScope
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
        if (typeOfMaterial!=null) return this.materialRepository.findAllByTypeOfMaterial(typeOfMaterial);
        else return this.findAll();
    }

    public List<Material> findAllBySize(SizeOfPrintLeaf sizeOfPrintLeaf) {
        if (sizeOfPrintLeaf!=null) return this.materialRepository.findAllBySizeOfPrintLeaf(sizeOfPrintLeaf);
        else return findAll();
    }

    public List<Material> findAllByTypeAndSize(TypeOfMaterial typeOfMaterial, SizeOfPrintLeaf sizeOfPrintLeaf) {
        if (typeOfMaterial!=null)
            if (sizeOfPrintLeaf!=null) return this.materialRepository.findAllByTypeOfMaterialAndSizeOfPrintLeaf(typeOfMaterial, sizeOfPrintLeaf);
            else return this.materialRepository.findAllByTypeOfMaterial(typeOfMaterial);
        else return this.findAll();
    };

    public List<Material> findByFilters(TypeOfMaterial typeOfMaterial, SizeOfPrintLeaf sizeOfPrintLeaf, Thickness thickness){
        if (typeOfMaterial==null) {
            if (sizeOfPrintLeaf==null) {
                if (thickness==null) return materialRepository.findAll();
                else return materialRepository.findAllByThickness(thickness);
            }
            else {
                if (thickness==null) return materialRepository.findAllBySizeOfPrintLeaf(sizeOfPrintLeaf);
                else return materialRepository.findAllBySizeOfPrintLeafAndThickness(sizeOfPrintLeaf, thickness);
            }
        }
        else if (sizeOfPrintLeaf==null) {
                if (thickness==null) return materialRepository.findAllByTypeOfMaterial(typeOfMaterial);
                else return materialRepository.findAllByTypeOfMaterialAndThickness(typeOfMaterial, thickness);
             }
             else if (thickness==null) return  materialRepository.findAllByTypeOfMaterialAndSizeOfPrintLeaf(typeOfMaterial, sizeOfPrintLeaf);
                  else return this.materialRepository.findAllByTypeOfMaterialAndSizeOfPrintLeafAndThickness(typeOfMaterial, sizeOfPrintLeaf, thickness);
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
