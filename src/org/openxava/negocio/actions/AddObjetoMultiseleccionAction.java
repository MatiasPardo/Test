package org.openxava.negocio.actions;

import java.util.*;

import javax.inject.*;

import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.model.*;
import org.openxava.tab.*;

public class AddObjetoMultiseleccionAction  extends CollectionElementViewBaseAction implements IChainAction{	
	
	private int row = -1;
	
	@Inject
	private Tab tab;
		
	private boolean cancelar = false;
		
	@Override
	public void execute() throws Exception {
		Map<?, ?> [] objetosSeleccionados = getTab().getSelectedKeys();
		
		if (this.getRow() >= 0){
			Map<?, ?> key = (Map<?, ?>) getTab().getTableModel().getObjectAt(this.getRow());
			if (key != null){
				objetosSeleccionados = new Map<?, ?>[1];
				objetosSeleccionados[0] = key;				
			}
		}
		
		if (objetosSeleccionados != null && objetosSeleccionados.length > 0) {
			this.cancelar = true;
			try{
				Transaccion transaccion = (Transaccion)MapFacade.findEntity(this.getView().getModelName(), this.getView().getKeyValues());
				transaccion.agregarItemsMultiseleccion(objetosSeleccionados);
				this.commit();
				addMessage("ejecucion_OK");
			}
			catch(Exception e){
				this.rollback();
				if (e.getMessage() != null){
					addError(e.getMessage());
				}
				else{
					addError(e.toString());
				}
			}
		}		
	}

	@Override
	public String getNextAction() throws Exception {
		if (this.cancelar){
			return "Multiseleccion.cancel";
		}
		else{
			return null;
		}		
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public Tab getTab() {
		return tab;
	}

	public void setTab(Tab tab) {
		this.tab = tab;
	}
}

