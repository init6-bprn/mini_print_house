package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.data.entity.PrintMashine;

public interface PrintMashineRepository extends JpaRepository<PrintMashine, Integer> {
}