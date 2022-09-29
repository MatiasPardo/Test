package org.openxava.base.actions;

import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;

public class EditarItemTransaccionAction extends EditElementInCollectionAction{

	public void execute() throws Exception{		
		super.execute();
		addActions("ItemTransaccion.saveAndNext");
		
		this.evaluarAtributos();
		
	}
	
	private void evaluarAtributos(){
		try{
			List<String> propiedadesSoloLectura = new LinkedList<String>();
	    	List<String> propiedadesEditables = new LinkedList<String>();
	    	ObjetoNegocio bo = (ObjetoNegocio)MapFacade.findEntity(this.getCollectionElementView().getModelName(), this.getCollectionElementView().getKeyValues());
	    	bo.propiedadesSoloLecturaAlEditar(propiedadesSoloLectura, propiedadesEditables, null);
	    	for(String propiedad: propiedadesEditables){
	    		getCollectionElementView().setEditable(propiedad, true);
	    	}
	    	for(String propiedad: propiedadesSoloLectura){
	    		getCollectionElementView().setEditable(propiedad, false);
	    	}
	    	
	    	List<String> propiedadesOcultar = new LinkedList<String>();
	    	List<String> propiedadesVisibles = new LinkedList<String>();
	    	bo.propiedadesOcultas(propiedadesOcultar, propiedadesVisibles);
	    	for(String propiedad: propiedadesOcultar){
	    		getCollectionElementView().setHidden(propiedad, true);
	    	}
	    	for(String propiedad: propiedadesVisibles){
	    		getCollectionElementView().setHidden(propiedad, false);
	    	}
		}catch(Exception e){			
		}
	}
	
}
