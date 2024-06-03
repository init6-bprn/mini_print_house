package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.Gap;
import ru.bprn.printhouse.data.repository.GapRepository;

import java.util.List;

@Service
public class GapService {
    private GapRepository gapRepository;

    public GapService (GapRepository gapRepository){
        this.gapRepository = gapRepository;
    }

    public List<Gap> findAllBleeds (String str) {
        return gapRepository.findAllByNameContainingIgnoreCase(str);
    }

    public List<Gap> findAll(){
        return gapRepository.findAll();
    }

    public Gap save(Gap gap) {
        return this.gapRepository.save(gap);
    }

    public void delete(Gap gap) {
        this.gapRepository.delete(gap);
    }
}
