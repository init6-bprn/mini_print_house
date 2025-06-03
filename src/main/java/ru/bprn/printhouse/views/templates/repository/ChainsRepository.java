package ru.bprn.printhouse.views.templates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.views.templates.entity.Chains;

public interface ChainsRepository extends JpaRepository<Chains, Long> {

    //List<Chains> findAllByTemplates(Templates templates);

}
