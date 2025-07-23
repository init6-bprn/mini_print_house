package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bprn.printhouse.data.entity.PrintMashine;
import ru.bprn.printhouse.data.repository.PrintMashineRepository;
import ru.bprn.printhouse.data.repository.QuantityColorsRepository;
import ru.bprn.printhouse.data.repository.TypeOfPrinterRepository;

import java.util.List;

@Service
@Transactional
public class DigitalPrintingMachineService {
    private final PrintMashineRepository printMashineRepository;
    private QuantityColorsRepository quantityColorsRepository;
    private TypeOfPrinterRepository typeOfPrinterRepository;

    public DigitalPrintingMachineService(PrintMashineRepository printMashineRepository, QuantityColorsRepository quantityColorsRepository, TypeOfPrinterRepository typeOfPrinterRepository) {
        this.printMashineRepository = printMashineRepository;
        this.quantityColorsRepository = quantityColorsRepository;
        this.typeOfPrinterRepository = typeOfPrinterRepository;
    }
    public List<PrintMashine> findAll(){
        return  printMashineRepository.findAll();
    }
/*
    public List<QuantityColors> findAllColors(PrintMashine printMashine) {
        printMashineRepository.findById(Math.toIntExact(printMashine.getId())).get().getQuantityColors();
        return List.of(printMashine.getQuantityColors());
    }
*/
    public PrintMashine save(PrintMashine pmachine) {

        if (pmachine.getPriceOfCmykClick()!=0)
        pmachine.setFinalCostOfCmykClick(pmachine.getPriceOfCmykClick()
                +pmachine.getCost().floatValue()/pmachine.getClicks().floatValue());
        else pmachine.setFinalCostOfCmykClick(0f);

        if (pmachine.getPriceOfBlackClick()!=0)
        pmachine.setFinalCostOfBlackClick(pmachine.getPriceOfBlackClick()
                +pmachine.getCost().floatValue()/pmachine.getClicks().floatValue());
        else pmachine.setFinalCostOfBlackClick(0f);

        if (pmachine.getPriceOfSpotClick()!=0)
        pmachine.setFinalCostOfSpotClick(pmachine.getPriceOfSpotClick()
                +pmachine.getCost().floatValue()/pmachine.getClicks().floatValue());
        else pmachine.setFinalCostOfSpotClick(0f);

        return printMashineRepository.save(pmachine);
    }

    public void delete(PrintMashine pmachine) {
        printMashineRepository.delete(pmachine);
    }

}
