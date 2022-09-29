package org.openxava.contabilidad.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.EmpresaExterna;
import org.openxava.negocio.model.*;

@Entity

@Table(name="VIEW_ITEMMAYORCONTABLE")

@View(members="mayor;" + 
		 	"ItemAsiento[" + 
			"numero, fecha, fechaCreacion;" + 
			"detalle;" +
			"observaciones;" + 
			"debe, haber, saldo];" + 
			"asiento;")

public class ItemMayorContable implements IGeneradoPor {

	@Id
	@Hidden
	@Column(length=32)
	private String id;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@ReadOnly
	@ReferenceView("Simple")
	private MayorContable mayor; 
	
	@ReadOnly
	private String numero;
	
	@ReadOnly
	private Date fecha;
	
	@ReadOnly
	@Stereotype("DATETIME")
	private Date fechaCreacion;
	
	@ReadOnly
	private String detalle;
	
	@ReadOnly
	private String observaciones;
	
	@ReadOnly
	private BigDecimal debe;
	
	@ReadOnly
	private BigDecimal haber;
	
	@ReadOnly
	private BigDecimal saldo;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@ReadOnly
	@ReferenceView("Mayor")
	private ItemAsiento asiento;

	@ReadOnly
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@NoCreate @NoModify
	@ReferenceView("Simple")
	private EmpresaExterna originante;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public MayorContable getMayor() {
		return mayor;
	}

	public void setMayor(MayorContable mayor) {
		this.mayor = mayor;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public String getDetalle() {
		return detalle;
	}

	public void setDetalle(String detalle) {
		this.detalle = detalle;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}

	public BigDecimal getDebe() {
		return debe;
	}

	public void setDebe(BigDecimal debe) {
		this.debe = debe;
	}

	public BigDecimal getHaber() {
		return haber;
	}

	public void setHaber(BigDecimal haber) {
		this.haber = haber;
	}

	public BigDecimal getSaldo() {
		return saldo;
	}

	public void setSaldo(BigDecimal saldo) {
		this.saldo = saldo;
	}

	public ItemAsiento getAsiento() {
		return asiento;
	}

	public void setAsiento(ItemAsiento asiento) {
		this.asiento = asiento;
	}

	@Override
	public String generadaPorId() {
		return this.getAsiento().getAsiento().getId();
	}

	@Override
	public String generadaPorTipoEntidad() {
		return "Asiento";
	}

	public EmpresaExterna getOriginante() {
		return originante;
	}

	public void setOriginante(EmpresaExterna originante) {
		this.originante = originante;
	}
}
