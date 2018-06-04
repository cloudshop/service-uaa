package com.cloud.uaa.wxlogin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import com.cloud.uaa.service.UserService;

@Component
public class WechatSecurityConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
	@Autowired
	private WechatLoginSuccessHandler wechatLoginSuccessHandler;
	@Autowired
	private UserService userService;

	@Override
	public void configure(HttpSecurity http) throws Exception {
		WechatAuthenticationFilter wechatAuthenticationFilter = new WechatAuthenticationFilter();
		wechatAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
		wechatAuthenticationFilter.setAuthenticationSuccessHandler(wechatLoginSuccessHandler);

		WechatAuthenticationProvider wechatAuthenticationProvider = new WechatAuthenticationProvider();
		wechatAuthenticationProvider.setUserService(userService);
		http.authenticationProvider(wechatAuthenticationProvider).addFilterAfter(wechatAuthenticationFilter,
				UsernamePasswordAuthenticationFilter.class);
	}
}
