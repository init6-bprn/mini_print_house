package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.GuillotinePaperCutter;
import ru.bprn.printhouse.data.repository.GuillotinePaperCutterRepository;

import java.util.List;

@Service
public class GuillotinePaperCutterService {

    private GuillotinePaperCutterRepository guillotinePaperCutterRepository;

    public GuillotinePaperCutterService (GuillotinePaperCutterRepository guillotinePaperCutterRepository){
        this.guillotinePaperCutterRepository = guillotinePaperCutterRepository;
    };

    public List<GuillotinePaperCutter> findAll() {
        return guillotinePaperCutterRepository.findAll();
    }

    public GuillotinePaperCutter save(GuillotinePaperCutter guillotinePaperCutter) {
        return this.guillotinePaperCutterRepository.save(guillotinePaperCutter);
    }

    public void delete(GuillotinePaperCutter guillotinePaperCutter) {
        this.guillotinePaperCutterRepository.delete(guillotinePaperCutter);
    }
}
