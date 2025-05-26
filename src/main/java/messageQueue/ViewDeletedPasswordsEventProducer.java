package messageQueue;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import com.example.PVault.service.BackupAndRestoreService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.PVault.entityClasses.Password;
import jakarta.servlet.http.HttpServletRequest;


@Controller
@RequestMapping("/queue")
public class ViewDeletedPasswordsEventProducer 
{
	@Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
	
	@Autowired
	private BackupAndRestoreService backupAndRestoreService;
    

    @PostMapping("/viewDeletedPasswords")
    public String publishViewDeletedPasswordsEvent(Model model, HttpServletRequest request)
    {
    	 String userID = SecurityContextHolder.getContext().getAuthentication().getName();
    	 List<Password> userDeletedPasswords = new ArrayList<>();
    	 List<String> jsonPasswords = new ArrayList<>();

    	 try 
    	 {
    	     userDeletedPasswords = backupAndRestoreService.restoreUserSavedPasswords(userID);
    	     
    	     ObjectMapper objectMapper = new ObjectMapper();
    	     for(Password p : userDeletedPasswords) 
    	     {
    	         try 
    	         {
    	             jsonPasswords.add(objectMapper.writeValueAsString(p));
    	         } 
    	         catch(Exception e) 
    	         {
    	              jsonPasswords.add("{}");
    	         }
    	     }
    	 } 
    	 catch (Exception e) { e.printStackTrace(); }
    	 
    	 
    	 model.addAttribute("deletedPasswordList", userDeletedPasswords);
    	 model.addAttribute("jsonPasswords", jsonPasswords);

    	 kafkaTemplate.send("view-deleted-passwords-event", userID);
    	 return "restoredPasswords";
    }
}





