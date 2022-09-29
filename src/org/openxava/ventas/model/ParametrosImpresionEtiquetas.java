package org.openxava.ventas.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.negocio.calculators.*;
import org.openxava.negocio.model.*;
import org.openxava.view.View;

public class ParametrosImpresionEtiquetas implements IParametrosReporte{

	@Required
	private int cantidad = 0;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	private ListaPrecio listaPrecio;
	
	public int getCantidad() {
		return cantidad;
	}

	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}
	
	public ListaPrecio getListaPrecio() {
		return listaPrecio;
	}

	public void setListaPrecio(ListaPrecio listaPrecio) {
		this.listaPrecio = listaPrecio;
	}

	@Override
	public void asignarValoresIniciales(View view, View previousView, Map<?, ?>[] idsSeleccionados) {
		ObjetoPrincipalCalculator calculator = new ObjetoPrincipalCalculator();
		calculator.setEntidad("ListaPrecio");
		try{
			ListaPrecio lista = (ListaPrecio)calculator.calculate();
			if (lista != null){
				Map<String, Object> values  = new HashMap<String, Object>();
				values.put("__MODEL_NAME__", view.getSubview("listaPrecio").getModelName());
				values.put("id", lista.getId());
				view.getSubview("listaPrecio").setValues(values);
			}
		}
		catch(Exception e){			
		}
	}

}
