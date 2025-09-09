package ru.bprn.printhouse.views.templates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.views.templates.entity.TemplatesMenuItem;

import java.util.List;

public interface TemplatesMenuItemRepository extends JpaRepository<TemplatesMenuItem, Long> {
    boolean existsByClassName(String className);
    List<TemplatesMenuItem> findByContextAndActiveTrueOrderByOrderIndex(String context);
    boolean existsByNameAndContext(String name, String context);
}
