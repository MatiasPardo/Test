package org.openxava.contabilidad.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.negocio.model.*;
import org.openxava.view.View;

@Views({
	@org.openxava.annotations.View(members="empresa"),
	@org.openxava.annotations.View(name="TipoFormato", 
				members="empresa; formato"),
	@org.openxava.annotations.View(name="TipoFecha", members="empresa; desde; hasta"),
})

public class ParametrosReporteImpresionContable implements IParametrosReporte{

	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private Empresa empresa;
	
	private TipoFormatoImpresion formato;
	
	private Date desde;
	
	private Date hasta;
	
	@Override
	public void asignarValoresIniciales(View view, View previousView, Map<?, ?>[] idsSeleccionados) {
		Empresa empresa = Empresa.buscarEmpresaPorNro(1);
		Map<String, Object> values = new HashMap<String, Object>();
		values.put("id", empresa.getId());
		view.setValue("empresa", values);
		
		try{
			view.setValue("formato", TipoFormatoImpresion.PDF);
		}
		catch(Exception e){			
		}
		
		try{
			view.setValue("desde", previousView.getValue("fechaDesde"));
			view.setValue("hasta", previousView.getValue("fechaHasta"));
		}
		catch(Exception e){
			
		}
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public TipoFormatoImpresion getFormato() {
		return formato;
	}

	public void setFormato(TipoFormatoImpresion formato) {
		this.formato = formato;
	}

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
}
