package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.data.entity.Thickness;

public interface ThicknessRepository extends JpaRepository<Thickness, Long> {
}