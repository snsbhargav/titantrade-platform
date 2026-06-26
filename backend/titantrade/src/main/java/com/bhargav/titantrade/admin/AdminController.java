package com.bhargav.titantrade.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bhargav.titantrade.common.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
	
	@GetMapping("/test")
	public ApiResponse test() {
		return new ApiResponse(true, "Welcome admin", null);
	}

}
