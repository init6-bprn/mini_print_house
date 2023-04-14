package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.data.entity.TypeOfPrinter;

public interface TypeOfPrinterRepository extends JpaRepository<TypeOfPrinter, Integer> {
}