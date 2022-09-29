package org.openxava.ventas.model;

import java.util.*;

import javax.persistence.OrderBy;

import org.openxava.view.*;
import org.openxava.annotations.Condition;
import org.openxava.annotations.ListProperties;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ViewAction;
import org.openxava.clasificadores.model.Marca;
import org.openxava.negocio.model.*;

@org.openxava.annotations.View(
		members="Mes[mes];" + 
				"marcas")

public class ParametrosReporteFacturacionVenta implements IParametrosReporte{
	
	private Date mes;
		
	public Date getMes() {
		return mes;
	}

	public void setMes(Date mes) {
		this.mes = mes;
	}

	@Override
	public void asignarValoresIniciales(View view, View previousView, Map<?, ?>[] idsSeleccionados) {
		view.setValue("mes", new Date());		
	}
	
	@Condition("${activo} = 't'")
	@ListProperties("codigo, nombre")
	@OrderBy("codigo asc")
	@ReadOnly 
	@ViewAction(value="")
	public Collection<Marca> getMarcas(){
		return null;
	}	
}
