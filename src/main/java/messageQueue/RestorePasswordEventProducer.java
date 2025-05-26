package messageQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.PVault.entityClasses.Password;
import com.fasterxml.jackson.databind.ObjectMapper;


@RestController
@RequestMapping("/queue")
public class RestorePasswordEventProducer 
{
	 @Autowired
	 private KafkaTemplate<String, Password> kafkaTemplate;
	    

	 @PostMapping("/restorePassword")
	 public void publishRestorePasswordEvent(@RequestParam("password") String passwordJson) 
	 {
		 try 
		 {			 
		     ObjectMapper mapper = new ObjectMapper();
		     Password password = mapper.readValue(passwordJson, Password.class);
		      
		     kafkaTemplate.send("restore-password-event", password);
		     //System.out.println("User password sent to Kafka: " + password);
		 } 
		 
		 catch(Exception e) 
		 {
			 e.printStackTrace();
		 }
	 }
}










