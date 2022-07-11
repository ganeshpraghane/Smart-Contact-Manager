package com.smart.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import com.smart.helper.ValidPassword;
@Entity
@Table(name="CHANGEPASS")
public class Changepass {

	@Id 
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
//	@NotBlank(message="this field is manadatory")
	private String oldpassword;
	@NotBlank(message="this field is manadatory")
	@ValidPassword
	@Valid
	private String newpassword;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getOldpassword() {
		return oldpassword;
	}
	public void setOldpassword(String oldpassword) {
		this.oldpassword = oldpassword;
	}
	public String getNewpassword() {
		return newpassword;
	}
	public void setNewpassword(String newpassword) {
		this.newpassword = newpassword;
	}
	@Override
	public String toString() {
		return "Changepass [id=" + id + ", oldpassword=" + oldpassword + ", newpassword=" + newpassword + "]";
	}
}
