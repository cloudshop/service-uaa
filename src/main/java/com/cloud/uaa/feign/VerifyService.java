package com.cloud.uaa.feign;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("verify")
public interface VerifyService {

	@GetMapping("/api/verify/{phone}")
	public String getVerifyCodeByPhone(@PathVariable String phone);
	
}
