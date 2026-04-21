package com.app.ecom.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.app.ecom.model.User;
import com.app.ecom.service.UserService;
import com.app.ecom.dto.UserRequest;
import com.app.ecom.dto.UserResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/users")
public class UserController {
	
	
	private final UserService userService;
	
	@GetMapping()
	public ResponseEntity<List<UserResponse>> getAllUsers(){
		return new ResponseEntity<>(userService.fetchAllUsers(), HttpStatus.OK);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<UserResponse> findUserById(@PathVariable("id") Long id){
		
		return userService.fetchUser(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());		
	}
	
	@PostMapping("")
	public ResponseEntity<String> createUser(@RequestBody UserRequest userRequest){
		userService.addUser(userRequest);
		return ResponseEntity.ok("User added successfully");
		//return userService.addUser(user);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<String> updateUser(@PathVariable("id") Long id,
											@RequestBody UserRequest updatedUserRequest){
		boolean isUpdated = userService.updateUser(id, updatedUserRequest);
		if(isUpdated)
			return ResponseEntity.ok("User updated successfully");
		return ResponseEntity.notFound().build();
	}

}
