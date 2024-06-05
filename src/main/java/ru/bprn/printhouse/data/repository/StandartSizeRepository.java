package ru.bprn.printhouse.data.repository;

import org.springframework.stereotype.Repository;
import ru.bprn.printhouse.data.entity.StandartSize;
@Repository
public interface StandartSizeRepository extends org.springframework.data.jpa.repository.JpaRepository<StandartSize, Long> {
}