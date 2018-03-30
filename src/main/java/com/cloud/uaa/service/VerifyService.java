package com.cloud.uaa.service;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value="verify")
public interface VerifyService {

	@GetMapping("/api/verify/{phone}")
	public String getVerifyCodeByPhone(@PathVariable String phone);
	
}
