package ru.bprn.printhouse.data.entity;

import java.util.Set;

public interface HasMaterials {
    Set<Material> getListOfMaterials();
    Material getDefaultMaterial();
    Formulas getMaterialFormula();
    boolean haveMaterials();
}
