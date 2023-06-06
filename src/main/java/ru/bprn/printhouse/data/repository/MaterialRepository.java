package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.data.entity.Material;

public interface MaterialRepository extends JpaRepository<Material, Long> {
}