package ru.bprn.printhouse.views.templates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.bprn.printhouse.views.templates.entity.AbstractTemplate;
import ru.bprn.printhouse.views.templates.entity.Chains;
import ru.bprn.printhouse.views.templates.entity.Templates;

import java.util.Collection;
import java.util.List;

public interface TemplatesRepository extends JpaRepository<Templates, Long> {

    @Query("select c from Templates c where lower(c.name) like lower(concat('%', :searchTerm, '%')) or lower(c.description) like lower(concat('%', :searchTerm, '%'))")
    Collection<AbstractTemplate> search(@Param("searchTerm") String searchTerm);
}
