package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.PrintMashine;
import ru.bprn.printhouse.data.repository.PrintMashineRepository;
import ru.bprn.printhouse.data.repository.QuantityColorsRepository;
import ru.bprn.printhouse.data.repository.TypeOfPrinterRepository;

import java.util.List;

@Service
public class PrintMashineService {
    private PrintMashineRepository printMashineRepository;
    private QuantityColorsRepository quantityColorsRepository;
    private TypeOfPrinterRepository typeOfPrinterRepository;

    public PrintMashineService (PrintMashineRepository printMashineRepository, QuantityColorsRepository quantityColorsRepository, TypeOfPrinterRepository typeOfPrinterRepository) {
        this.printMashineRepository = printMashineRepository;
        this.quantityColorsRepository = quantityColorsRepository;
        this.typeOfPrinterRepository = typeOfPrinterRepository;
    }

    public List<PrintMashine> findAll(){
        return  printMashineRepository.findAll();
    }

    public PrintMashine save (PrintMashine pmachine) {

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
