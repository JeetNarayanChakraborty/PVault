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
public class UserRegistrationEventProducer 
{
    @Autowired
    private KafkaTemplate<String, User> kafkaTemplate;
    

    @PostMapping("/userRegistration")
    public String publishUserRegistrationEvent(@RequestBody User registrationFormData) 
    {
        kafkaTemplate.send("user-registration-event", registrationFormData);
        return "mainPage";
    }
}