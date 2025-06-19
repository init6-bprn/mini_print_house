package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.bprn.printhouse.data.entity.Formulas;

import java.util.List;

public interface FormulasRepository extends JpaRepository<Formulas, Long> {
    @Query("select c from Formulas c where lower(c.name) like lower(concat('%', :searchTerm, '%'))")
    List<Formulas> search(@Param("searchTerm") String searchTerm);
}