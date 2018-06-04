package com.cloud.uaa.wxlogin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

public class WechatAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
	//public static final String SPRING_SECURITY_FORM_MOBILE_KEY = "mobile";

	//private String mobileParameter = SPRING_SECURITY_FORM_MOBILE_KEY;
	private boolean postOnly = true;

	public WechatAuthenticationFilter() {
		super(new AntPathRequestMatcher("/wechat/token", "POST"));
	}

	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException {
		if (postOnly && !request.getMethod().equals(HttpMethod.POST.name())) {
			throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
		}

//		String mobile = obtainMobile(request);
//
//		if (mobile == null) {
//			mobile = "";
//		}
//
//		mobile = mobile.trim();

		//MobileAuthenticationToken mobileAuthenticationToken = new MobileAuthenticationToken(mobile);
		String accessToken = request.getParameter("accessToken");
		String openid = request.getParameter("openid");
		WechatAuthenticationToken wechatAuthenticationToken = new WechatAuthenticationToken(new WeChatLogin(accessToken, openid));

		setDetails(request, wechatAuthenticationToken);

		return this.getAuthenticationManager().authenticate(wechatAuthenticationToken);
	}

//	protected String obtainMobile(HttpServletRequest request) {
//		return request.getParameter(mobileParameter);
//	}

	protected void setDetails(HttpServletRequest request, WechatAuthenticationToken authRequest) {
		authRequest.setDetails(authenticationDetailsSource.buildDetails(request));
	}

	public void setPostOnly(boolean postOnly) {
		this.postOnly = postOnly;
	}

//	public String getMobileParameter() {
//		return mobileParameter;
//	}
//
//	public void setMobileParameter(String mobileParameter) {
//		this.mobileParameter = mobileParameter;
//	}

	public boolean isPostOnly() {
		return postOnly;
	}
}