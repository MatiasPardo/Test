package org.openxava.negocio.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.calculators.*;
import org.openxava.clasificadores.model.*;
import org.openxava.view.View;

public class ParametrosReporteComprasVentas implements IParametrosReporte{
	
	@DefaultValueCalculator(value=FechaFinMesCalculator.class)
	private Date hasta;
	
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private Marca marca;

	public Date getHasta() {
		return hasta;
	}

	public void setHasta(Date hasta) {
		this.hasta = hasta;
	}

	public Marca getMarca() {
		return marca;
	}

	public void setMarca(Marca marca) {
		this.marca = marca;
	}

	@Override
	public void asignarValoresIniciales(View view, View previousView, Map<?, ?>[] idsSeleccionados) {		
		try {			
			Date hasta = (Date) new FechaFinMesCalculator().calculate();			
			view.setValue("hasta", hasta);
		} catch (Exception e) {
			
		}				
	}
}
