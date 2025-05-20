package ru.bprn.printhouse.data.entity;

import java.util.List;

public interface HasMaterials {
    List<Material> getListOfMaterials();
    Material getDefaultMaterial();
    String getMaterialFormula();
    boolean haveMaterials();
}
