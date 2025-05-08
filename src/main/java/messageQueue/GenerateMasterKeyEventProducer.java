package messageQueue;

import java.util.ArrayList;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class GenerateMasterKeyEventProducer 
{
	private KafkaTemplate<String, ArrayList<String>> kafkaTemplate;
	
    public GenerateMasterKeyEventProducer(KafkaTemplate<String, ArrayList<String>> kafkaTemplate) 
    {
        this.kafkaTemplate = kafkaTemplate;
    }
	
	public ResponseEntity<String> publishGenerateMasterKeyEvent(ArrayList<String> masterKeyList) 
	{
		kafkaTemplate.send("generate-master-key-event", masterKeyList);
	    return ResponseEntity.ok("Generate Master Key event published successfully for user: " + masterKeyList.get(0));
	}
}
