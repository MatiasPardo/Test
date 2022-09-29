package org.openxava.base.actions;


import java.util.*;

import org.openxava.actions.*;
import org.openxava.application.meta.*;
import org.openxava.base.model.*;
import org.openxava.model.*;
import org.openxava.model.meta.*;
import org.openxava.util.*;

import com.openxava.naviox.model.*;

public class EditarObjetoNegocioAction extends SearchByViewKeyAction{//extends SearchExecutingOnChangeAction{
	
	private ObjetoNegocio objetoNegocio = null;
	
	private boolean configuracionBuscada = false;
	
	private ConfiguracionEntidad configuracion = null;
	
	protected ConfiguracionEntidad getConfiguracionEntidad(){
		if (!configuracionBuscada){
			configuracionBuscada = true;
			configuracion = ConfiguracionEntidad.buscarConfigurador(getView().getModelName()); 
		}
		return configuracion;
	}
	
	protected ObjetoNegocio getObjetoNegocio() throws Exception{
		if (objetoNegocio == null){
			Object object = MapFacade.findEntity(getView().getModelName(), getView().getKeyValues());
			if (object instanceof ObjetoNegocio){
				this.objetoNegocio = (ObjetoNegocio)object;
			}
		}
		return this.objetoNegocio;
	}
	
	@Override
	public void execute() throws Exception {
		ObjetoNegocio bo = this.getObjetoNegocio();
		if (bo != null){
			// primero se asigna el nombre de la view
			String newViewName = bo.viewName();
			if (!Is.equal(newViewName, getView().getViewName())){
				@SuppressWarnings("rawtypes")
				Map clave = getView().getKeyValuesWithValue();
				getView().setViewName(newViewName);
				getView().setValues(clave);
			}
			
			
		}
				
        super.execute();
        
        
        if (bo != null){
	        if (bo.soloLectura()){
	        	getView().setEditable(false);	        	
	        }
	        else{
	        	List<String> propiedadesSoloLectura = new LinkedList<String>();
	        	List<String> propiedadesEditables = new LinkedList<String>();
	        	ConfiguracionEntidad configuracion = ConfiguracionEntidad.buscarConfigurador(getView().getModelName());
	        	bo.propiedadesSoloLecturaAlEditar(propiedadesSoloLectura, propiedadesEditables, configuracion);
	        	for(String propiedad: propiedadesEditables){
	        		getView().setEditable(propiedad, true);
	        	}
	        	for(String propiedad: propiedadesSoloLectura){
	        		getView().setEditable(propiedad, false);
	        	}
	        }
        }
        
        // Atributos ocultos
        this.ocultarAtributos();        
	}
	
	private void ocultarAtributos(){
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
		
		if (this.getConfiguracionEntidad() != null){
			if (this.getConfiguracionEntidad().getOcultarImagenes()){
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
