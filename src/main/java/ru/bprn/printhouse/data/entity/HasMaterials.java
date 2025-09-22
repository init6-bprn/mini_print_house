package ru.bprn.printhouse.data.entity;

import ru.bprn.printhouse.views.material.entity.AbstractMaterials;

import java.util.Set;

public interface HasMaterials {
    Set<AbstractMaterials> getListOfMaterials();
    AbstractMaterials getDefaultMaterial();
    String getMaterialFormula();
    boolean haveMaterials();
}
