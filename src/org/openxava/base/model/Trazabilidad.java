package org.openxava.base.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.validators.*;

@Entity

@Tabs({
	@Tab(
		properties="fechaCreacion, comprobanteOrigen, comprobanteDestino",
		defaultOrder="${fechaCreacion} desc")
})

public class Trazabilidad {
	
	public static Trazabilidad crearTrazabilidad(Transaccion origen, String tipoOrigen, Transaccion destino, String tipoDestino){
		Trazabilidad trazabilidad = new Trazabilidad();
		if (Is.emptyString(origen.getId())){
			throw new ValidationException("Error al crear trazabilidad: comprobante origen no tiene id asignado");
		}
		if (Is.emptyString(destino.getId())){
			throw new ValidationException("Error al crear trazabilidad: comprobante destino no tiene id asignado");
		}
		
		trazabilidad.setIdOrigen(origen.getId());
		trazabilidad.setIdDestino(destino.getId());
		if (!Is.emptyString(tipoOrigen)){
			trazabilidad.setTipoTrOrigen(tipoOrigen);
		}
		else{
			trazabilidad.setTipoTrOrigen(origen.getClass().getSimpleName());
		}
		if (!Is.emptyString(tipoDestino)){
			trazabilidad.setTipoTrDestino(tipoDestino);
		}
		else{
			trazabilidad.setTipoTrDestino(destino.getClass().getSimpleName());
		}
		trazabilidad.setComprobanteOrigen(origen.toString());
		trazabilidad.setComprobanteDestino(destino.toString());
		XPersistence.getManager().persist(trazabilidad);		
		return trazabilidad;
	}
	
	@SuppressWarnings("unchecked")
	public static void buscarTrazabilidadPorTipo(Collection<Trazabilidad> trazabilidad, ObjetoNegocio obj, boolean objOrigen, String tipo){
		String sql = "from Trazabilidad where ";
		if (objOrigen){
			sql += "idOrigen = :id and tipoTrDestino = :tipo";
		}
		else{
			sql += "idDestino = :id and tipoTrOrigen = :tipo";
		}
		Query query = XPersistence.getManager().createQuery(sql);
		query.setParameter("id", obj.getId());
		query.setParameter("tipo", tipo);
		List<?> list = query.getResultList();
		if (!list.isEmpty()){
			trazabilidad.addAll((Collection<? extends Trazabilidad>) list);
		}
	}
	
	@Id @Hidden
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(length=32)
	@ReadOnly
	@Hidden
	@Required
	private String idOrigen;
	
	@Column(length=32)
	@ReadOnly
	@Hidden
	@Required
	private String idDestino;
	
	@Column(length=100)
	@Hidden
	@ReadOnly	
	@Required
	private String tipoTrOrigen;
	
	@Column(length=100)
	@Hidden
	@ReadOnly	
	@Required
	private String tipoTrDestino;
	
	@ReadOnly
	@Stereotype("DATETIME")
	private Date fechaCreacion;
	
	@Column(length=50)
	@ReadOnly
	private String usuario;
	
	@Column(length=100)
	@ReadOnly
	private String comprobanteOrigen;
	
	@Column(length=100)
	@ReadOnly
	private String comprobanteDestino;

	public String getIdOrigen() {
		return idOrigen;
	}

	public void setIdOrigen(String idOrigen) {
		this.idOrigen = idOrigen;
	}

	public String getIdDestino() {
		return idDestino;
	}

	public void setIdDestino(String idDestino) {
		this.idDestino = idDestino;
	}

	public String getTipoTrOrigen() {
		return tipoTrOrigen;
	}

	public void setTipoTrOrigen(String tipoTrOrigen) {
		this.tipoTrOrigen = tipoTrOrigen;
	}

	public String getTipoTrDestino() {
		return tipoTrDestino;
	}

	public void setTipoTrDestino(String tipoTrDestino) {
		this.tipoTrDestino = tipoTrDestino;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public String getComprobanteOrigen() {
		return comprobanteOrigen;
	}

	public void setComprobanteOrigen(String comprobanteOrigen) {
		this.comprobanteOrigen = comprobanteOrigen;
	}

	public String getComprobanteDestino() {
		return comprobanteDestino;
	}

	public void setComprobanteDestino(String comprobanteDestino) {
		this.comprobanteDestino = comprobanteDestino;
	}
	
	@Transient
	private Transaccion origen;
	
	@Transient
	private Transaccion destino;
	
	public Transaccion trOrigen(){
		if (this.origen == null){
			Query query = XPersistence.getManager().createQuery("from " + this.getTipoTrOrigen() + " where id = :id");
			query.setParameter("id", this.getIdOrigen());
			this.origen = (Transaccion)query.getSingleResult();			
		}
		return this.origen;
	}
	
	public Transaccion trDestino(){
		if (this.destino == null){
			Query query = XPersistence.getManager().createQuery("from " + this.getTipoTrDestino() + " where id = :id");
			query.setParameter("id", this.getIdDestino());
			this.destino = (Transaccion)query.getSingleResult();
		}
		return this.destino;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	@PrePersist
	protected void onPrePersist() {
		this.setFechaCreacion(new Date());
		this.setUsuario(Users.getCurrent());
	}
	
	public static void buscarTrazabilidadCompleta(Collection<Trazabilidad> trazabilidadCompleta, ObjetoNegocio obj){
		
		Map<String, Object> procesados = new HashMap<String, Object>();
		trazabilidadOrigen(obj.getId(), trazabilidadCompleta, procesados);
		procesados.clear();
		trazabilidadDestino(obj.getId(), trazabilidadCompleta, procesados);		
	}
	
	private static void trazabilidadOrigen(String id, Collection<Trazabilidad> lista, Map<String, Object> procesados){
		if (!procesados.containsKey(id)){
			procesados.put(id, null);
			Query query = XPersistence.getManager().createQuery("from Trazabilidad where idDestino = :id");
			query.setParameter("id", id);
			List<?> list = query.getResultList();
			for(Object res: list){
				Trazabilidad origen = (Trazabilidad)res;
				lista.add(origen);
				trazabilidadOrigen(origen.getIdOrigen(), lista, procesados);
			}
		}
	}
	
	private static void trazabilidadDestino(String id, Collection<Trazabilidad> lista, Map<String, Object> procesados){
		if (!procesados.containsKey(id)){
			procesados.put(id, null);
			Query query = XPersistence.getManager().createQuery("from Trazabilidad where idOrigen = :id");
			query.setParameter("id", id);
			List<?> list = query.getResultList();
			for(Object res: list){
				Trazabilidad destino = (Trazabilidad)res;
				lista.add(destino);
				trazabilidadDestino(destino.getIdDestino(), lista, procesados);
			}
		}
	}
}
