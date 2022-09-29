package com.clouderp.maps.actions;

import java.util.Map;

import org.openxava.model.MapFacade;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;

import com.clouderp.maps.model.AddressCloud;
import com.clouderp.maps.model.IObjectMapCloud;
import com.clouderp.maps.model.MapCloud;

public abstract class ShowMapSelectedForPolygonAction extends MapsCloudBaseAction{

	private String classNameMapAction;
	
	public String getClassNameMapAction() {
		return classNameMapAction;
	}

	public void setClassNameMapAction(String classNameMapAction) {
		this.classNameMapAction = classNameMapAction;
	}

	protected Class<?> getMapSelectionAction(){
		try{
			return Class.forName(this.getClassNameMapAction());
		}
		catch(Exception e){
			throw new ValidationException("No se pudo instanciar la clase " + this.getClassNameMapAction() + ": " + e.toString());
		}
	}
	
	@Override
	public void buildMap(MapCloud map) {
		if (Is.emptyString(this.getClassNameMapAction())){
			throw new ValidationException("Falta asignar classNameMapAction ");
		}
		
		if (this.getSelectedKeys().length > 0){
			for(@SuppressWarnings("rawtypes") Map key: this.getSelectedKeys()){
				try{
					IObjectMapCloud objetoMapCloud = (IObjectMapCloud)MapFacade.findEntity(this.getTab().getModelName(), key);
					AddressCloud address = objetoMapCloud.addressMapCloud(map);
					if (address == null){
						addError("Comprobante sin coordenadas: " + objetoMapCloud.toString());
					}
					else{
						address.setCodigo(key.get("id").toString());
					}
				}
				catch(Exception e){					
				}
			}
		}
		else{
			this.addError("sin_seleccionar_items");
		}				
	}
}
