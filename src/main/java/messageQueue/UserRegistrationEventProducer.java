package messageQueue;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.PVault.entityClasses.User;
import com.example.PVault.entityClasses.formDetails;

import jakarta.servlet.http.HttpServletRequest;


@Controller
@RequestMapping("/queue")
public class UserRegistrationEventProducer 
{
    @Autowired
    private KafkaTemplate<String, formDetails> kafkaTemplate;
	
	
    @PostMapping("/userRegistration")
    public String publishUserRegistrationEvent(@ModelAttribute User registrationFormData, 
    		                                   HttpServletRequest request) 
    {
    	String username = registrationFormData.getUsername();
    	request.getSession().setAttribute("username", username);
    	request.getSession().setAttribute("UserID", registrationFormData.getId());
		String masterKey = UUID.randomUUID().toString().substring(0, 16); // Generate Master key
		
		KeyGenerator keyGen=null;
		
		try 
		{
			keyGen = KeyGenerator.getInstance("AES");
		} catch(NoSuchAlgorithmException e) { e.printStackTrace();}
		
        keyGen.init(256);
        SecretKey secretKey = keyGen.generateKey();          //Create AES key to encrypt master key
        String AESEncyptionKeyForMasterKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
		
        request.getSession().setAttribute("AESEncyptionKeyForMasterKey", AESEncyptionKeyForMasterKey); //Save the AES key for master key in session
    	
    	ArrayList<String> masterKeyList = new ArrayList<>(
			              Arrays.asList(username, masterKey, AESEncyptionKeyForMasterKey));
    	
    	
    	formDetails formDetails = new formDetails(masterKeyList, registrationFormData);
    	
        kafkaTemplate.send("user-registration-event", formDetails);
        return "mainPage";
    }
}





