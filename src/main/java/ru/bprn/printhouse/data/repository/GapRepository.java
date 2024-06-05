package ru.bprn.printhouse.data.repository;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bprn.printhouse.data.entity.Gap;

import java.util.List;
@Repository
public interface GapRepository extends JpaRepository<Gap, Long> {

    @Override
    <S extends Gap> List<S> findAll(Example<S> example);

    List<Gap> findAllByNameContainingIgnoreCase(String name);
}