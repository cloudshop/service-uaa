package com.cloud.uaa.wxlogin;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.stereotype.Component;

@Component
public class WechatLoginSuccessHandler implements org.springframework.security.web.authentication.AuthenticationSuccessHandler {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private ClientDetailsService clientDetailsService;
	
	@Autowired
	private AuthorizationServerTokenServices authorizationServerTokenServices;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) {
		String header = request.getHeader("Authorization");

		if (header == null || !header.startsWith("Basic ")) {
			throw new UnapprovedClientAuthenticationException("请求头中client信息为空");
		}

		try {
			String[] tokens = extractAndDecodeHeader(header);
			assert tokens.length == 2;
			String clientId = tokens[0];
			String clientSecret = tokens[1];

			JSONObject params = new JSONObject();
			params.put("clientId", clientId);
			params.put("clientSecret", clientSecret);
			params.put("authentication", authentication);

			ClientDetails clientDetails = clientDetailsService.loadClientByClientId(clientId);
			TokenRequest tokenRequest = new TokenRequest(new HashMap<>(), clientId, clientDetails.getScope(), "mobile");
			OAuth2Request oAuth2Request = tokenRequest.createOAuth2Request(clientDetails);

			OAuth2Authentication oAuth2Authentication = new OAuth2Authentication(oAuth2Request, authentication);
			OAuth2AccessToken oAuth2AccessToken = authorizationServerTokenServices
					.createAccessToken(oAuth2Authentication);
			logger.info("获取token 成功：{}", oAuth2AccessToken.getValue());

			response.setCharacterEncoding("utf-8");
			response.setContentType("application/json");
			PrintWriter printWriter = response.getWriter();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("access_token", oAuth2AccessToken.getValue());
			jsonObject.put("token_type", oAuth2AccessToken.getTokenType());
			jsonObject.put("refresh_token", oAuth2AccessToken.getRefreshToken().getValue());
			jsonObject.put("expires_in", oAuth2AccessToken.getExpiresIn());
			jsonObject.put("scope", oAuth2AccessToken.getScope());
			jsonObject.put("iat", oAuth2AccessToken.getAdditionalInformation().get("iat"));
			jsonObject.put("jti", oAuth2AccessToken.getAdditionalInformation().get("jti"));
			printWriter.print(jsonObject);
		} catch (Exception e) {
			throw new BadCredentialsException("Failed to decode basic authentication token");
		}
	}

	/**
	 * Decodes the header into a username and password.
	 *
	 * @throws BadCredentialsException
	 *             if the Basic header is not present or is not valid Base64
	 */
	private String[] extractAndDecodeHeader(String header) throws IOException {

		byte[] base64Token = header.substring(6).getBytes("UTF-8");
		byte[] decoded;
		try {
			decoded = Base64.decode(base64Token);
		} catch (IllegalArgumentException e) {
			throw new BadCredentialsException("Failed to decode basic authentication token");
		}

		String token = new String(decoded, "UTF8");

		int delim = token.indexOf(":");

		if (delim == -1) {
			throw new BadCredentialsException("Invalid basic authentication token");
		}
		return new String[] { token.substring(0, delim), token.substring(delim + 1) };
	}
}
