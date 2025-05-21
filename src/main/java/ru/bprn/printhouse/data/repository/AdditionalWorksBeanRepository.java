package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bprn.printhouse.data.entity.AdditionalWorksBean;
import ru.bprn.printhouse.data.entity.TypeOfWorks;

import java.util.List;

@Repository
public interface AdditionalWorksBeanRepository extends JpaRepository<AdditionalWorksBean, Long> {
    List<AdditionalWorksBean> findAllByTypeOfWorks(TypeOfWorks type);
}
