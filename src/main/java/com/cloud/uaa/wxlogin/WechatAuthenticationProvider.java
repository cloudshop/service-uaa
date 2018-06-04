package com.cloud.uaa.wxlogin;

import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.client.RestTemplate;

import com.cloud.uaa.domain.User;
import com.cloud.uaa.service.UserService;

import io.undertow.util.BadRequestException;

public class WechatAuthenticationProvider implements AuthenticationProvider {
	private UserService userService;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		WechatAuthenticationToken wechatAuthenticationToken = (WechatAuthenticationToken) authentication;
		WeChatLogin wechatLogin = (WeChatLogin) wechatAuthenticationToken.getPrincipal();
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new WxMappingJackson2HttpMessageConverter());
		ResponseEntity<WxUser> responseEntity = restTemplate.getForEntity("https://api.weixin.qq.com/sns/userinfo?access_token="+wechatLogin.getAccessToken()+"&openid="+wechatLogin.getOpenid(), WxUser.class);
		WxUser wxUser = responseEntity.getBody();
		Optional<User> userByWechatUnion = userService.getUserByWechatUnionid(wxUser.getUnionid());
		//Optional<User> userWithAuthoritiesByLogin = userService.getUserWithAuthoritiesByLogin((String) mobileAuthenticationToken.getPrincipal());

		org.springframework.security.core.userdetails.User userDetails = buildUserDeatils(userByWechatUnion.get());

		WechatAuthenticationToken authenticationToken = new WechatAuthenticationToken(userDetails,
				userDetails.getAuthorities());
		authenticationToken.setDetails(wechatAuthenticationToken.getDetails());
		return authenticationToken;
	}

	private org.springframework.security.core.userdetails.User buildUserDeatils(User user) {
		return new org.springframework.security.core.userdetails.User(user.getLogin(), user.getPassword(), user.getAuthorities().stream()
	            .map(authority -> new SimpleGrantedAuthority(authority.getName()))
	            .collect(Collectors.toList()));
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return WechatAuthenticationToken.class.isAssignableFrom(authentication);
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}
}