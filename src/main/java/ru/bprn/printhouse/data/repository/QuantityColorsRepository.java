package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.data.entity.QuantityColors;

public interface QuantityColorsRepository extends JpaRepository<QuantityColors, Integer> {

}