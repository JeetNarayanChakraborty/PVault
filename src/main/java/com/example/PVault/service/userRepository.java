package com.example.PVault.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.example.PVault.entityClasses.User;



@Repository
public interface userRepository extends JpaRepository<User, Long> 
{
    User findByUsername(String username);
    User findById(String userID);
    void deleteByUsername(String username);
    
    @Modifying
    @Transactional
    @Query(value = "UPDATE password_history SET created_Time = NOW() WHERE user_id = :userID", nativeQuery = true)
    void updatePasswordHistory(String userID);
}
