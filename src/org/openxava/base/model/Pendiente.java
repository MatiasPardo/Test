package org.openxava.base.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;

@MappedSuperclass

public abstract class Pendiente extends ObjetoNegocio{
	
	public static final String BASECONDITION = "${anulado} = 'f' and ${cumplido} = 'f'";
	
	public Pendiente(){
	}
	
	public Pendiente(Transaccion origen){
		this.setCumplido(false);
		this.setFechaCumplimiento(null);
		this.setFecha(origen.getFecha());
		this.setTipoTrOrigen(origen.getClass().getSimpleName());
		this.setIdTrOrigen(origen.getId());
		this.setTipoTrDestino(this.tipoEntidadDestino(origen));
		this.setNumero(origen.getNumero());
		this.setEmpresa(origen.getEmpresa());
		this.setSucursal(origen.getSucursal());
	}
	
	public void inicializar(Transaccion origen){
		
	}
	
	@ReadOnly
	private Boolean cumplido = Boolean.FALSE;
	
	public Boolean getEjecutado(){
		Boolean ejecutado = Boolean.FALSE;
		Transaccion tr = this.origen();
		if (tr != null){
			String tipoEntidadDestino = this.tipoEntidadDestino(tr);
			if (!Is.emptyString(tipoEntidadDestino)){
				Collection<Transaccion> transacciones = new LinkedList<Transaccion>();
				tr.getTransaccionesGeneradas(transacciones);
				for(Transaccion trgenerada: transacciones){
					if (trgenerada.getClass().getSimpleName().equals(tipoEntidadDestino)){
						if ((trgenerada.getEstado().equals(Estado.Abierta)) || (trgenerada.getEstado().equals(Estado.Borrador))){
							ejecutado = Boolean.TRUE;
							break;
						}
					}
				}
			}
		}
		return ejecutado;
	}
	
	@ReadOnly
	private Date fecha;
	
	@ReadOnly
	@Stereotype("DATETIME") 
	private Date fechaCumplimiento;
	
	@ReadOnly
	@Stereotype("DATETIME") 
	private Date fechaUltimaActualizacion;
	
	@Hidden
	@Column(length=100)
	@Required
	private String tipoTrDestino;
	
	@Hidden
	@Column(length=100)
	@Required
	private String tipoTrOrigen;
	
	@Hidden
	@Column(length=32)
	@Required
	private String idTrOrigen;
	
