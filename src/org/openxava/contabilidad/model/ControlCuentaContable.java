package org.openxava.contabilidad.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.openxava.annotations.Hidden;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.Tab;

@Entity

@Table(name="VIEW_CONTROLCUENTACONTABLE")

@Tab(properties="cuentaContable.codigo, cuentaContable.nombre, tipoEntidad, nombreEntidad, tipoCuentaContable")

public class ControlCuentaContable {
	
	// se compone de ID Entidad y el Tipo Cuenta Contable
	@Id
	@Hidden
	@Column(length=65)
	private String id;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView("Simple")
	@ReadOnly
	private CuentaContable cuentaContable;
	
	@Column
	private String tipoEntidad;
		
	@Column
	private String nombreEntidad;
	
	@Column
	private String tipoCuentaContable;
	
	@Column
	@Hidden
	private String nombreTabla;
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public String getTipoEntidad() {
		return tipoEntidad;
	}

	public void setTipoEntidad(String tipoEntidad) {
		this.tipoEntidad = tipoEntidad;
	}

	public String getNombreEntidad() {
		return nombreEntidad;
	}

	public void setNombreEntidad(String nombreEntidad) {
		this.nombreEntidad = nombreEntidad;
	}

	public String getTipoCuentaContable() {
		return tipoCuentaContable;
	}

	public void setTipoCuentaContable(String tipoCuentaContable) {
		this.tipoCuentaContable = tipoCuentaContable;
	}

	public CuentaContable getCuentaContable() {
		return cuentaContable;
	}

	public void setCuentaContable(CuentaContable cuentaContable) {
		this.cuentaContable = cuentaContable;
	}
	
	public String getNombreTabla() {
		return nombreTabla;
	}

	public void setNombreTabla(String nombreTabla) {
		this.nombreTabla = nombreTabla;
	}	
}
