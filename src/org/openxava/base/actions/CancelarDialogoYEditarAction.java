package org.openxava.base.actions;

import org.openxava.actions.IChainAction;
import org.openxava.actions.ViewBaseAction;
import org.openxava.util.Is;

public class CancelarDialogoYEditarAction extends ViewBaseAction implements IChainAction{
	
private String accionEditar = null;
	
	@Override
	public void execute() throws Exception {
		this.closeDialog();		
	}
	
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
		
		if (Is.emptyString(accionEditar)){
			// no hay proxima accion vamos a volver a modo lista.
			accionEditar = "Mode.list";
		}
		return accionEditar;
	}	
}
