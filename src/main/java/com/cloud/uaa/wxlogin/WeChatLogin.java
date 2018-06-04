package com.cloud.uaa.wxlogin;

public class WeChatLogin {

	private String accessToken;
	private String openid;
	public WeChatLogin(String accessToken, String openid) {
		this.accessToken = accessToken;
		this.openid = openid;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getOpenid() {
		return openid;
	}
	public void setOpenid(String openid) {
		this.openid = openid;
	}
	
}
