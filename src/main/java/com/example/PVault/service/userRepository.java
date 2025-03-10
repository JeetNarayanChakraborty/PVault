package com.example.PVault.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.PVault.entityClasses.User;



@Repository
public interface userRepository extends JpaRepository<User, Long> 
{
    User findByUsername(String username);
    User findById(String userID);
    void deleteByUsername(String username);
}
