package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.PrintMashine;
import ru.bprn.printhouse.data.entity.PrintSpeedMaterialDensity;
import ru.bprn.printhouse.data.repository.PrintMashineRepository;
import ru.bprn.printhouse.data.repository.PrintSpeedMaterialDensityRepository;
import ru.bprn.printhouse.data.repository.TypeOfMaterialRepository;

import java.util.List;

@Service
public class PrintSpeedMaterialDensityService {
    private PrintMashineRepository printMashineRepository;
    private TypeOfMaterialRepository typeOfMaterialRepository;
    private PrintSpeedMaterialDensityRepository printSpeedMaterialDensityRepository;

    public PrintSpeedMaterialDensityService(PrintSpeedMaterialDensityRepository printSpeedMaterialDensityRepository, PrintMashineRepository printMashineRepository, TypeOfMaterialRepository typeOfMaterialRepository) {
        this.printSpeedMaterialDensityRepository = printSpeedMaterialDensityRepository;
        this.printMashineRepository = printMashineRepository;
        this.typeOfMaterialRepository = typeOfMaterialRepository;
    }

    public List<PrintSpeedMaterialDensity> findAll() {
        return printSpeedMaterialDensityRepository.findAll();
    }

    public List<PrintSpeedMaterialDensity> findPrintSpeedMaterialDensitiesByPrintMashine (PrintMashine printMashine) {
        return this.printSpeedMaterialDensityRepository.findPrintSpeedMaterialDensitiesByPrintMashine(printMashine);
    }

    public PrintSpeedMaterialDensity save(PrintSpeedMaterialDensity printSpeedMaterialDensity){
        return this.printSpeedMaterialDensityRepository.save(printSpeedMaterialDensity);
    }

    public void delete(PrintSpeedMaterialDensity printSpeedMaterialDensity) {
        this.printSpeedMaterialDensityRepository.delete(printSpeedMaterialDensity);
    }
}
