package messageQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.PVault.entityClasses.User;


@Controller
@RequestMapping("/queue")
public class UserRegistrationEventProducer 
{
    @Autowired
    private KafkaTemplate<String, User> kafkaTemplate;
    

    @PostMapping("/userRegistration")
    public String publishUserRegistrationEvent(@ModelAttribute User registrationFormData) 
    {
        kafkaTemplate.send("user-registration-event", registrationFormData);
        return "mainPage";
    }
}