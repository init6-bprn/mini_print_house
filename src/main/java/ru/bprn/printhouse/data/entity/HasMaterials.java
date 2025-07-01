package ru.bprn.printhouse.data.entity;

import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.entity.Material;

import java.util.Set;

public interface HasMaterials {
    Set<AbstractMaterials> getListOfMaterials();
    AbstractMaterials getDefaultMaterial();
    Formulas getMaterialFormula();
    boolean haveMaterials();
}
