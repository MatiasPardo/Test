package org.openxava.base.actions;

import java.util.*;

import javax.ejb.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;
import org.openxava.util.*;

public class EditarItemTransaccionSiguienteAction extends CollectionElementViewBaseAction{

	private int row;

	private String id = null;

	@SuppressWarnings({ "rawtypes" })
	public void execute() throws Exception {		

		Collection elements;
		Map keys = null;
				
		if(id == null){

			if (getCollectionElementView().isCollectionFromModel()) {		
				elements = getCollectionElementView().getCollectionValues();
				if (elements == null) return;
				if (elements instanceof List) {
					keys = (Map) ((List) elements).get(getRow());			
				}
			} else {
				
					keys = (Map) getCollectionElementView().getCollectionTab().getTableModel().getObjectAt(row);
					
			}
			if (keys != null) {
				
				loadNewElementCollection(keys);
				this.evaluarAtributosSoloLectura();
				
			} else {
					throw new XavaException("only_list_collection_for_aggregates");
			}
								
		}else {
			Map<String,String> keyMap = new HashMap<String,String>();
			keyMap.put("id", id);
			keys = keyMap;
			
			loadNewElementCollection(keys);
			this.evaluarAtributosSoloLectura();
			
		}
	}

	@SuppressWarnings("rawtypes")
	private void loadNewElementCollection(Map keys) throws FinderException {
		Map values = MapFacade.getValues(getCollectionElementView().getModelName(), keys, getCollectionElementView().getMembersNames());			
		getCollectionElementView().setValues(values);						
		getCollectionElementView().setCollectionEditingRow(getRow());
	}
	
	public int getRow() {
		return row;
	}

	public void setRow(int i) {
		row = i;
	}
	
	private void evaluarAtributosSoloLectura(){
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
		}catch(Exception e){			
		}
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
