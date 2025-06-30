package ru.bprn.printhouse.views.additionalWorks.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bprn.printhouse.views.additionalWorks.entity.TypeOfWorks;

@Repository
public interface TypeOfWorksRepository extends JpaRepository<TypeOfWorks, Long> {


}
