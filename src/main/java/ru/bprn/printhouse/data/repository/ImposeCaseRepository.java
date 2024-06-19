package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bprn.printhouse.data.entity.ImposeCase;

@Repository
public interface ImposeCaseRepository extends JpaRepository<ImposeCase, Long> {



}
