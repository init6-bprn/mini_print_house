package ru.bprn.printhouse.views.material.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.bprn.printhouse.views.material.entity.AbstractMaterials;

import java.util.List;
import java.util.UUID;

@Repository
public interface AbstractMaterialRepository extends JpaRepository<AbstractMaterials, UUID> {
    @Query("select c from AbstractMaterials c where lower(c.searchStr) like lower(concat('%', :searchTerm, '%'))")
    List<AbstractMaterials> search(@Param("searchTerm") String searchTerm);

}
