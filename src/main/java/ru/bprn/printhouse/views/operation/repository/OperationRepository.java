package ru.bprn.printhouse.views.operation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.bprn.printhouse.views.operation.entity.Operation;
import ru.bprn.printhouse.views.operation.entity.TypeOfOperation;

import java.util.List;

@Repository
public interface OperationRepository extends JpaRepository<Operation, Long> {
    List<Operation> findAllByTypeOfOperation(TypeOfOperation type);

    @Query("select c from Operation c where lower(c.name) like lower(concat('%', :searchTerm, '%'))")
    List<Operation> search(@Param("searchTerm") String searchTerm);
}
