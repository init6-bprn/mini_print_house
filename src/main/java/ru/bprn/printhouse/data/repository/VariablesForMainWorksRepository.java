package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bprn.printhouse.data.entity.VariablesForMainWorks;

import java.util.List;

@Repository
public interface VariablesForMainWorksRepository extends JpaRepository<VariablesForMainWorks,Long> {

    List<VariablesForMainWorks> findAllByClazzContainingIgnoreCase(String clazz);
    List<VariablesForMainWorks> findAllByClazz(String clazz);
}