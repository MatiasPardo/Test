package org.openxava.base.actions;

import org.openxava.actions.*;
import org.openxava.util.*;

public class VolverYEditarAction extends ReturnAction implements IChainAction{

	private String accionEditar = null;
	
	public String getAccionEditar() {
		return accionEditar;
	}

	public void setAccionEditar(String accionEditar) {
		this.accionEditar = accionEditar;
	}

	@Override
	public String getNextAction() throws Exception {	
		String accionEditar = this.getAccionEditar();
		if (Is.emptyString(this.getAccionEditar())){
			try{
				if (!this.getView().getKeyValues().isEmpty()){
					accionEditar = (String)this.getEnvironment().getValue("XAVA_SEARCH_ACTION");
				}
			}
			catch(Exception e){
				accionEditar = null;
			}
		}
		
		if (!Is.emptyString(accionEditar)){
			if (this.getView().getKeyValues().isEmpty()){
				accionEditar = null;
			}
			else{
				for(Object value: this.getView().getKeyValues().values()){
					if (Is.empty(value)){
						accionEditar = null;
					}
					break;
				}
			}
		}		
		return accionEditar;
	}
}
