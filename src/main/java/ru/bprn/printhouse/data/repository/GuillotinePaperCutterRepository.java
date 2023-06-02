package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.data.entity.GuillotinePaperCutter;

public interface GuillotinePaperCutterRepository extends JpaRepository<GuillotinePaperCutter, Integer> {
}