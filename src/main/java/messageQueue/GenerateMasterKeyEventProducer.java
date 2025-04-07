package messageQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/queue")
public class GenerateMasterKeyEventProducer 
{
	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;
	
	@RequestMapping("/generateMasterKey")
	public void publishGenerateMasterKeyEvent(@RequestParam("hiddenField") String username) 
	{
	     kafkaTemplate.send("generate-master-key-event", username);
	}
}
