package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.data.entity.Material;
import ru.bprn.printhouse.data.entity.SizeOfPrintLeaf;
import ru.bprn.printhouse.data.entity.Thickness;
import ru.bprn.printhouse.data.entity.TypeOfMaterial;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    List<Material> findAllByTypeOfMaterial(TypeOfMaterial typeOfMaterial);

    List<Material> findAllByTypeOfMaterialAndSizeOfPrintLeaf(TypeOfMaterial typeOfMaterial,
                                                             SizeOfPrintLeaf sizeOfPrintLeaf);

    List<Material> findAllByTypeOfMaterialAndSizeOfPrintLeafAndThickness(TypeOfMaterial typeOfMaterial,
                                                                         SizeOfPrintLeaf sizeOfPrintLeaf, Thickness thickness);

}