package com.example.PVault.service;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.PVault.entityClasses.Password;


@Repository
public interface passwordRepository extends JpaRepository<Password, String>
{
    @Query(value = "SELECT * FROM passwords WHERE ID = :Id", nativeQuery = true)
    Password findPasswordDetailsById(@Param("Id") String Id);
    
    @Query(value = "SELECT * FROM passwords WHERE username = :username", nativeQuery = true)
	List<Password> findAllPasswordsByUsername(@Param("username") String username);
    
    @Query(value = "SELECT Id, domainname, password FROM passwords WHERE username = :username", nativeQuery = true)
    List<Object[]> findWebsiteAndPasswordByUsername(@Param("username") String username);
    
    @Query(value = "INSERT INTO password_backup VALUES (userID, backup)", nativeQuery = true)
    void addBackup(@Param("userID") String userID, @Param("backup") String backup);
    
    @Query(value = "INSERT INTO secureKeys VALUES (username, masterKey)", nativeQuery = true)
    void addMasterKey(@Param("username") String username, @Param("masterKey") String masterKey);
    
	void deleteById(String ID);
}
