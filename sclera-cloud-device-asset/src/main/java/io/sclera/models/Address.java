package io.sclera.models;

import java.math.BigInteger;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * JPA entity representing a postal address (street, city, state, country, zip) associated
 * one-to-one with a {@link User} in the asset-management domain.
 */
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class , property = "id" , scope = Address.class)
public class Address {

	@Id
//	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private String id;

	private String address;
	
	@Column(length = 64)
	private String city;
	
	@Column(length = 64)
	private String country;
	
	@Column(length = 64)
	private String state;

	@Column(length = 8)
	private Integer zip;
	
	@OneToOne(mappedBy = "address")
	private User user;


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
		user.setAddress(this);
	}

	

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Integer getZip() {
		return zip;
	}

	public void setZip(Integer zip) {
		this.zip = zip;
	}

	

	
	
	
	
}
