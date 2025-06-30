package ru.bprn.printhouse.views.additionalWorks.service;

import org.springframework.stereotype.Service;
import ru.bprn.printhouse.views.additionalWorks.entity.TypeOfWorks;
import ru.bprn.printhouse.views.additionalWorks.repository.TypeOfWorksRepository;

import java.util.List;

@Service
public class TypeOfWorksService {
    private final TypeOfWorksRepository repository;

    public TypeOfWorksService (TypeOfWorksRepository typeOfWorksRepository) {
        this.repository = typeOfWorksRepository;
    }

    public TypeOfWorks save(TypeOfWorks typeOfWorks) {return this.repository.save(typeOfWorks);}
    public List<TypeOfWorks> findAll() {return this.repository.findAll();}
    public void delete(TypeOfWorks typeOfWorks) {this.repository.delete(typeOfWorks);}

}
