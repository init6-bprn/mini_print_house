package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.data.entity.Formulas;

public interface FormulasRepository extends JpaRepository<Formulas, Long> {
}