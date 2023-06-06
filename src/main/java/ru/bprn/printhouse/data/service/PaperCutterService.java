package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.PaperCutter;
import ru.bprn.printhouse.data.repository.PaperCutterRepository;

import java.util.List;

@Service
public class PaperCutterService {

    private PaperCutterRepository paperCutterRepository;

    public PaperCutterService(PaperCutterRepository paperCutterRepository){
        this.paperCutterRepository = paperCutterRepository;
    };

    public List<PaperCutter> findAll() {
        return paperCutterRepository.findAll();
    }

    public PaperCutter save(PaperCutter paperCutter) {
        var i = 0f;
        if (paperCutter.getCuts()!=0){
            i = paperCutter.getCostOfCutter().floatValue()/paperCutter.getCuts();
        }

        i = i + paperCutter.getCostOfKnifeSharpening().floatValue()/paperCutter.getCutsToSharpening();
        paperCutter.setFinalCostOfCut(i);
        return this.paperCutterRepository.save(paperCutter);
    }

    public void delete(PaperCutter paperCutter) {
        this.paperCutterRepository.delete(paperCutter);
    }
}
