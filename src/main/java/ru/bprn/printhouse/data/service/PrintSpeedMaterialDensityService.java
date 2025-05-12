package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.*;
import ru.bprn.printhouse.data.repository.PrintSpeedMaterialDensityRepository;

import java.util.List;

@Service
public class PrintSpeedMaterialDensityService {
    private final PrintSpeedMaterialDensityRepository printSpeedMaterialDensityRepository;

    public PrintSpeedMaterialDensityService(PrintSpeedMaterialDensityRepository printSpeedMaterialDensityRepository) {
        this.printSpeedMaterialDensityRepository = printSpeedMaterialDensityRepository;
    }

    public List<PrintSpeedMaterialDensity> findAll() {
        return printSpeedMaterialDensityRepository.findAll();
    }

    public int findTimeOfOperation(PrintMashine printMashine, Thickness thickness, SizeOfPrintLeaf sizeOfPrintLeaf) {
        return this.printSpeedMaterialDensityRepository.findByPrintMashineAndThicknessAndSizeOfPrintLeaf(printMashine,thickness,sizeOfPrintLeaf).getTimeOfOperation();
    }

    public List<PrintSpeedMaterialDensity> findPrintSpeedMaterialDensitiesByPrintMashine (PrintMashine printMashine) {
        if (printMashine!= null) return this.printSpeedMaterialDensityRepository.findPrintSpeedMaterialDensitiesByPrintMashine(printMashine);
        else return this.findAll();
    }

    public PrintSpeedMaterialDensity save(PrintSpeedMaterialDensity printSpeedMaterialDensity){
        return this.printSpeedMaterialDensityRepository.save(printSpeedMaterialDensity);
    }

    public void delete(PrintSpeedMaterialDensity printSpeedMaterialDensity) {
        this.printSpeedMaterialDensityRepository.delete(printSpeedMaterialDensity);
    }
}
