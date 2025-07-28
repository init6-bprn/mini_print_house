package ru.bprn.printhouse.views.material.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.machine.entity.DigitalPrintingMachine;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;
import ru.bprn.printhouse.views.material.entity.PrintingMaterials;
import ru.bprn.printhouse.views.material.repository.PrintingMaterialsRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class PrintingMaterialService {
    private final PrintingMaterialsRepository repository;

    public PrintingMaterialService(PrintingMaterialsRepository repository) {
        this.repository = repository;
    }

    public PrintingMaterials save(PrintingMaterials materials) {
        return this.repository.save(materials);
    }

    public void delete(PrintingMaterials materials) {
        this.repository.delete(materials);
    }

    public List<PrintingMaterials> findAll() {return this.repository.findAll();}

    public List<AbstractMaterials> findAllAsAbstract() {return new ArrayList<>(this.repository.findAll());}

    public List<PrintingMaterials> populate (String str){
        if (str == null) return findAll();
        else return this.repository.search(str);
    }

    public void duplicate(PrintingMaterials bean) {
        if (bean!= null){
            var materials = new PrintingMaterials();
            materials.setName("Дубликат "+bean.getName());
            materials.setSizeOfClick(bean.getSizeOfClick());
            materials.setSearchStr(bean.getSearchStr());
            materials.setUnitsOfMeasurement(bean.getUnitsOfMeasurement());
            this.repository.saveAndFlush(materials);
        }
    }

}