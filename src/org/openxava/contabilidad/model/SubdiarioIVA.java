package org.openxava.contabilidad.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.actions.*;
import org.openxava.base.calculators.*;
import org.openxava.base.model.*;

@Entity

@View(members="subdiario; fechaDesde; fechaHasta; fechaCreacion; usuario")

@Tab(properties="subdiario; fechaDesde; fechaHasta; fechaCreacion; usuario", 
	defaultOrder="${fechaDesde} desc")

public class SubdiarioIVA extends ObjetoNegocio{

	@Required
	private TipoSubdiarioIVA subdiario;
	
	@Required
	@DefaultValueCalculator(FechaInicioMesCalculator.class)
	@OnChange(OnChangeFechaDesdeAction.class)
	private Date fechaDesde;
	
	@ReadOnly
	@DefaultValueCalculator(FechaFinMesCalculator.class)
	private Date fechaHasta;

	public TipoSubdiarioIVA getSubdiario() {
		return subdiario;
	}

	public void setSubdiario(TipoSubdiarioIVA subdiario) {
		this.subdiario = subdiario;
	}

	public Date getFechaDesde() {
		return fechaDesde;
	}

	public void setFechaDesde(Date fechaDesde) {
		this.fechaDesde = fechaDesde;
	}

	public Date getFechaHasta() {
		return fechaHasta;
	}

	public void setFechaHasta(Date fechaHasta) {
		this.fechaHasta = fechaHasta;
	}
}
