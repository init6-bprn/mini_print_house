package ru.bprn.printhouse.views.templates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.bprn.printhouse.views.templates.entity.Chains;

import java.util.List;

public interface ChainsRepository extends JpaRepository<Chains, Long> {

    @Query("select c from Chains c where lower(c.name) like lower(concat('%', :searchTerm, '%')) ")

    List<Chains> search(@Param("searchTerm") String searchTerm);

}
