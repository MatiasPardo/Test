package org.openxava.contratos.actions;

import java.util.Date;

import org.openxava.actions.OnChangePropertyBaseAction;
import org.openxava.contratos.model.CicloFacturacion;
import org.openxava.contratos.model.FrecuenciaCicloFacturacion;
import org.openxava.contratos.model.TipoVencimientoCicloFacturacion;

public class OnChangeFechaNovedadCicloFacturacion extends OnChangePropertyBaseAction{

	@Override
	public void execute() throws Exception {
		if (this.getNewValue() != null){
			
			Date fechaNovedad = (Date)this.getView().getValue("fechaNovedad");			
			FrecuenciaCicloFacturacion frecuencia = (FrecuenciaCicloFacturacion)this.getView().getValue("frecuencia");
			Integer diaFechaEmision = (Integer)this.getView().getValue("diaFechaEmision");
			Integer diaFechaVencimiento = (Integer)this.getView().getValue("diaFechaVencimiento");
			Boolean primerFacturaEmiteCualquierDia = (Boolean)this.getView().getValue("primerFacturaEmiteCualquierDia");
						
			CicloFacturacion ciclo = new CicloFacturacion();
			ciclo.setFrecuencia(frecuencia);
			ciclo.setDiaFechaEmision(diaFechaEmision);
			ciclo.setDiaFechaVencimiento(diaFechaVencimiento);
			ciclo.setPrimerFacturaEmiteCualquierDia(primerFacturaEmiteCualquierDia);
			ciclo.setTipoVencimiento((TipoVencimientoCicloFacturacion)this.getView().getValue("tipoVencimiento"));
			ciclo.simularFechasFacturacion(fechaNovedad, true, true);			
			
			this.getView().setValue("fechaEmision1", ciclo.getFechaEmision1());
			this.getView().setValue("fechaVencimiento1", ciclo.getFechaVencimiento1());
			
			this.getView().setValue("fechaEmision2", ciclo.getFechaEmision2());
			this.getView().setValue("fechaVencimiento2", ciclo.getFechaVencimiento2());
		}		
	}
}
