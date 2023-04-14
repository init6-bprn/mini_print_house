package ru.bprn.printhouse.data.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bprn.printhouse.data.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);
}