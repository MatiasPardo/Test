package org.openxava.proyectos.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.Required;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.base.filter.EmpresaFilter;
import org.openxava.base.model.Transaccion;
import org.openxava.clasificadores.model.Clasificador;

@Entity

@Views({
	@View(members=
			"Principal{" +
				"fecha, fechaCreacion, usuario;" +
				"estado, subestado, ultimaTransicion;" + 
				"empresa, numero, moneda;" +
				"nombre;" +			
				"observaciones;" +
			"}" +
			"Clasificadores{proyectoClasificador1; proyectoClasificador2; proyectoClasificador3}" 	
		),
	@View(name="Simple", members="numero, nombre"),
})

@Tab(
	filter=EmpresaFilter.class,
	properties="empresa.nombre, fecha, numero, estado, nombre, subestado.nombre",
	baseCondition=EmpresaFilter.BASECONDITION,
	defaultOrder="${fechaCreacion} desc")

public class Proyecto extends Transaccion{

	@Column(length=100) @Required
    private String nombre;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", 
			condition="${tipoClasificador.numero} = 1 and ${tipoClasificador.modulo} = 'Proyecto'" + Clasificador.CONDICION)
	private Clasificador proyectoClasificador1;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", 
			condition="${tipoClasificador.numero} = 2 and ${tipoClasificador.modulo} = 'Proyecto'" + Clasificador.CONDICION)
	private Clasificador proyectoClasificador2;
	
	@NoCreate @NoModify
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre", 
		condition="${tipoClasificador.numero} = 3 and ${tipoClasificador.modulo} = 'Proyecto'" + Clasificador.CONDICION)
	private Clasificador proyectoClasificador3;
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Proyecto";
	}
	
	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Clasificador getProyectoClasificador1() {
		return proyectoClasificador1;
	}

	public void setProyectoClasificador1(Clasificador proyectoClasificador1) {
		this.proyectoClasificador1 = proyectoClasificador1;
	}

	public Clasificador getProyectoClasificador2() {
		return proyectoClasificador2;
	}

	public void setProyectoClasificador2(Clasificador proyectoClasificador2) {
		this.proyectoClasificador2 = proyectoClasificador2;
	}

	public Clasificador getProyectoClasificador3() {
		return proyectoClasificador3;
	}

	public void setProyectoClasificador3(Clasificador proyectoClasificador3) {
		this.proyectoClasificador3 = proyectoClasificador3;
	}
}
