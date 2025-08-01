package ru.bprn.printhouse.views.templates.entity;

import ru.bprn.printhouse.views.material.entity.AbstractMaterials;

import java.util.Set;
import java.util.UUID;

public interface HasMateria {

        UUID getDefaultMaterial();
        void setDefaultMaterial(UUID material);

        Set<UUID> getSelectedMaterials();
        void setSelectedMaterials(Set<UUID> materialSet);

        String getMaterialFormula();
        void setMaterialFormula(String formula);

}
