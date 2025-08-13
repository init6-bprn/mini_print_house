package ru.bprn.printhouse.views.templates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bprn.printhouse.views.templates.entity.TemplatesMenuItem;

import java.util.List;

@Repository
public interface TemplatesMenuItemRepository extends JpaRepository<TemplatesMenuItem, Long> {
    List<TemplatesMenuItem> findByContextAndActiveTrueOrderByOrderIndex(String str);
    boolean existsByClassName(String className);
}