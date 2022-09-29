package org.openxava.reclamos.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.base.validators.*;
import org.openxava.clasificadores.model.*;
import org.openxava.jpa.*;

@Views({
	@View(members="codigo, estado;" + 
			"nombre;" + 
			"marca, modelo;" +
			"nroChasis, nroMotor, patente;" +
			"año, fechaVencimientoVTV;" + 
			"mantenimiento;"),
	@View(name="Simple", members="codigo, nombre;"),
	@View(name="Reclamo", 
		members="codigo, estado;" +	
				"nombre;" +
				"mantenimiento;")
})



@Entity
public class Vehiculo extends ObjetoEstatico{
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@DescriptionsList(descriptionProperties="nombre")
	private Marca marca;
	
	private String modelo;
	
	@PropertyValidator(AnioActualValidator.class)
	@Required
	private int año; 

	@Column(length=25)
	private String nroChasis;
	
	@Column(length=25)
	private String nroMotor;
	
	@Column(length=15)
	private String patente;
	
	private Date fechaVencimientoVTV;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly
	@DescriptionsList(descriptionProperties="nombre", 
					condition="${entidad.entidad} = 'Vehiculo'")
	private EstadoEntidad estado;
	
	@OneToMany(mappedBy="vehiculo")
	@ReadOnly
	@ListProperties("fechaCreacion, usuario, fecha, estado, tipo.nombre, kilometraje")
	@OrderBy("fechaCreacion desc")
	@Condition("${estado} != 2 AND ${estado} != 5 AND ${vehiculo.id} = ${this.id}")
	private Collection<MantenimientoVehiculo> mantenimiento;
	
	public String getNroChasis() {
		return nroChasis;
	}

	public void setNroChasis(String nroChasis) {
		this.nroChasis = nroChasis;
	}

	public String getNroMotor() {
		return nroMotor;
	}

	public void setNroMotor(String nroMotor) {
		this.nroMotor = nroMotor;
	}

	public Marca getMarca() {
		return marca;
	}

	public void setMarca(Marca marca) {
		this.marca = marca;
	}

	public String getModelo() {
		return modelo;
	}

	public void setModelo(String modelo) {
		this.modelo = modelo;
	}

	public int getAño() {
		return año;
	}

	public void setAño(int año) {
		this.año = año;
	}

	public String getPatente() {
		return patente;
	}

	public void setPatente(String patente) {
		this.patente = patente;
	}

	public Date getFechaVencimientoVTV() {
		return fechaVencimientoVTV;
	}

	public void setFechaVencimientoVTV(Date fechaVencimientoVTV) {
		this.fechaVencimientoVTV = fechaVencimientoVTV;
	}

	public EstadoEntidad getEstado() {
		return estado;
	}

	public void setEstado(EstadoEntidad estado) {
		this.estado = estado;
	}

	public Collection<MantenimientoVehiculo> getMantenimiento() {
		return mantenimiento;
	}

	public void setMantenimiento(Collection<MantenimientoVehiculo> mantenimiento) {
		this.mantenimiento = mantenimiento;
	}

	public Integer ultimoKilometrajeRegistrado() {
		Integer kilometraje = 0;
		String sql = "select kilometraje from {h-schema}MantenimientoVehiculo where vehiculo_id = :id and estado = :confirmado order by fechaCreacion desc limit 1";
		Query query = XPersistence.getManager().createNativeQuery(sql);
		query.setParameter("confirmado", Estado.Confirmada.ordinal());
		query.setParameter("id", this.getId());
				
		List<?> result = query.getResultList();
		if (!result.isEmpty()){
			kilometraje = (Integer)result.get(0);
		}
		return kilometraje;
	}
}

