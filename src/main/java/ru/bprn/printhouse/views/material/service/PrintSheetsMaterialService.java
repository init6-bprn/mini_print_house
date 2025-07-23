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

}