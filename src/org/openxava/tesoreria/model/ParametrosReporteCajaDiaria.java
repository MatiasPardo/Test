package org.openxava.tesoreria.model;

import java.util.Date;
import java.util.Map;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.calculators.FechaFinMesCalculator;
import org.openxava.base.calculators.FechaInicioMesCalculator;
import org.openxava.base.model.Empresa;
import org.openxava.base.model.TipoFormatoImpresion;
import org.openxava.negocio.model.*;


public class ParametrosReporteCajaDiaria implements IParametrosReporte {

	private Date desde;
	
	private Date hasta;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	private Empresa empresa;
	
	@DescriptionsList(descriptionProperties="codigo, nombre")
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private Caja caja;
	
	@Required
	private TipoFormatoImpresion formato;
	
	public Date getDesde() {
		return desde;
	}

	public void setDesde(Date desde) {
		this.desde = desde;
	}

	public Date getHasta() {
		return hasta;
	}

	public void setHasta(Date hasta) {
		this.hasta = hasta;
	}
	
	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}
	
	public Caja getCaja() {
		return this.caja;
	}

	public void setCaja(Caja caja) {
		this.caja = caja;
	}
	
	public TipoFormatoImpresion getFormato() {
		return formato;
	}

	public void setFormato(TipoFormatoImpresion formato) {
		this.formato = formato;
	}

	@Override
	public void asignarValoresIniciales(org.openxava.view.View view, org.openxava.view.View previousView,Map<?, ?>[] idsSeleccionados) {
		Date desde;
		try {
			desde = (Date) new FechaInicioMesCalculator().calculate();
			Date hasta = (Date) new FechaFinMesCalculator().calculate();
			view.setValue("desde", desde);
			view.setValue("hasta", hasta);
		} catch (Exception e) {
		}
		
		try{
			view.setValue("formato", TipoFormatoImpresion.PDF);
		}catch(Exception e){
		}
	}	
}
