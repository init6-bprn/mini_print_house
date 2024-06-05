package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bprn.printhouse.data.entity.Thickness;
@Repository
public interface ThicknessRepository extends JpaRepository<Thickness, Long> {
}