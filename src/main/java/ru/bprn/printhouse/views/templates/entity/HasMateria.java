package ru.bprn.printhouse.views.templates.entity;

import ru.bprn.printhouse.views.material.entity.AbstractMaterials;

import java.util.Set;

public interface HasMateria {

        AbstractMaterials getDefaultMat();

        Set<AbstractMaterials> getSelectedMat();

        String getMatFormula();

}
