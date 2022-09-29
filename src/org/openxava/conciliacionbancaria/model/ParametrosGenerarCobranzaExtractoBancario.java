package org.openxava.conciliacionbancaria.model;

import java.util.Map;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.negocio.model.IParametrosReporte;
import org.openxava.ventas.model.Cliente;
import org.openxava.view.View;

public class ParametrosGenerarCobranzaExtractoBancario implements IParametrosReporte{

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre, numeroDocumento", 
				condition="${activo} = 't'")
	@NoCreate @NoModify
	private Cliente cliente;
		
	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	@Override
	public void asignarValoresIniciales(View view, View previousView, Map<?, ?>[] idsSeleccionados) {		
	}

}
