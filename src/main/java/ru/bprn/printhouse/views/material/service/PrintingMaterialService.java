package ru.bprn.printhouse.views.material.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.material.entity.PrintingMaterials;
import ru.bprn.printhouse.views.material.repository.PrintingMaterialsRepository;

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

}