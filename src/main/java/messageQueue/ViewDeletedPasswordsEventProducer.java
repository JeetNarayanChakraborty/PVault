package messageQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.PVault.entityClasses.User;


@RestController
@RequestMapping("/queue")
public class ViewDeletedPasswordsEventProducer 
{
	@Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    

    @PostMapping("/viewDeletedPasswords")
    public void publishViewDeletedPasswordsEvent() 
    {
        kafkaTemplate.send("view-deleted-passwords-event", "ViewDeletedPasswordsEvent");
    }
}
