package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import ru.bprn.printhouse.data.entity.TypeOfMaterial;

public interface TypeOfMaterialRepository extends JpaRepository<TypeOfMaterial, Long> {
}