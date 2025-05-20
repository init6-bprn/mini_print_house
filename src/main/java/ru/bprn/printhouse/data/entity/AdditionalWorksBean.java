package ru.bprn.printhouse.data.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdditionalWorksBean implements HasAction, HasMaterials{

    private Long id;

    private String name;

    private TypeOfWork typeOfWork;

    private List<String> parentClassList;

    private String actionFormula;

    private boolean haveAction = true;

    private List<Material> listOfMaterials;

    private Material defaultMaterial;

    private String materialFormula;

    private boolean haveMaterial = true;


    @Override
    public String getActionFormula() {
        return actionFormula;
    }

    @Override
    public void setActionFormula(String formula) {
        this.actionFormula = formula;
    }

    @Override
    public String getActionName() {
        return name;
    }

    @Override
    public boolean haveAction() {
        return haveAction;
    }

    @Override
    public List<Material> getListOfMaterials() {
        return listOfMaterials;
    }

    @Override
    public Material getDefaultMaterial() {
        return defaultMaterial;
    }

    @Override
    public String getMaterialFormula() {
        return materialFormula;
    }

    @Override
    public boolean haveMaterials() {
        return haveMaterial;
    }
}
