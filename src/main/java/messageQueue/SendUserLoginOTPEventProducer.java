package messageQueue;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import security.OTPService;

import java.util.ArrayList;


@Controller
@RequestMapping("/queue")
public class SendUserLoginOTPEventProducer 
{
	@Autowired
	private KafkaTemplate<String, ArrayList<String>> kafkaTemplate;
	
	@Autowired
	private OTPService otpService;
	
	private final Logger log = LoggerFactory.getLogger(SendUserLoginOTPEventProducer.class);


	@GetMapping("/sendUserLoginOTP")
	public String publishSendUserLoginOTPEvent(HttpServletRequest request, @RequestParam("hiddenField") String username)
	{
		try
		{
			String userOTP = otpService.generateOTP();
			request.getSession().setAttribute("OTP", userOTP);

            ArrayList<String> mailDetails = new ArrayList<>();
			mailDetails.add(userOTP);
			mailDetails.add(username);

			kafkaTemplate.send("send-user-login-otp-event", mailDetails);

			log.info("Successfully generated OTP and published event for user: {}", username);
		}

		catch(Exception e)
		{
			log.error("Fatal error processing OTP request for user: {}. Error: {}", username, e.getMessage());
		}

		return "homePage";
	}
}
