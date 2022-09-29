package org.openxava.negocio.model;

import java.util.*;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.openxava.annotations.*;
import org.openxava.base.calculators.*;
import org.openxava.base.model.Empresa;
import org.openxava.base.model.TipoFormatoImpresion;
import org.openxava.base.model.UtilERP;

@Views({
	@View(members="desde;hasta"),
	@View(name="SoloFecha", members="fecha"),
	@View(name="SoloFechaSucursal", members="sucursal; fecha"),
	@View(name="RangoFechaConFormato", members="desde;hasta;formato"),
})

public class ParametrosRangoFecha implements IParametrosReporte {

	private Date desde;
	
	private Date hasta;
	
	@Required
	private TipoFormatoImpresion formato;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	private Empresa empresa;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	private Sucursal sucursal;
	
	private Date fecha;
	
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

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}
	
	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public TipoFormatoImpresion getFormato() {
		return formato;
	}

	public void setFormato(TipoFormatoImpresion formato) {
		this.formato = formato;
	}
	
	@Override
	public void asignarValoresIniciales(org.openxava.view.View view, org.openxava.view.View previousView, Map<?, ?>[] idsSeleccionados) {
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
		
		try{
			view.setValue("fecha", UtilERP.trucarDateTime(new Date()));
		}
		catch (Exception e) {
			
		}
		
		try{
			Sucursal sucursal = Sucursal.sucursalDefault();
			if (sucursal != null){
				view.setValue("sucursal.id", sucursal.getId());
			}
		}
		catch(Exception e){
			
		}
	}
}
