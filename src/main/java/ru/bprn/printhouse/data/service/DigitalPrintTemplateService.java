package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.DigitalPrintTemplate;
import ru.bprn.printhouse.data.repository.DigitalPrintTemplateRepository;

import java.util.List;

@Service
public class DigitalPrintTemplateService {

    private DigitalPrintTemplateRepository digitalPrintTemplateRepository;

    public DigitalPrintTemplateService(DigitalPrintTemplateRepository digitalPrintTemplateRepository) {this.digitalPrintTemplateRepository = digitalPrintTemplateRepository;}

    public List<DigitalPrintTemplate> findAll() {return this.digitalPrintTemplateRepository.findAll();}

    public  DigitalPrintTemplate save(DigitalPrintTemplate digitalPrintTemplate) {return this.digitalPrintTemplateRepository.save(digitalPrintTemplate);}

    public void delete(DigitalPrintTemplate digitalPrintTemplate) {this.digitalPrintTemplateRepository.delete(digitalPrintTemplate);}

}
