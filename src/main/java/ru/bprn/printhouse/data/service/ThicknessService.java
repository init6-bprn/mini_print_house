package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.Thickness;
import ru.bprn.printhouse.data.repository.ThicknessRepository;

import java.util.List;

@Service
public class ThicknessService {
    private ThicknessRepository thicknessRepository;

    public ThicknessService(ThicknessRepository thicknessRepository) {
        this.thicknessRepository = thicknessRepository;
    }

    public List<Thickness> findAll(){
        return  this.thicknessRepository.findAll();
    }

    public Thickness save(Thickness thickness) {
        return this.thicknessRepository.save(thickness);
    }

    public void delete(Thickness thickness) {
        this.thicknessRepository.delete(thickness);
    }
}
