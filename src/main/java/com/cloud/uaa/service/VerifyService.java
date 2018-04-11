package com.cloud.uaa.service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cloud.uaa.client.AuthorizedFeignClient;

@AuthorizedFeignClient(name="verify")
public interface VerifyService {

	@GetMapping("/api/verify/{phone}")
	public String getVerifyCodeByPhone(@PathVariable("phone") String phone);
	
}
