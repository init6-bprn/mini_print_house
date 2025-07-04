package ru.bprn.printhouse.data.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.data.entity.Formulas;
import ru.bprn.printhouse.data.repository.FormulasRepository;

import java.util.List;
import java.util.Optional;

@Service
public class FormulasService {
    private final FormulasRepository formulasRepository;

    public FormulasService(FormulasRepository formulasRepository) {
        this.formulasRepository = formulasRepository;
    }

    public List<Formulas> findAll() {return this.formulasRepository.findAll();}

    public void delete(Formulas formula) {this.formulasRepository.delete(formula);}

    public Formulas save(Formulas formula) {return this.formulasRepository.save(formula);}

    public Formulas saveAndFlush(Formulas formula){return this.formulasRepository.saveAndFlush(formula);}

    public List<Formulas> populate(String str){
        if (str==null) return findAll();
        else return  this.formulasRepository.search(str);
    }

    public Optional<Formulas> duplicate(Formulas formula) {
        if (formula!= null) {
            var newFormula = new Formulas();
            newFormula.setName(formula.getName());
            newFormula.setDescription(formula.getDescription());
            newFormula.setFormula(formula.getFormula());
            newFormula.setTypeOfWorks(formula.getTypeOfWorks());
            return Optional.of(save(newFormula));
        }
        else return Optional.empty();
    }
}
