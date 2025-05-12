package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.CostOfPrintSizeLeafAndColor;
import ru.bprn.printhouse.data.entity.PrintMashine;
import ru.bprn.printhouse.data.entity.QuantityColors;
import ru.bprn.printhouse.data.entity.SizeOfPrintLeaf;
import ru.bprn.printhouse.data.repository.CostOfPrintSizeLeafAndColorRepository;

import java.util.List;

@Service
public class CostOfPrintSizeLeafAndColorService {

    private final CostOfPrintSizeLeafAndColorRepository costOfPrintSizeLeafAndColorRepository;

    public CostOfPrintSizeLeafAndColorService (CostOfPrintSizeLeafAndColorRepository costOfPrintSizeLeafAndColorRepository) {
        this.costOfPrintSizeLeafAndColorRepository = costOfPrintSizeLeafAndColorRepository;
    }

    public List<CostOfPrintSizeLeafAndColor> findAll() {return this.costOfPrintSizeLeafAndColorRepository.findAll();}

    public CostOfPrintSizeLeafAndColor save(CostOfPrintSizeLeafAndColor costOfPrint) {return this.costOfPrintSizeLeafAndColorRepository.save(costOfPrint);}

    public void delete(CostOfPrintSizeLeafAndColor costOfPrint) {this.costOfPrintSizeLeafAndColorRepository.delete(costOfPrint);}

    public List<CostOfPrintSizeLeafAndColor> findAllByPrintMashine(PrintMashine printMashine) {
        return costOfPrintSizeLeafAndColorRepository.findAllByPrintMashine(printMashine);
    }

    public List<CostOfPrintSizeLeafAndColor> findAllByPrintMashineAndQuantityColors
            (PrintMashine printMashine, QuantityColors quantityColors) {
        return costOfPrintSizeLeafAndColorRepository.findAllByPrintMashineAndQuantityColors
                (printMashine, quantityColors);
    }

    public CostOfPrintSizeLeafAndColor findByPrintMashineAndQuantityColorsSizeOfPrintLeaf
            (PrintMashine printMashine, QuantityColors quantityColors, SizeOfPrintLeaf sizeOfPrintLeaf) {
        return costOfPrintSizeLeafAndColorRepository.findByPrintMashineAndQuantityColorsAndSizeOfPrintLeaf
                (printMashine, quantityColors, sizeOfPrintLeaf);
    }

}
