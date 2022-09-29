package com.clouderp.web.permanentlinks;

import org.openxava.util.Is;
import org.openxava.util.Users;

public class PermanentLinks {
	
	public static String relativeUrlModeList(String module){
		String organization = Users.getCurrentUserInfo().getOrganization();
		String url = "";
		if (!Is.emptyString(organization)){
			url = "o/" + organization;	
		}
		url += "/m/";
		url += module;
		url += "?retainOrder=true";
		return url;
	}
	
}
