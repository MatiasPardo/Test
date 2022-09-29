package org.openxava.conciliacionbancaria.actions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.openxava.actions.TabBaseAction;
import org.openxava.base.model.Transaccion;
import org.openxava.conciliacionbancaria.model.ExtractoBancario;
import org.openxava.model.MapFacade;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;


public class GenerarMovimientoFinancieroDesdeExtractoAction extends TabBaseAction{

	@Override
	public void execute() throws Exception {
		
		if (Is.equalAsString(this.getView().getViewName(), "MultiplesConceptos")){
			this.generarMovimientosMultiplesConceptos();
		}
		else{
			this.genenerarMovimientoUnicoConcepto();
		}
	}

	private void generarMovimientosMultiplesConceptos() {
		String idConcepto1 = this.getView().getValueString("concepto1.id");
		BigDecimal importe1 = (BigDecimal)this.getView().getValue("importeConcepto1");
		
		String idConcepto2 = this.getView().getValueString("concepto2.id");
		BigDecimal importe2 = (BigDecimal)this.getView().getValue("importeConcepto2");
		
		Collection<ArrayList<Object>> idsConceptos = new ArrayList<ArrayList<Object>>(2);
		if (!Is.emptyString(idConcepto1) && importe1 != null){
			idsConceptos.add(new ArrayList<>(Arrays.asList(idConcepto1, importe1)));
		}
		else if (!(Is.emptyString(idConcepto1) && importe1 == null)){
			this.addError("Si asigna un concepto, debe asignar el importe");
		}
		
		if (!Is.emptyString(idConcepto2) && importe2 != null){
			idsConceptos.add(new ArrayList<>(Arrays.asList(idConcepto2, importe2)));
		}
		else if (!(Is.emptyString(idConcepto2) && importe2 == null)){
			this.addError("Si asigna un concepto, debe asignar el importe");
		}
		
		if (this.getErrors().isEmpty()){
			if (!idsConceptos.isEmpty()){
				@SuppressWarnings("rawtypes")
				Map [] selectedOnes = getSelectedKeys();
								
				for(int i = 0; i < selectedOnes.length; i++){
					@SuppressWarnings("rawtypes")
					Map clave = selectedOnes[i];
					ExtractoBancario extracto = null;
					try{
						extracto = (ExtractoBancario)MapFacade.findEntity(this.getTab().getModelName(), clave);
						Transaccion transaccion = extracto.generarMovimientoFinanciero(idsConceptos);
						this.addMessage(transaccion.toString());
						this.commit();						
					}
					catch(Exception e){
						if (e instanceof ValidationException){
							this.addError(extracto.toString() + ": " + ((ValidationException)e).getErrors());
						}
						else{
							this.addError(extracto.toString() + ": " + e.toString());
						}
						this.rollback();
					}
				}
								
				this.getTab().deselectAll();
				this.closeDialog();				
			}
			else{
				this.addError("Por lo menos debe asignar un concepto");
			}
		}		
	}

	private void genenerarMovimientoUnicoConcepto() {
		String idConcepto = this.getView().getValueString("concepto.id");
		if (!Is.emptyString(idConcepto)){		
			@SuppressWarnings("rawtypes")
			Map [] selectedOnes = getSelectedKeys();
			for(int i = 0; i < selectedOnes.length; i++){
				@SuppressWarnings("rawtypes")
				Map clave = selectedOnes[i];
				ExtractoBancario extracto = null;
				try{
					extracto = (ExtractoBancario)MapFacade.findEntity(this.getTab().getModelName(), clave);
					Transaccion transaccion = extracto.generarMovimientoFinanciero(idConcepto);
					this.addMessage(transaccion.toString());
					this.commit();
				}
				catch(Exception e){
					this.addError(extracto.toString() + ": " + e.toString());
					this.rollback();
				}
			}						
			this.getTab().deselectAll();
			this.closeDialog();
		}
		else{
			this.addError("Concepto no asignado");
		}		
	}
}
