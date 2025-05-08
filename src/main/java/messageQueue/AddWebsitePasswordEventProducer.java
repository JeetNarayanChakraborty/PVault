package messageQueue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.PVault.entityClasses.Password;
import jakarta.servlet.http.HttpServletRequest;


@Controller
@RequestMapping("/queue")
public class AddWebsitePasswordEventProducer 
{
	@Autowired
	private KafkaTemplate<String, Password> kafkaTemplate;
	
	
	@PostMapping("/addWebsitePassword")
	public String publishWebsitePasswordEvent(@ModelAttribute Password userPassword, HttpServletRequest request) 
	{
		String username = (String) request.getSession().getAttribute("username");
		
		if(username == null) 
		{
			username = SecurityContextHolder.getContext().getAuthentication().getName();
		}
		
		userPassword.setUsername(username);
		kafkaTemplate.send("add-website-password-event", userPassword);
		return "mainPage";
	}
}
