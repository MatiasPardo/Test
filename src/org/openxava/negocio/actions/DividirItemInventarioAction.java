package org.openxava.negocio.actions;


import java.util.*;

import org.openxava.actions.*;
import org.openxava.base.actions.*;
import org.openxava.model.*;
import org.openxava.negocio.model.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

public class DividirItemInventarioAction extends GrabarItemTransaccionYEditarSiguienteAction implements IChainAction{

	private String id;
	
	@Override
	public void execute() throws Exception {
		
		String modelName = this.getCollectionElementView().getModelName();
		@SuppressWarnings("rawtypes")
		Map keyValues = this.getCollectionElementView().getKeyValues();
		IDivisionItemTransaccion item = (IDivisionItemTransaccion)MapFacade.findEntity(modelName, keyValues);
		this.validacionesPreDividir(item);
		
		super.execute();
		
		item = (IDivisionItemTransaccion) MapFacade.findEntity(modelName, keyValues);
		id = item.dividirConNuevoItem().getId();
		
		this.commit();
		
	}
	
	private void validacionesPreDividir(IDivisionItemTransaccion item) {
		
		item.validacionesPreDividir();
		Producto productoConLote = item.getProducto();
		if(!productoConLote.getLote()){
			throw new ValidationException("Accion solo disponible para productos con lote");
		}
		
		
		
	}


	@Override
	public String getNextActionArgv() throws Exception {
		if (this.getErrors().isEmpty()){
			return "id=" + id;
		}
		else{
			return null;
		}
	}
	
	@Override
	public boolean editarProximo(){
		return true;
	}
	
	

}
