package ru.bprn.printhouse.views.material.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.material.entity.PrintSheetsMaterial;
import ru.bprn.printhouse.views.material.repository.PrintSheetsMaterialRepository;

import java.util.List;

@Service
public class PrintSheetsMaterialService {
    private final PrintSheetsMaterialRepository repository;

    public PrintSheetsMaterialService(PrintSheetsMaterialRepository repository) {
        this.repository = repository;
    }

    public PrintSheetsMaterial save(PrintSheetsMaterial materials) {
        return this.repository.save(materials);
    }

    public void delete(PrintSheetsMaterial materials) {
        this.repository.delete(materials);
    }

    public List<PrintSheetsMaterial> findAll() {return this.repository.findAll();}

    public List<PrintSheetsMaterial> populate (String str){
        if (str == null) return findAll();
        else return this.repository.search(str);
    }

    public void duplicate(PrintSheetsMaterial bean) {
        if (bean!= null){
            var materials = new PrintSheetsMaterial();
            materials.setName("Дубликат "+bean.getName());
            materials.setSizeX(bean.getSizeX());
            materials.setSizeY(bean.getSizeY());
            materials.setThickness(bean.getThickness());
            materials.setSearchStr(bean.getSearchStr());
            materials.setUnitsOfMeasurement(bean.getUnitsOfMeasurement());
            materials.setTypeOfMaterial(bean.getTypeOfMaterial());
            materials.setAbstractMachines(bean.getAbstractMachines());
            this.repository.saveAndFlush(materials);
        }
    }
}