	@Column(length=20)  
	@ReadOnly
	private String numero;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@ReadOnly
	private Empresa empresa;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@ReadOnly
	private Sucursal sucursal;
	
	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}
	
	@Version
	@Hidden
	private Integer version;
	
	@ReadOnly
	private Boolean anulado = false;
	
	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Boolean getCumplido() {
		return cumplido == null ? false : this.cumplido;
	}

	public void setCumplido(Boolean cumplido) {
		this.cumplido = cumplido;
	}

	public Date getFechaCumplimiento() {
		return fechaCumplimiento;
	}

	public void setFechaCumplimiento(Date fechaCumplimiento) {
		this.fechaCumplimiento = fechaCumplimiento;
	}

	public String getTipoTrDestino() {
		return tipoTrDestino;
	}

	public void setTipoTrDestino(String tipoTrDestino) {
		this.tipoTrDestino = tipoTrDestino;
	}

	public String getTipoTrOrigen() {
		return tipoTrOrigen;
	}

	public void setTipoTrOrigen(String tipoTrOrigen) {
		this.tipoTrOrigen = tipoTrOrigen;
	}

	public String getIdTrOrigen() {
		return idTrOrigen == null ? new String(): this.idTrOrigen;
	}

	public void setIdTrOrigen(String idTrOrigen) {
		this.idTrOrigen = idTrOrigen;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}
	
	public Date getFechaUltimaActualizacion() {
		return fechaUltimaActualizacion;
	}

	public void setFechaUltimaActualizacion(Date fechaUltimaActualizacion) {
		this.fechaUltimaActualizacion = fechaUltimaActualizacion;
	}

	public Boolean getAnulado() {
		return anulado;
	}

	public void setAnulado(Boolean anulado) {
		this.anulado = anulado;
	}

	@Override
	public Boolean soloLectura(){
		return true;
	}
	
	public abstract Transaccion origen();
	
	public abstract String tipoEntidadDestino(Transaccion origen);
	
	public abstract Transaccion crearTransaccionDestino();

	public void verificarCumplimiento(){
		Boolean cumplido = true;
		List<IItemPendiente> items = new LinkedList<IItemPendiente>();
		this.itemsPendientes(items);
		if (!items.isEmpty()){
			for(IItemPendiente itemPendiente: items){
				if (!itemPendiente.cumplido()){
					cumplido = false;
					break;
				}
			}
			if (cumplido){
				this.cumplir();
			}
		}		
	}
	
	protected void cumplir(){
		this.cumplido = true;
		this.setFechaCumplimiento(new Date());
		this.setFechaUltimaActualizacion(this.getFechaCumplimiento());
	}

	public void anularCumplimiento(){
		if (this.getCumplido()){
			this.cumplido = false;
			this.setFechaCumplimiento(null);
			this.setFechaUltimaActualizacion(new Date());
		}
	}
	
	public static void separarPendientes(List<List<Pendiente>> grupoPendientes, List<Pendiente> pendientes) {
		List<Pendiente> noAgrupados = new LinkedList<Pendiente>();
		noAgrupados.addAll(pendientes);
		while (!noAgrupados.isEmpty()){
			List<Pendiente> grupo = new LinkedList<Pendiente>();
			List<Pendiente> sinGrupo = new LinkedList<Pendiente>();
			Pendiente pendienteGrupo = null;
			for (Pendiente pendiente: noAgrupados){
				if (grupo.isEmpty()){
					grupo.add(pendiente);
					pendienteGrupo = pendiente;
				}
				else{
					if (pendiente.permiteProcesarJunto(pendienteGrupo)){
						grupo.add(pendiente);
					}
					else{
						sinGrupo.add(pendiente);
					}
				}
			}
			grupoPendientes.add(grupo);
			noAgrupados = sinGrupo;
		}
	}

	public boolean permiteProcesarJunto(Pendiente pendiente) {
		boolean procesarJunto = false;
		if ((this.origen() != null) && (pendiente.origen() != null)){
			if (this.origen().getEmpresa().equals(pendiente.origen().getEmpresa())){
				procesarJunto = true;
			}
		}
		return procesarJunto;
	}

	@Override
	public String toString(){
		Transaccion origen = this.origen();
		if (origen != null){
			return origen.toString();
		}
		else{
			return "Pendiente " + this.getFecha().toString();
		}
	}

	
	public void separarItemsPendientes(List<Pendiente> pendientes, List<List<IItemPendiente>> grupoItemsPendientes) {
		// por defecto van todos juntos
		List<IItemPendiente> items = new LinkedList<IItemPendiente>();
		for(Pendiente p: pendientes){
			p.itemsPendientesNoCumplidos(items);
		}
		if (!items.isEmpty()){
			grupoItemsPendientes.add(items);
		}
	}
	
	// Redefinir esté método cuando el pendiente es por Item
	public void itemsPendientes(List<IItemPendiente> items){
	}
	
	public void itemsPendientesNoCumplidos(List<IItemPendiente> items){
		List<IItemPendiente> itemsPendientes = new LinkedList<IItemPendiente>();
		this.itemsPendientes(itemsPendientes);
		for(IItemPendiente item: itemsPendientes){
			if (!item.cumplido()){
				items.add(item);
			}
		}
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}
	
	protected Transaccion buscarOrigen(){
		Query query = XPersistence.getManager().createQuery("from " + this.getTipoTrOrigen() + " where id = :id");
		query.setParameter("id", this.getIdTrOrigen());
		query.setMaxResults(1);
		return (Transaccion)query.getSingleResult();
	}
}