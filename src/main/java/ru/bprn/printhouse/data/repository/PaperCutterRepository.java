package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.data.entity.PaperCutter;

public interface PaperCutterRepository extends JpaRepository<PaperCutter, Integer> {
}