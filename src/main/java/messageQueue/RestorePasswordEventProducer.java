package messageQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.PVault.entityClasses.Password;


@RestController
@RequestMapping("/queue")
public class RestorePasswordEventProducer 
{
	 @Autowired
	 private KafkaTemplate<String, Password> kafkaTemplate;
	    

	 @PostMapping("/restorePassword")
	 public void publishRestorePasswordEvent(@RequestParam(name = "password") Password userPassword) 
	 {
	     kafkaTemplate.send("restore-password-event", userPassword);
	 }
}