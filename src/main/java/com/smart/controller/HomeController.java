package com.smart.controller;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import javax.validation.Valid;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	//handler to open home page
	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title","Home - Smart Contact Manager");
		return "home";
	}
	
	//handler to open signup form
	@RequestMapping("/signup")
	public String signUp(Model model) {
		model.addAttribute("user",new User());
		model.addAttribute("title","SignUp Page");
		return "signup";
	}
	
	//handler for registering user
	@RequestMapping(value="/doRegister", method= RequestMethod.POST)
	public String registerUser(@Valid @ModelAttribute("user") User user,
			BindingResult result1,@RequestParam(value="agreement",defaultValue="false") 
			boolean agreement, Model model,HttpSession session) {
		
		try {
			if(!agreement) {
				System.out.println("you have not accept agreement");
				throw new Exception("you have not accept agreement");
			}
			if(result1.hasErrors()) {
				System.out.println("ERROR"+result1.toString());
				model.addAttribute("user",user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
			System.out.println("Agreement"+agreement);
			System.out.println("User"+user);
			User result=userRepository.save(user);	
			model.addAttribute("user",new User());
			session.setAttribute("message",new Message("Successfully registered!!","alert-success"));
			return "signup";
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message",new Message("Something went wrong!!"+ e.getMessage(),"alert-danger"));
			return "signup";
		}
	}
	
	//	handler for custom login
	@RequestMapping("/signin")
	public String signIn(Model model) {
		model.addAttribute("title","Login Page");
		return "login";
	}
}
