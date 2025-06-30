package ru.bprn.printhouse.data.entity;

import ru.bprn.printhouse.views.material.entity.Material;

import java.util.Set;

public interface HasMaterials {
    Set<Material> getListOfMaterials();
    Material getDefaultMaterial();
    Formulas getMaterialFormula();
    boolean haveMaterials();
}
