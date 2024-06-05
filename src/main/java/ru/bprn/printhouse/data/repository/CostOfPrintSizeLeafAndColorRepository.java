package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bprn.printhouse.data.entity.CostOfPrintSizeLeafAndColor;
import ru.bprn.printhouse.data.entity.PrintMashine;

import java.util.List;

@Repository
public interface CostOfPrintSizeLeafAndColorRepository extends JpaRepository<CostOfPrintSizeLeafAndColor, Long> {
    List<CostOfPrintSizeLeafAndColor> findAllByPrintMashine (PrintMashine printMashine);
}