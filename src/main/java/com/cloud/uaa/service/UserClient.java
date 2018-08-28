package com.cloud.uaa.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cloud.uaa.client.AuthorizedFeignClient;
import com.cloud.uaa.service.dto.UserAnnexDTO;

@AuthorizedFeignClient(name="user")
public interface UserClient {

	@PostMapping("/api/user-annexes")
	public ResponseEntity<UserAnnexDTO> createUserAnnex(@RequestBody UserAnnexDTO userAnnexDTO);
	
}
