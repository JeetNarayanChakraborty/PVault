package messageQueue;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import security.OTPService;

import java.util.ArrayList;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;


@RestController
@RequestMapping("/queue")
public class SendUserLoginOTPEventProducer 
{
	@Autowired
	private KafkaTemplate<String, ArrayList<String>> kafkaTemplate;
	
	@Autowired
	private OTPService otpService;
	
	private final Logger log = LoggerFactory.getLogger(SendUserLoginOTPEventProducer.class);
	
	
	@PostMapping("/sendUserLoginOTP")
	public void publishSendUserLoginOTPEvent(HttpServletRequest request, @RequestParam("hiddenField") String username) 
	{
		String executionID = UUID.randomUUID().toString();
	    int maxRetries = 3;
	    int retryCount = 0;
	    int[] backoffTimes = {100, 200, 400}; // Minimal Backoff times in milliseconds to increase user experience
	    boolean success = false;
	    Exception lastException = null;

	    while(retryCount <= maxRetries && !success) 
	    {
	        try 
	        {
	            if(retryCount > 0) 
	            {
	                try 
	                {
	                    Thread.sleep(backoffTimes[retryCount - 1]);
	                    
	                } 
	                
	                catch(InterruptedException ie) 
	                {
	                    Thread.currentThread().interrupt();
	                }
	            }
	            
	            String userOTP = otpService.generateOTP();
	    		request.getSession().setAttribute("OTP", userOTP);
	    		String toEmail = username;
	    		
	    		ArrayList<String> mailDetails = new ArrayList<>();
	    		
	    		mailDetails.add(userOTP);
	    		mailDetails.add(toEmail);
	    		
	    		kafkaTemplate.send("send-user-login-otp-event", mailDetails);
	    		
	            success = true;	            
	        } 
	        
	        catch(Exception e) 
	        {
	            retryCount++;
	            lastException = e;
	            
	            if(retryCount > maxRetries) 
	            {
	                break;
	            }
	        }
	    }
	    
	    if(success) 
	    {
	        log.info("Successfully after {} attempts, execution: {}", 
	                retryCount, executionID);
	    } 
	    
	    else 
	    {
	        log.error("Failed after {} attempts, execution: {}, error: {}", 
	                 retryCount, executionID, lastException.getMessage());
	    }
	}
}
