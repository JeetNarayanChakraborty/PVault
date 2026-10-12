package messageQueue;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import security.OTPService;

import java.util.ArrayList;


@RestController
@RequestMapping("/queue")
public class SendOTPEventProducer 
{
	@Autowired
	private KafkaTemplate<String, ArrayList<String>> kafkaTemplate;
	
	@Autowired
	OTPService otpService;
	
	private final Logger log = LoggerFactory.getLogger(SendOTPEventProducer.class);
	
	
	@PostMapping("/sendOTP")
	public void publishSendOTPEvent(HttpServletRequest request) 
	{
		String userOTP = otpService.generateOTP();
		request.getSession().setAttribute("OTP", userOTP);
		String toEmail = (String) request.getSession().getAttribute("username");

		ArrayList<String> emailList = new ArrayList<>();
		emailList.add(toEmail);
		emailList.add(userOTP);

		kafkaTemplate.send("send-otp-event", emailList);
	}
}
