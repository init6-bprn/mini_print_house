package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.PrintMashine;
import ru.bprn.printhouse.data.entity.QuantityColors;
import ru.bprn.printhouse.data.entity.TypeOfPrinter;
import ru.bprn.printhouse.data.repository.PrintMashineRepository;
import ru.bprn.printhouse.data.repository.QuantityColorsRepository;
import ru.bprn.printhouse.data.repository.TypeOfPrinterRepository;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    @PostConstruct
    public void populateTestData() {
        if (typeOfPrinterRepository.count() == 0) {
            typeOfPrinterRepository.saveAll(
                    Stream.of("Струйный", "Лазерный", "Трафаретный")
                                    .map(TypeOfPrinter::new)
                                    .collect(Collectors.toList()));
        }

        if (quantityColorsRepository.count() == 0) {
            quantityColorsRepository.saveAll(
                    Stream.of("1", "4", "0")
                            .map(QuantityColors::new)
                            .collect(Collectors.toList()));
        }
        if (printMashineRepository.count()==0) {
            Random r = new Random(0);
            List<TypeOfPrinter> types = typeOfPrinterRepository.findAll();
            List<QuantityColors> colors = quantityColorsRepository.findAll();
            printMashineRepository.saveAll(
            Stream.of("HP LaserJet", "Konica Minolta", "Duplo")
                    .map(name ->{
                        PrintMashine mash = new PrintMashine(name);
                        mash.setTypeOfPrinter(types.get(r.nextInt(types.size())));
                        mash.setQuantityColors(colors.get(r.nextInt(colors.size())));
                        return  mash;
                    }).collect(Collectors.toList()));
        }

    }


}
