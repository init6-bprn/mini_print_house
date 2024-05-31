package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.data.entity.Gap;

public interface GapRepository extends JpaRepository<Gap, Long> {
}