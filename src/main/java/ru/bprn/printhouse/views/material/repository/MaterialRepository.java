package ru.bprn.printhouse.views.material.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.views.material.entity.Material;
import ru.bprn.printhouse.data.entity.SizeOfPrintLeaf;
import ru.bprn.printhouse.data.entity.Thickness;
import ru.bprn.printhouse.data.entity.TypeOfMaterial;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    List<Material> findAllByTypeOfMaterial(TypeOfMaterial typeOfMaterial);

    List<Material> findAllByTypeOfMaterialAndSizeOfPrintLeaf(TypeOfMaterial typeOfMaterial,
                                                             SizeOfPrintLeaf sizeOfPrintLeaf);

    List<Material> findAllByTypeOfMaterialAndThickness(TypeOfMaterial typeOfMaterial,
                                                             Thickness thickness);

    List<Material> findAllByTypeOfMaterialAndSizeOfPrintLeafAndThickness(TypeOfMaterial typeOfMaterial,
                                                                         SizeOfPrintLeaf sizeOfPrintLeaf, Thickness thickness);

    List<Material> findAllBySizeOfPrintLeaf(SizeOfPrintLeaf sizeOfPrintLeaf);

    //List<Material> findAllByThicknessList(List<Thickness> thicknessList);

    List<Material> findAllByThickness(Thickness thickness);

    List<Material> findAllBySizeOfPrintLeafAndThickness(SizeOfPrintLeaf sizeOfPrintLeaf, Thickness thickness);

}