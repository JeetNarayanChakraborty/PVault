package messageQueue;

import com.example.PVault.entityClasses.Password;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/queue")
public class RestorePasswordEventProducer 
{
	private static final Logger log = LoggerFactory.getLogger(RestorePasswordEventProducer.class);
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
		 } 
		 
		 catch(Exception e)
		 {
			 log.error(e.getMessage());
		 }
	 }
}










