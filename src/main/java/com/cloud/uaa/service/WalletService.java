package com.cloud.uaa.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cloud.uaa.client.AuthorizedFeignClient;
import com.cloud.uaa.service.dto.WalletDTO;

@AuthorizedFeignClient(name="wallet")
public interface WalletService {

	@PostMapping("/api/wallets")
	public ResponseEntity<WalletDTO> createdWallet(@RequestBody WalletDTO walletDTO);
	
}
