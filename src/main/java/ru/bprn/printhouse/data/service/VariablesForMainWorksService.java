package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.VariablesForMainWorks;
import ru.bprn.printhouse.data.repository.VariablesForMainWorksRepository;

import java.util.List;

@Service
public class VariablesForMainWorksService {

    private VariablesForMainWorksRepository variables;

    public VariablesForMainWorksService(VariablesForMainWorksRepository variables){
        this.variables = variables;
    }

    public List<VariablesForMainWorks> findAll() {return variables.findAll();}

    public List<VariablesForMainWorks> findAllClazz(String clazz) {return variables.findAllByClazz(clazz);}

    public VariablesForMainWorks save(VariablesForMainWorks var) {return variables.save(var);}

    public void delete(VariablesForMainWorks var) {variables.delete(var);}
}
