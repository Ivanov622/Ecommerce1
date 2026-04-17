package com.app.ecom;

import java.util.List;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/users")
public class UserController {
	
	
	private final UserService userService;
	
	@GetMapping()
	public ResponseEntity<List<User>> getAllUsers(){
		return new ResponseEntity<>(userService.fetchAllUsers(), HttpStatusCode.valueOf(200));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<User> findUserById(@PathVariable("id") Long id){
		
		return userService.fetchUser(id)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());		
	}
	
	@PostMapping("")
	public ResponseEntity<String> createUser(@RequestBody User user){
		userService.addUser(user);
		return ResponseEntity.ok("User added successfully");
		//return userService.addUser(user);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<String> updateUser(@PathVariable("id") Long id,
											@RequestBody User userUpdated){
		boolean isUpdated = userService.updateUser(id, userUpdated);
		if(isUpdated)
			return ResponseEntity.ok("User updated successfully");
		return ResponseEntity.notFound().build();
	}

}
