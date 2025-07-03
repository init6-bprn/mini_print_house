package ru.bprn.printhouse.views.material.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.bprn.printhouse.data.entity.SizeOfPrintLeaf;
import ru.bprn.printhouse.data.entity.Thickness;
import ru.bprn.printhouse.data.entity.TypeOfMaterial;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class PrintSheetsMaterial extends AbstractMaterials{

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_of_material", nullable = false )
    private TypeOfMaterial typeOfMaterial;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "size_of_print_leaf", nullable = false)
    private SizeOfPrintLeaf sizeOfPrintLeaf;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "thickness", nullable = false )
    private Thickness thickness;

}
