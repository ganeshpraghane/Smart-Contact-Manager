package com.smart.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.servlet.annotation.MultipartConfig;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="Contact")
public class Contact {
@Id
@GeneratedValue(strategy = GenerationType.AUTO)
private int cId;
@NotBlank(message="name is mandatory")
//@Size(min=2,max=10,message="character must be between 2 and 10")
@Pattern(regexp="^[A-Za-z]*$",message = "Invalid Input")
private String name;
private String secondName;
@NotBlank(message="work is mandatory")
private String work;
@NotBlank(message="email is manadatory")
@Email
private String email;
private String image;
@NotBlank(message="phone is manadatory")
@Size(min=10,max=10,message="phone should be 10 digits")
private String phone;
@Column(length=1000)
private String description;
@ManyToOne
@JsonIgnore
private User user;
public int getcId() {
	return cId;
}
public void setcId(int cId) {
	this.cId = cId;
}
public String getName() {
	return name;
}
public void setName(String name) {
	this.name = name;
}
public String getSecondName() {
	return secondName;
}
public void setSecondName(String secondName) {
	this.secondName = secondName;
}
public String getWork() {
	return work;
}
public void setWork(String work) {
	this.work = work;
}
public String getEmail() {
	return email;
}
public void setEmail(String email) {
	this.email = email;
}
public String getImage() {
	return image;
}
public void setImage(String image) {
	this.image = image;
}
public String getPhone() {
	return phone;
}
public void setPhone(String phone) {
	this.phone = phone;
}
public String getDescription() {
	return description;
}
public void setDescription(String description) {
	this.description = description;
}
public User getUser() {
	return user;
}
public void setUser(User user) {
	this.user = user;
}
//@Override
//public String toString() {
//	return "Contact [cId=" + cId + ", name=" + name + ", secondName=" + secondName + ", work=" + work + ", email="
//			+ email + ", image=" + image + ", phone=" + phone + ", description=" + description + ", user=" + user + "]";
//}
@Override
public boolean equals(Object obj) {
	// TODO Auto-generated method stub
	return this.cId==((Contact)obj).getcId();
}


}
