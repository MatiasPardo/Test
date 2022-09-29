package org.openxava.conciliacionbancaria.model;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.jpa.XPersistence;
import org.openxava.tesoreria.model.CuentaBancaria;
import org.openxava.tesoreria.model.MovimientoValores;
import org.openxava.util.Users;
import org.openxava.validators.ValidationException;

@Entity

@View(members="id, usuario, fechaCreacion, anulado;" +
		"movimientos;" +
		"extracto;")

public class GrupoConciliacion {
	
	@Id 
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@ReadOnly
	private Long id;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@Column(length=50)
	@ReadOnly
	@DisplaySize(30)
	private String usuario = new String("");
	
	public String getUsuario() {
		return usuario;
	}
	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}
	
	@ReadOnly
	@Stereotype("DATETIME")
	private Date fechaCreacion;
	
	public Date getFechaCreacion() {
		return fechaCreacion;
	}
	
	public void setFechaCreacion(Date fechaCreacion) {
		if (fechaCreacion != null){
			this.fechaCreacion = fechaCreacion;
		}
	}
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@NoModify @NoCreate
	@DescriptionsList(descriptionProperties="nombre")
	private CuentaBancaria cuenta;
	
	public CuentaBancaria getCuenta() {
		return cuenta;
	}
	public void setCuenta(CuentaBancaria cuenta) {
		this.cuenta = cuenta;
	}
	
	@ReadOnly
	@ListProperties("fechaComprobante, tipoComprobante, numeroComprobante, importeOriginal, detalle")
	@CollectionView("Simple") 
	@Condition("${this.id} = ${conciliadoCon} and ${tipoConciliacion} = 4")
	public Collection<MovimientoValores> getMovimientos(){
		return null;
	}
	
	@ReadOnly
	@ListProperties("fecha, concepto, importe, observaciones")
	@CollectionView("Simple") 
	@Condition("${this.id} = ${conciliadoCon} and ${tipoConciliacion} = 4")
	public Collection<ExtractoBancario> getExtracto(){
		return null;
	}
	
	@ReadOnly
	private Boolean anulado = Boolean.FALSE;
		
	public Boolean getAnulado() {
		return anulado;
	}
	public void setAnulado(Boolean anulado) {
		this.anulado = anulado;
	}
	
	@PrePersist
	protected void onPrePersist() {
		this.setUsuario(Users.getCurrent());
		this.setFechaCreacion(new java.util.Date());
	}
	
	public void anularConciliacion(){
		if (!this.getAnulado()){
			Query query = XPersistence.getManager().createQuery("from MovimientoValores where conciliadoCon = :id and tipoConciliacion = :tipo");
			query.setParameter("id", this.getId());
			query.setParameter("tipo", TipoConciliacionBancaria.Grupo);		
			List<?> result = query.getResultList();
			for(Object res: result){
				ExtractoBancario.anularConciliacionGrupo(((IObjetoConciliable)res));
			}
			
			query = XPersistence.getManager().createQuery("from ExtractoBancario where conciliadoCon = :id and tipoConciliacion = :tipo");
			query.setParameter("id", this.getId());	
			query.setParameter("tipo", TipoConciliacionBancaria.Grupo);
			result = query.getResultList();
			for(Object res: result){
				ExtractoBancario.anularConciliacionGrupo(((IObjetoConciliable)res));
			}
			this.setAnulado(true);
		}
		else{
			throw new ValidationException("Ya esta anulado el grupo " + this.getId());
		}
	}
	
}
