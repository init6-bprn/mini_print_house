package ru.bprn.printhouse.views.templates.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.templates.entity.Chains;
import ru.bprn.printhouse.views.templates.repository.ChainsRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ChainsService {
    private final ChainsRepository chainsRepository;

    public ChainsService(ChainsRepository chainsRepository) {
        this.chainsRepository = chainsRepository;
    }

    public List<Chains> findAll() {return chainsRepository.findAll();}

    public Chains save(Chains chains) {return this.chainsRepository.save(chains);}

    public void delete(Chains chains) {this.chainsRepository.delete(chains);}

    public Optional<Chains> findById(Long id) {return this.chainsRepository.findById(id);}

    //public List<Chains> findAllByTemplates(Templates templates) {return this.findAllByTemplates(templates);}
}
