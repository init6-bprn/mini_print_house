package ru.bprn.printhouse.views.template;

import ru.bprn.printhouse.data.entity.Formulas;
import ru.bprn.printhouse.data.entity.Material;

import java.util.Set;

public interface HasMaterial {

    String getDescription();
    void setDescription(String str);

    Material getDefaultMaterial();
    Set<Material> getSelectedMaterials();

    Formulas getMaterialFormula();

}
