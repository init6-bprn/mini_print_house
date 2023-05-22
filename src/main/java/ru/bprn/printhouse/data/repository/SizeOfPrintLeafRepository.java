package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.data.entity.SizeOfPrintLeaf;

public interface SizeOfPrintLeafRepository extends JpaRepository<SizeOfPrintLeaf, Long> {
}