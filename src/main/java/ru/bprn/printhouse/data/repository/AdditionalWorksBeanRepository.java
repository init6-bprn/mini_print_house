package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.bprn.printhouse.data.entity.AdditionalWorksBean;
import ru.bprn.printhouse.data.entity.TypeOfWorks;

import java.util.List;

@Repository
public interface AdditionalWorksBeanRepository extends JpaRepository<AdditionalWorksBean, Long> {
    List<AdditionalWorksBean> findAllByTypeOfWorks(TypeOfWorks type);

    @Query("select c from AdditionalWorksBean c where lower(c.name) like lower(concat('%', :searchTerm, '%'))")
    List<AdditionalWorksBean> search(@Param("searchTerm") String searchTerm);
}
