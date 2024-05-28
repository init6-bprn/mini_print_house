package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.data.entity.CostOfPrintSizeLeafAndColor;

public interface CostOfPrintSizeLeafAndColorRepository extends JpaRepository<CostOfPrintSizeLeafAndColor, Long> {
}