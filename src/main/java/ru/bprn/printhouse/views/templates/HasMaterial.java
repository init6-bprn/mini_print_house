package ru.bprn.printhouse.views.templates;

import ru.bprn.printhouse.data.entity.Formulas;
import ru.bprn.printhouse.views.material.entity.Material;

import java.util.Set;

public interface HasMaterial {

    Material getMaterial();
    Set<Material> getSelectedMaterials();

    Formulas getMaterialFormula();

}
