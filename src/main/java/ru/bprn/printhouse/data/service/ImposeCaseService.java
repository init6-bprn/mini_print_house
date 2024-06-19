package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.ImposeCase;
import ru.bprn.printhouse.data.repository.ImposeCaseRepository;

import java.util.List;

@Service
public class ImposeCaseService {
    private ImposeCaseRepository imposeCaseRepository;

    public ImposeCaseService (ImposeCaseRepository imposeCaseRepository){
        this.imposeCaseRepository = imposeCaseRepository;
    }

    public List<ImposeCase> findAll() {return imposeCaseRepository.findAll();}

    public ImposeCase save(ImposeCase imposeCase) {return imposeCaseRepository.save(imposeCase);}

    public void delete(ImposeCase imposeCase) {imposeCaseRepository.delete(imposeCase);}
}
