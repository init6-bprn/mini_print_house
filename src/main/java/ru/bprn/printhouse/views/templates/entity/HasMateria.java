package ru.bprn.printhouse.views.templates.entity;

import ru.bprn.printhouse.views.material.entity.AbstractMaterials;

import java.util.Set;

public interface HasMateria {

        AbstractMaterials getDefaultMaterial();
        void setDefaultMaterial(AbstractMaterials material);

        Set<AbstractMaterials> getSelectedMaterials();
        void setSelectedMaterials(Set<AbstractMaterials> materialSet);

        String getMaterialFormula();
        void setMaterialFormula(String formula);

}
