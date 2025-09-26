package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.bprn.printhouse.data.entity.TypeOfMaterial;

import java.util.Optional;
@Repository
public interface TypeOfMaterialRepository extends JpaRepository<TypeOfMaterial, Long> {

    Optional<TypeOfMaterial> findByName(String name);
}