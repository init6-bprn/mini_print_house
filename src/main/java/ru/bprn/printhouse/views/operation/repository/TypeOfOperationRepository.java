package ru.bprn.printhouse.views.operation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bprn.printhouse.views.operation.entity.TypeOfOperation;

@Repository
public interface TypeOfOperationRepository extends JpaRepository<TypeOfOperation, Long> {


}
