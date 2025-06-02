package ru.bprn.printhouse.views.templates.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.templates.entity.Chains;
import ru.bprn.printhouse.views.templates.entity.Templates;
import ru.bprn.printhouse.views.templates.repository.ChainsRepository;
import ru.bprn.printhouse.views.templates.repository.TemplatesRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class TemplatesService {

    private final TemplatesRepository repository;
    private final ChainsRepository chainsRepository;

    public TemplatesService (TemplatesRepository repository, ChainsRepository chainsRepository) {
        this.repository = repository;
        this.chainsRepository = chainsRepository;
    }

    public List<Templates> findAll() {return this.repository.findAll();}

    public void delete(Templates templates) {this.repository.delete(templates);}

    public Templates save(Templates templates) {return  this.repository.save(templates);}

    public Optional<Templates> findById(Long id) {return this.repository.findById(id);}

}
