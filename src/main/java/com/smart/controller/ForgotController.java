package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.Changepass;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

@Controller
public class ForgotController {
	
	@Autowired
	EmailService emailService;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	BCryptPasswordEncoder bcrypt;
	
	Random random=new Random(1000);

	// handler for open emil form
	@RequestMapping("/forgot")
	public String openEmailForm(HttpSession session) {
		
		return "forgot_email_form";
	}
	
	//send otp to email handler
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam String email,HttpSession session) {
		
		System.out.println("email "+email);
		
		// generating otp of 6 digits
		
		int otp=random.nextInt(999999);
		System.out.println("OTP "+otp);
		// add otp and email to session...
		session.setAttribute("email", email);
		session.setAttribute("myotp",otp);
		
		// write code for send otp to email...
		
		String subject="OTP from SCM";
		String message="OTP = "+otp;
		String to=email;
		
		boolean flag=emailService.sendEmail(subject,message,to);
		if(flag) {
			session.setAttribute("message",new Message("We have sent OTP to your email...","alert-success"));
			return "verify_otp";
		}
		else {
			session.setAttribute("message",new Message("Something went wrong...","alert-danger"));
			return "forgot_email_form";
		}
	}
	
	@PostMapping("/verify-otp")
	public String verifyOTP(int otp,HttpSession session,Model model) {
		
		model.addAttribute("changepass",new Changepass());
		int myotp=(int)session.getAttribute("myotp");
		String email=(String)session.getAttribute("email");
		if(myotp==otp) {
			//password change form...
			User user=this.userRepository.getUserByUserName(email);
			if(user==null) {
				session.setAttribute("message",new Message("Email not exists with user!!!...","alert-danger"));
				return "forgot_email_form";
			}
			return "password_change_form";
		}
		else {
			session.setAttribute("message",new Message("Invalid OTP!! Enter valid OTP...","alert-danger"));
			return "verify_otp";
		}
	
	}
	
	@PostMapping("/change-password")
	public String changePass(@Valid Changepass changepass,BindingResult result1, HttpSession session) {
		
		if(result1.hasErrors()) {
			return "password_change_form";
		}
		
		String email=(String)session.getAttribute("email");
		System.out.println(email);
		User user=userRepository.getUserByUserName(email);
		System.out.println(user);
		user.setPassword(this.bcrypt.encode(changepass.getNewpassword()));
		userRepository.save(user);
		return "redirect:/signin?change=password changed successfully..";
	}
	
	
	
	
}
