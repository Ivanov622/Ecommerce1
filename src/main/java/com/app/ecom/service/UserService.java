package com.app.ecom.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.app.ecom.dto.UserResponse;
import com.app.ecom.model.User;
import com.app.ecom.repository.UserRepository;
import com.app.ecom.dto.AddressDTO;
import com.app.ecom.dto.UserRequest;
import com.app.ecom.model.Address;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {
	
	private final UserRepository userRepository;
	
//	private List<User> userList = new ArrayList<>();
//	private Long nextId = 1L;
	
	public List<UserResponse> fetchAllUsers(){
		
	//	List<User> userList = userRepository.findAll();
		return userRepository.findAll().stream()
				.map(this::mapToUserResponse)
				.collect(Collectors.toList());

		//return userList;
		//return userRepository.findAll(); // lo provee automáticamente jparepository
	}
	
	public Optional<UserResponse> fetchUser(Long id){
		
		
		return userRepository.findById(id).map(this::mapToUserResponse);
		
//		return userList.stream().filter(user -> user.getId().equals(id)).findFirst();

//		for(User user: userList) {
//			if(user.getId().equals(id)) {
//				return user;
//			}
//		}
	}
	
	public void addUser(UserRequest userRequest){
		
		User user = new User();
		updateUserFromRequest(user, userRequest);		
		userRepository.save(user);
		
		//user.setId(nextId++);
		//userList.add(user);
	}
	
	public boolean updateUser(Long id, UserRequest updatedUserRequest) {
		
		return userRepository.findById(id)
				.map(existingUser -> {
					updateUserFromRequest(existingUser, updatedUserRequest);
		//			existingUser.setFirstName(updatedUser.getFirstName());
		//			existingUser.setLastName(updatedUser.getLastName());
					userRepository.save(existingUser);
					return true;
				}).orElse(false);
		
		/*
		 * return userList.stream().filter(user -> user.getId().equals(id)).findFirst()
		 * .map(existingUser -> { existingUser.setFirstName(updatedUser.getFirstName());
		 * existingUser.setLastName(updatedUser.getLastName()); return true;
		 * }).orElse(false);
		 */
		
	}
	
	public UserResponse mapToUserResponse (User user) {
		
		UserResponse userResponse = new UserResponse();
		userResponse.setId(String.valueOf(user.getId()));
		userResponse.setFirstName(user.getFirstName());
		userResponse.setLastName(user.getLastName());
		userResponse.setEmail(user.getEmail());
		userResponse.setPhone(user.getPhone());
		userResponse.setRole(user.getRole());
		
		if(user.getAddress() != null) {
			AddressDTO addressDTO = new AddressDTO();
			addressDTO.setStreet(user.getAddress().getStreet());
			addressDTO.setCity(user.getAddress().getCity());
			addressDTO.setState(user.getAddress().getState());
			addressDTO.setCountry(user.getAddress().getCountry());
			addressDTO.setZipcode(user.getAddress().getZipcode());
			
			userResponse.setAddress(addressDTO);
		}
		
		return userResponse;		
	}
	
	public User updateUserFromRequest(User user, UserRequest userRequest) {
		
		user.setFirstName(userRequest.getFirstName());
		user.setLastName(userRequest.getLastName());
		user.setEmail(userRequest.getEmail());
		user.setPhone(userRequest.getPhone());
		
		if(userRequest.getAddress() != null) {
			
			Address address = new Address();
			address.setStreet(userRequest.getAddress().getStreet());
			address.setCity(userRequest.getAddress().getCity());
			address.setState(userRequest.getAddress().getState());
			address.setCountry(userRequest.getAddress().getCountry());
			address.setZipcode(userRequest.getAddress().getZipcode());
			
			user.setAddress(address);
			
		}
		
		return user;
		
	}

}
