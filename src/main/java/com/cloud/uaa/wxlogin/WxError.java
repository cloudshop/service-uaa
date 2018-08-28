package com.cloud.uaa.wxlogin;

public class WxError {

	private Integer errcode; 
	private String errmsg;
	public Integer getErrcode() {
		return errcode;
	}
	public void setErrcode(Integer errcode) {
		this.errcode = errcode;
	}
	public String getErrmsg() {
		return errmsg;
	}
	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	} 
	public boolean isError(){
		if (errcode == 0 || errcode == null) {
			return false;
		} else {
			return true;
		}
		
	}
	
}
