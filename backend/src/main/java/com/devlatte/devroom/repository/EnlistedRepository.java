package com.devlatte.devroom.repository;

import com.devlatte.devroom.entity.Enlisted;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnlistedRepository extends JpaRepository<Enlisted, Long> {
}
