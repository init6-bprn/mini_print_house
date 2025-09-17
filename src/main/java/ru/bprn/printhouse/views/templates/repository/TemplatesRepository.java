package ru.bprn.printhouse.views.templates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.bprn.printhouse.views.templates.entity.Templates;

import java.util.Collection;
import java.util.List;

public interface TemplatesRepository extends JpaRepository<Templates, Long> {

    @Query("select c from Templates c where lower(c.name) like lower(concat('%', :searchTerm, '%')) or lower(c.description) like lower(concat('%', :searchTerm, '%'))")
    Collection<Templates> search(@Param("searchTerm") String searchTerm);

    @Query("select t from Templates t " +
            "where lower(t.name) like lower(concat('%', :searchTerm, '%')) " +
            "or lower(t.description) like lower(concat('%', :searchTerm, '%'))")
    List<Templates> searchList(@Param("searchTerm") String searchTerm);

    
}
