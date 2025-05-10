package com.example.PVault.service;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO password_backup (userID, backup, time_stamp) VALUES (:userID, :backup, CURRENT_TIMESTAMP)", nativeQuery = true)
    void addBackup(@Param("userID") String userID, @Param("backup") String backup);
    
    @Query(value = "SELECT backup FROM password_backup WHERE ID = :userID", nativeQuery = true)
    List<String> getBackup(@Param("userID") String userID);
    
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO securekeys (username, master_key) VALUES (:username, :masterKey)", nativeQuery = true)
    void addMasterKey(@Param("username") String username, @Param("masterKey") String masterKey);
    
    @Query(value = "SELECT master_key FROM securekeys WHERE username = :username", nativeQuery = true)
    String getMasterKeyByUsername(@Param("username") String username);
    
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO encryptionkey (user, masterkeyencryptionkey) VALUES (:username, :key)", nativeQuery = true)
    void addAESEncryptionKeyForMasterKey(@Param("username") String username, @Param("key") String key);    
    
    @Query(value = "SELECT masterkeyencryptionkey FROM encryptionkey WHERE user = :username", nativeQuery = true)
    String getAESEncryptionKeyForMasterKey(@Param("username") String username);
    
	void deleteById(String ID);
}





