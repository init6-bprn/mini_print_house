package ru.bprn.printhouse.views.templates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.views.templates.entity.Templates;

public interface TemplatesRepository extends JpaRepository<Templates, Long> {
}
