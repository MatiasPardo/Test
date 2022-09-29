package com.clouderp.flowchart.actions;

import org.openxava.actions.*;
import org.openxava.util.*;
import org.openxava.validators.*;

import com.clouderp.flowchart.model.*;

public abstract class FlowChartCloudBaseAction extends CollectionElementViewBaseAction implements IForwardAction{

	public static String FINLINEA = "<br/>";
	
	public abstract void drawFlow(FlowCloud flow);
	
	@Override
	public void execute() throws Exception {
		FlowCloud flow = new FlowCloud();
		
		this.drawFlow(flow);
		
		if (!flow.getLink().isEmpty()){			
			this.getRequest().getSession().setAttribute("clouderp.flowchart", flow);
		}
		else{
			throw new ValidationException("No hay datos");			
		}
	}

	@Override
	public String getForwardURI() {
		String parameters = "";
		if (!Is.emptyString(Users.getCurrentUserInfo().getOrganization())){
			parameters = "?organization=" + Users.getCurrentUserInfo().getOrganization();
		}
		return "/flowcloud.jsp" + parameters;		
	}

	@Override
	public boolean inNewWindow() {
		return true;
	}

}
