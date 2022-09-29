package org.openxava.conciliacionbancaria.model;

import java.util.Map;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.compras.model.Proveedor;
import org.openxava.negocio.model.IParametrosReporte;
import org.openxava.view.View;

public class ParametrosGenerarPagoExtractoBancario implements IParametrosReporte{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre, numeroDocumento", 
				condition="${activo} = 't'")
	@NoCreate @NoModify
	private Proveedor proveedor;

	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}
	
	@Override
	public void asignarValoresIniciales(View view, View previousView, Map<?, ?>[] idsSeleccionados) {		
	}
}
