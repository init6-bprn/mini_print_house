package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.AdditionalWorksBean;
import ru.bprn.printhouse.data.entity.TypeOfWorks;
import ru.bprn.printhouse.data.repository.AdditionalWorksBeanRepository;

import java.util.List;

@Service
public class AdditionalWorksBeanService {
    private final AdditionalWorksBeanRepository repository;

    private AdditionalWorksBeanService(AdditionalWorksBeanRepository repository) {
        this.repository = repository;
    }

    public List<AdditionalWorksBean> findAll() {return this.repository.findAll();}
    public List<AdditionalWorksBean> findAllByType(TypeOfWorks type) {return this.repository.findAllByTypeOfWorks(type);}
    public AdditionalWorksBean save(AdditionalWorksBean bean) {return this.repository.save(bean);}
    public void delete(AdditionalWorksBean bean) {this.repository.delete(bean);}
}
