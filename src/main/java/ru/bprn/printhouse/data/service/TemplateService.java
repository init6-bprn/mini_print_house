package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.Template;
import ru.bprn.printhouse.data.repository.TemplateRepository;

import java.util.List;

@Service
public class TemplateService {

    private TemplateRepository templateRepository;

    public TemplateService(TemplateRepository templateRepository){
        this.templateRepository = templateRepository;
    }

    public List<Template> findAll() {return this.templateRepository.findAll();}

    private Template save(Template template) {return this.templateRepository.save(template);}

    public void delete (Template template) {this.templateRepository.delete(template);}
}
