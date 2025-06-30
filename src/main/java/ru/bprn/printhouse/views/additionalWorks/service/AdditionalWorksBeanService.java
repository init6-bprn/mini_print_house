package ru.bprn.printhouse.views.additionalWorks.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.additionalWorks.entity.AdditionalWorksBean;
import ru.bprn.printhouse.views.additionalWorks.entity.TypeOfWorks;
import ru.bprn.printhouse.views.additionalWorks.repository.AdditionalWorksBeanRepository;

import java.util.List;
import java.util.Optional;

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
    public List<AdditionalWorksBean> populate (String str){
        if (str == null) return findAll();
        else return this.repository.search(str);
    }
    public Optional<AdditionalWorksBean> duplicate(AdditionalWorksBean bean) {
        if (bean!= null){
            var work = new AdditionalWorksBean();
            work.setName(bean.getName());
            work.setHaveAction(bean.isHaveAction());
            work.setActionFormula(bean.getActionFormula());
            work.setTypeOfWorks(bean.getTypeOfWorks());
            work.setHaveMaterial(bean.isHaveMaterial());
            work.setMaterialFormula(bean.getMaterialFormula());
            work.setListOfMaterials(bean.getListOfMaterials());
            work.setDefaultMaterial(bean.getDefaultMaterial());
            return Optional.of(this.repository.save(work));
        }
        else return Optional.empty();
    }
}
