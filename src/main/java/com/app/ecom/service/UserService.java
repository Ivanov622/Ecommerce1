package com.app.ecom.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.app.ecom.model.User;
import com.app.ecom.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {
	
	private final UserRepository userRepository;
	
//	private List<User> userList = new ArrayList<>();
//	private Long nextId = 1L;
	
	public List<User> fetchAllUsers(){
		//return userList;
		return userRepository.findAll(); // lo provee automáticamente jparepository
	}
	
	public Optional<User> fetchUser(Long id){
		return userRepository.findById(id);
		
//		return userList.stream().filter(user -> user.getId().equals(id)).findFirst();

//		for(User user: userList) {
//			if(user.getId().equals(id)) {
//				return user;
//			}
//		}
	}
	
	public void addUser(User user){
		userRepository.save(user);
		//user.setId(nextId++);
		//userList.add(user);
	}
	
	public boolean updateUser(Long id, User updatedUser) {
		
		return userRepository.findById(id)
				.map(existingUser -> {
					existingUser.setFirstName(updatedUser.getFirstName());
					existingUser.setLastName(updatedUser.getLastName());
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

}
