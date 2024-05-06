package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.StandartSize;
import ru.bprn.printhouse.data.repository.StandartSizeRepository;

import java.util.List;

@Service
public class StandartSizeService {
    private StandartSizeRepository standartSizeDictionaryRepository;

    public StandartSizeService(StandartSizeRepository standartSizeDictionaryRepository){
        this.standartSizeDictionaryRepository = standartSizeDictionaryRepository;
    }

    public List<StandartSize> findAll(){
        return standartSizeDictionaryRepository.findAll();
    }

    public StandartSize save(StandartSize standartSize) {
        return standartSizeDictionaryRepository.save(standartSize);
    }

    public StandartSize saveAndFlush(StandartSize standartSize) {
        return standartSizeDictionaryRepository.saveAndFlush(standartSize);
    }

    public void delete(StandartSize standartSize) {
        standartSizeDictionaryRepository.delete(standartSize);
    }

}
