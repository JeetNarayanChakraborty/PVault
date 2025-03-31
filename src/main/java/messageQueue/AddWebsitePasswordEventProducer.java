package messageQueue;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.example.PVault.entityClasses.Password;


@RestController
@RequestMapping("/queue")
public class AddWebsitePasswordEventProducer 
{
	private KafkaTemplate<String, Password> kafkaTemplate;
	
	
	@PostMapping("/addWebsitePassword")
	public void publishWebsitePasswordEvent(@ModelAttribute Password userPassword) 
	{
		kafkaTemplate.send("add-website-password-event", userPassword);
	}
}
