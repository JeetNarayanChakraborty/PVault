package com.example.PVault.entityClasses;

import java.util.ArrayList;



public class formDetails 
{
    private ArrayList<String> masterKeyList;
    private User registrationFormData;
    
    
    public formDetails() {}
    
    public formDetails(ArrayList<String> masterKeyList, User registrationFormData) 
    {
        this.masterKeyList = masterKeyList;
        this.registrationFormData = registrationFormData;
    }

    // Getters and setters
    public ArrayList<String> getMasterKeyList() 
    {
        return masterKeyList;
    }

    public void setMasterKeyList(ArrayList<String> masterKeyList) 
    {
        this.masterKeyList = masterKeyList;
    }

    public User getRegistrationFormData() 
    {
        return registrationFormData;
    }

    public void setRegistrationFormData(User registrationFormData) 
    {
        this.registrationFormData = registrationFormData;
    }
}




