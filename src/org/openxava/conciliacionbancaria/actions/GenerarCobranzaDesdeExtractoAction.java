package org.openxava.conciliacionbancaria.actions;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.openxava.actions.IChainAction;
import org.openxava.actions.TabBaseAction;
import org.openxava.base.model.Transaccion;
import org.openxava.conciliacionbancaria.model.ExtractoBancario;
import org.openxava.model.MapFacade;
import org.openxava.tesoreria.model.ReciboCobranza;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;

public class GenerarCobranzaDesdeExtractoAction extends TabBaseAction implements IChainAction{

	private boolean editarTransaccion = false;
	
	@Override
	public void execute() throws Exception {
		this.editarTransaccion = false;
		
		String idCliente = this.getView().getValueString("cliente.id");
		if (!Is.emptyString(idCliente)){		
			@SuppressWarnings("rawtypes")
			Map [] selectedOnes = getSelectedKeys();
			
			@SuppressWarnings("rawtypes")
			Map clave = selectedOnes[0];
			ExtractoBancario extracto = (ExtractoBancario)MapFacade.findEntity(this.getTab().getModelName(), clave);
			if (extracto.getImporte().compareTo(BigDecimal.ZERO) > 0){
				try{
					Transaccion transaccion = extracto.generarReciboCobranza(idCliente);
					this.commit();
					
					this.getTab().deselectAll();
					this.closeDialog();
					
					// se muestra el recibo
					this.showNewView();
					Map<String, Object> key = new HashMap<String, Object>();
					key.put("id", transaccion.getId());
					getView().setModelName(ReciboCobranza.class.getSimpleName());
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
				this.addError("Importe debe ser mayor a cero");
			}
		}
		else{
			this.addError("Cliente no asignado");
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
