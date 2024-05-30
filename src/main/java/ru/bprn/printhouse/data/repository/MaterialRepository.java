package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.data.entity.Material;
import ru.bprn.printhouse.data.entity.SizeOfPrintLeaf;
import ru.bprn.printhouse.data.entity.Thickness;
import ru.bprn.printhouse.data.entity.TypeOfMaterial;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    List<Material> findAllByTypeOfMaterial(TypeOfMaterial typeOfMaterial);

    List<Material> findAllByTypeOfMaterialAndSizeOfPrintLeafAndThickness(TypeOfMaterial typeOfMaterial,
                                                                         SizeOfPrintLeaf sizeOfPrintLeaf, Thickness thickness);

    //List<Thickness> findAllThicknessByTypeOfMaterial(TypeOfMaterial typeOfMaterial);

    //@Query("SELECT e FROM User e WHERE e.name LIKE %:name%")
    //List<SizeOfPrintLeaf> findAllSizeOfPrintLeafByTypeOfMaterial(TypeOfMaterial typeOfMaterial);
}