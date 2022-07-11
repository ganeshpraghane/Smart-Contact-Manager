package com.smart.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.smart.dao.ContactRepository;
import com.smart.dao.MyOrderRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Changepass;
import com.smart.entities.Contact;
import com.smart.entities.MyOrder;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	MyOrderRepository myOrderRepository;

	// method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String username = principal.getName();
		System.out.println("USERNAME " + username);
		User user = userRepository.getUserByUserName(username);
		System.out.println("User " + user);
		model.addAttribute("user", user);
	}

	// dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User DashBoard");
		return "normal/user_dashboard";
	}

	// open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}

	// handler for adding contact
	@PostMapping("/process-contact")
	public String processContact(@Valid @ModelAttribute Contact contact, BindingResult result,
			@RequestParam("profileImage") MultipartFile file, Model model, Principal principal, HttpSession session) {

		if (result.hasErrors()) {
//			model.addAttribute("contact",contact);
			return "normal/add_contact_form";
		}
		try {
			String name = principal.getName();
			User user = userRepository.getUserByUserName(name);

			session.setAttribute("message",
					new Message("Your Contact Successfully Added!! Add More...", "alert-success"));

//			processing and uploading file
			if (file.isEmpty()) {
//				if the file is empty then try our message
				System.out.println("file is empty");
				contact.setImage("default.png");
			} else {
//				upload the file to folder and update the name to contact
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is Uploaded");
			}
			contact.setUser(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			model.addAttribute("contact", new Contact());
			System.out.println("DATA " + contact);
			System.out.println("Added to database...");
		} catch (Exception e) {
			System.out.println("ERROR " + e.getMessage());
			session.setAttribute("message", new Message("Something went wrong!! try again...", "alert-danger"));
			e.printStackTrace();
		}
		return "normal/add_contact_form";
	}

	// handler for showing contact
	// per page=3[n]
	// current page=0[page]
	@GetMapping("/show-contacts/{page}")
	public String showContact(@PathVariable("page") Integer page, Model model, Principal principal) {

		String userName = principal.getName();
		User user = userRepository.getUserByUserName(userName);
		Pageable pageable = PageRequest.of(page, 3);
		Page<Contact> contacts = contactRepository.findContactsByUserId(user.getId(), pageable);
		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());
		model.addAttribute("title", "Show Contact Page");
		return "normal/show_contacts";
	}

	// handler for showing particular contact
	@RequestMapping("/{cId}/contact")
	public String showContactDeatails(@PathVariable("cId") Integer cId, Model model, Principal principal) {
		System.out.println("CID " + cId);
		Optional<Contact> contactOptional = contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		// security condition
		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			model.addAttribute("title", contact.getName());
		}
		return "normal/contact_detail";
	}

	// handler for deleting particular contact
	@RequestMapping("/delete/{cid}/{page}")
	public String deleteContact(@PathVariable("cid") Integer cId, @PathVariable Integer page, Principal principal,
			HttpSession session) throws IOException {

		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		Contact contact = this.contactRepository.findById(cId).get();
		// security condition
		if (user.getId() == contact.getUser().getId()) {
			contact.setUser(null);
//			contact.getImage();
			File deleteFile = new ClassPathResource("/static/image").getFile();
			Path path = Paths.get(deleteFile.getAbsolutePath() + File.separator + contact.getImage());
			if (!contact.getImage().trim().equals("default.png")) {
				Files.deleteIfExists(path);
			}
			user.getContacts().remove(contact);
			this.userRepository.save(user);
//			this.contactRepository.delete(contact);
			System.out.println("file is deleted...");
			session.setAttribute("message", new Message("contact deleted successfully...", "alert-success"));
		} else {
			session.setAttribute("message",
					new Message("You don't have permission to delete this contact...", "alert-danger"));
		}

		return "redirect:/user/show-contacts/{page}";
	}

	// handler for showing the update contact page
	@PostMapping("/update-contact/{cid}")
	public String updateContactPage(@PathVariable Integer cid, Model model) {
		model.addAttribute("title", "Update Contact Page");
		Contact contact = this.contactRepository.findById(cid).get();
		model.addAttribute("contact", contact);
		return "normal/update_contact";
	}

	// handler for updating the contact Page
	@PostMapping("/process-update-contact/")
	public String updateContact(@ModelAttribute Contact contact, Model model,
			@RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {

		// old contact details
		Contact oldContactDetails = this.contactRepository.findById(contact.getcId()).get();
		try {
			// image
			if (!file.isEmpty()) {
				// file work
				// rewrite

				// delete old photo

				File deleteFile = new ClassPathResource("/static/image").getFile();
				File file1 = new File(deleteFile, oldContactDetails.getImage());
				file1.delete();

				// update new photo
				File saveFile = new ClassPathResource("static/image").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			} else {
				contact.setImage(oldContactDetails.getImage());
			}
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);

			this.contactRepository.save(contact);

			session.setAttribute("message", new Message("Contact updated successfully...", "alert-success"));

		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("message", new Message("something went wrong..." + e.getMessage(), "alert-danger"));

		}

		System.out.println("name: " + contact.getName());
		System.out.println("name: " + contact.getcId());

		return "redirect:/user/" + contact.getcId() + "/contact";
	}

	// handler for user profile page
	@GetMapping("/profile")
	public String profile(Model model) {
		model.addAttribute("title","Profile Page");
		return "normal/profile";
	}

	//handler for settings page 
	@GetMapping("/settings")
	public String settings(Model model) {
		model.addAttribute("title","settings");
		model.addAttribute("changepass",new Changepass());
		return "normal/settings";
	}
	
	// handler for change password...
	@PostMapping("/change-password")
	public String changePass(@Valid Changepass changepass,BindingResult result,
			Model model,Principal principal,HttpSession session) {
		System.out.println(changepass.getOldpassword());
		System.out.println(changepass.getNewpassword());
		System.out.println(changepass);
		
		if(result.hasErrors()) {
//			model.addAttribute("changepass",changepass);
			return "normal/settings";
		}
		
		User currentUser=userRepository.getUserByUserName(principal.getName());
		System.out.println(currentUser.getPassword());
		if(bCryptPasswordEncoder.matches(changepass.getOldpassword(),currentUser.getPassword())){
			//change the password
			currentUser.setPassword(bCryptPasswordEncoder.encode(changepass.getNewpassword()));
			userRepository.save(currentUser);
			session.setAttribute("message",new Message("Password changed Successfully...","alert-success"));
		}
		else {
			//error
			session.setAttribute("message",new Message("wrong oldPassword!! please check and correct old password...","alert-danger"));
			return "redirect:/user/settings";
		}
		
		return"redirect:/user/index";
	}
	
	// creating order for payment
	
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String,Object> data,Principal principal) throws RazorpayException {
		System.out.println("hey! order function ex..");
		System.out.println(data);
		
		int amt=Integer.parseInt(data.get("amount").toString());
		RazorpayClient client=new RazorpayClient("rzp_test_juBymxoCENh40e", "5FJOok4GV2AkAMeeLV8hGF1H");
		JSONObject options = new JSONObject();
		options.put("amount", amt*100); 
		options.put("currency", "INR"); 
		options.put("receipt", "txn_123456"); 
		Order order = client.Orders.create(options);
		System.out.println(order);
		
		// save order to database...
		
		MyOrder myOrder=new MyOrder();
		int amount=order.get("amount");
		myOrder.setAmount(amount/100);
//		System.out.println(order.get("order_id").toString());
		myOrder.setOrderId(order.get("id"));
		myOrder.setPaymentId(null);
		myOrder.setStatus("created");
		myOrder.setUser(this.userRepository.getUserByUserName(principal.getName()));
		myOrder.setReceipt(order.get("receipt"));
		
		this.myOrderRepository.save(myOrder);
		
		return order.toString();
	}
	
	// update order handler
	@PostMapping("/update_order")
	public ResponseEntity<?> updateOrder(@RequestBody Map<String,Object> data){
		
		System.out.println(data.get("order_id").toString());
		MyOrder myOrder=this.myOrderRepository.findByOrderId(data.get("order_id").toString());
		
		System.out.println(myOrder);
		myOrder.setPaymentId(data.get("payment_id").toString());
		myOrder.setStatus(data.get("status").toString());
		
		this.myOrderRepository.save(myOrder);
		
		System.out.println(data);
		return ResponseEntity.ok(Map.of("msg","update"));
	}
	
}
