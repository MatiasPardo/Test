package org.openxava.conciliacionbancaria.actions;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.openxava.actions.IChainAction;
import org.openxava.actions.TabBaseAction;
import org.openxava.base.model.Transaccion;
import org.openxava.conciliacionbancaria.model.ExtractoBancario;
import org.openxava.model.MapFacade;
import org.openxava.tesoreria.model.PagoProveedores;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;

public class GenerarPagoDesdeExtractoAction extends TabBaseAction implements IChainAction{
	
	private boolean editarTransaccion = false;
	
	@Override
	public void execute() throws Exception {
		this.editarTransaccion = false;
		
		String idProveedor = this.getView().getValueString("proveedor.id");
		if (!Is.emptyString(idProveedor)){		
			@SuppressWarnings("rawtypes")
			Map [] selectedOnes = getSelectedKeys();
			
			@SuppressWarnings("rawtypes")
			Map clave = selectedOnes[0];
			ExtractoBancario extracto = (ExtractoBancario)MapFacade.findEntity(this.getTab().getModelName(), clave);
			if (extracto.getImporte().compareTo(BigDecimal.ZERO) < 0){
				try{
					Transaccion transaccion = extracto.generarPagoProveedores(idProveedor);
					this.commit();
					
					this.getTab().deselectAll();
					this.closeDialog();
					
					// se muestra el recibo
					this.showNewView();
					Map<String, Object> key = new HashMap<String, Object>();
					key.put("id", transaccion.getId());
					getView().setModelName(PagoProveedores.class.getSimpleName());
					getView().setValues(key);
					getView().findObject();                               
		            getView().setKeyEditable(false);
		            
		            String[] controladores = new String[1];
		            controladores[0] = "TransaccionGeneradaDesdeDialogo";			            
		            this.setControllers(controladores);
		            
		            this.editarTransaccion = true;
				}
				catch(ValidationException v){
					this.addError(extracto.toString() + ": " + v.getMessage());
					this.rollback();
					
					this.getTab().deselectAll();
					this.closeDialog();
				}
				catch(Exception e){
					this.addError(extracto.toString() + ": " + e.toString());
					this.rollback();
					
					this.getTab().deselectAll();
					this.closeDialog();
				}
			}
			else{
				this.addError("Importe debe ser menor a cero");
			}
		}
		else{
			this.addError("Proveedor no asignado");
		}		
	}

	@Override
	public String getNextAction() throws Exception {
		if (this.editarTransaccion){
			return "Transaccion.editar";
		}
		else{
			return null;
		}
	}
}

