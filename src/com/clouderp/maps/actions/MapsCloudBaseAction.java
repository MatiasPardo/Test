package com.clouderp.maps.actions;

import org.openxava.actions.IForwardAction;
import org.openxava.actions.TabBaseAction;
import org.openxava.base.model.Esquema;
import org.openxava.util.Is;
import org.openxava.util.Users;
import org.openxava.validators.ValidationException;

import com.clouderp.maps.model.MapCloud;

public abstract class MapsCloudBaseAction extends TabBaseAction implements IForwardAction{

	public abstract void buildMap(MapCloud map);
	
	protected Class<?> getMapSelectionAction(){
		return null;
	}
	
	@Override
	public void execute() throws Exception {
		MapCloud map = Esquema.getEsquemaApp().crearMapCloud();
		this.buildMap(map);
		
		if (!map.getAddress().isEmpty() && this.getErrors().isEmpty()){			
			this.getRequest().getSession().setAttribute("clouderp.map", map);
		}
		else{
			throw new ValidationException("No hay datos, falta asignar longitud y latitud");			
		}
	}
	
	@Override
	public String getForwardURI() {
		String parameters = "";
		if (!Is.emptyString(Users.getCurrentUserInfo().getOrganization())){
			parameters = "?organization=" + Users.getCurrentUserInfo().getOrganization();
		}
		
		if (this.getMapSelectionAction() == null){
			return Esquema.redireccionarUrl("mapcloud.jsp" + parameters);
		}
		else{
			this.getRequest().getSession().setAttribute("clouderp.mapaction", this.getMapSelectionAction());
			return Esquema.redireccionarUrl("mapcloudselect.jsp" + parameters);
		}
	}
	
	@Override
	public boolean inNewWindow() {
		return false;
	}

}
