package com.mini.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.mini.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
}
