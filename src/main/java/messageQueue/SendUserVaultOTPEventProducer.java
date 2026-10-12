package messageQueue;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import security.OTPService;

import java.util.ArrayList;


@Controller
@RequestMapping("/queue")
public class SendUserVaultOTPEventProducer 
{
	@Autowired
	private KafkaTemplate<String, ArrayList<String>> kafkaTemplate;
	
	@Autowired
	OTPService otpService;
	
	private final Logger log = LoggerFactory.getLogger(SendUserVaultOTPEventProducer.class);


	@GetMapping("/sendUserVaultOTP")
	public String publishRestorePasswordEvent(HttpServletRequest request,
											  RedirectAttributes redirectAttrs)
	{
		String username = (String) request.getSession().getAttribute("username");

		try
		{
			String userOTP = otpService.generateOTP();
			request.getSession().setAttribute("VaultOTP", userOTP);

			ArrayList<String> mailList = new ArrayList<>();
			mailList.add(userOTP);
			mailList.add(username);

			kafkaTemplate.send("send-user-vault-otp-event", mailList);

			log.info("Successfully generated Vault OTP and published event for user: {}", username);

			return "vaultOTP";
		}

		catch(Exception e)
		{
			log.error("Fatal error processing Vault OTP request for user: {}. Error: {}", username, e.getMessage());

			redirectAttrs.addFlashAttribute("error", "Could not send OTP, please try again.");
			return "redirect:/vaultOTP";
		}
	}
}




