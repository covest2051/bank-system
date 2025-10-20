package aop.repository;

import entity.Role;
import entity.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository  extends JpaRepository<Role,Long> {
    Optional<Role> findByName(RoleEnum roleEnum);
}
