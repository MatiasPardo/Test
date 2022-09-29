package org.openxava.base.actions;

import java.lang.reflect.*;
import java.util.*;

import org.openxava.actions.*;
import org.openxava.application.meta.*;
import org.openxava.base.model.*;
import org.openxava.model.meta.*;
import org.openxava.util.*;

import com.openxava.naviox.model.*;

public class CrearObjetoNegocioAction extends NewAction{
	
	private ConfiguracionEntidad configuracion = null;
	
	private boolean configuracionBuscada = false;
	
	protected ConfiguracionEntidad getConfiguracionEntidad(){
		if (!configuracionBuscada){
			configuracion = ConfiguracionEntidad.buscarConfigurador(getView().getModelName());
			configuracionBuscada = true;
		}
		return configuracion;
	}
	
	@Override
	public void execute() throws Exception {
		
		super.execute();
		
		ConfiguracionEntidad entidad = this.getConfiguracionEntidad();
		List<String> propiedadesSoloLectura = new LinkedList<String>();
    	List<String> propiedadesEditables = new LinkedList<String>();
    	this.propiedadesSoloLectura(propiedadesSoloLectura, propiedadesEditables, entidad);
    	for(String propiedad: propiedadesEditables){
    		getView().setEditable(propiedad, true);
    	}
    	for(String propiedad: propiedadesSoloLectura){
    		getView().setEditable(propiedad, false);
    	} 
    	
    	this.ocultarAtributos(entidad);
	}
	
	protected void propiedadesSoloLectura(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		// se aplican las propiedades solo lectura del objeto
		try{
			ObjetoNegocio objetoNegocio = (ObjetoNegocio)this.getView().getMetaModel().getPOJOClass().newInstance();
			Method method = objetoNegocio.getClass().getMethod("propiedadesSoloLecturaAlCrear", List.class, List.class, ConfiguracionEntidad.class);
			method.invoke(objetoNegocio, propiedadesSoloLectura, propiedadesEditables, configuracion);
		}
		catch(Exception e){			
		}
	}
	
	private void ocultarAtributos(ConfiguracionEntidad entidad){
		try{
			User user = User.find(Users.getCurrent());
	        MetaModule metaModule = MetaApplications.getMetaApplication(MetaApplications.getApplicationsNames().iterator().next().toString()).getMetaModule(this.getView().getModelName());
	        Collection<MetaMember> collection = user.getExcludedMetaMembersForMetaModule(metaModule);
	        for(MetaMember member: collection){
	        	this.getView().setHidden(member.getName(), true);
	        }
		}
		catch(Exception e){
			addError("Error exclusión de atributos: " + e.toString());
		}
		
		if (entidad != null){
	    	if (entidad.getOcultarImagenes()){
	        	// por defecto se muestran las imagenes
	    		try{
	    			getView().getSubview("imagen");
	    			getView().setHidden("imagen", true);
	    		}
	    		catch(ElementNotFoundException e){    			
	    		}
	    	}
    	}
	}
}
