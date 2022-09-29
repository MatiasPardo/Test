package org.openxava.base.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.calculators.*;
import org.openxava.jpa.*;
import org.openxava.util.Is;

@Entity

@Views({
	@View(members="entidad;" +
			"nombre, orden;" +
			"origen, destino;" +
			"condicion1, destino1;" +
			"condicion2, destino2;" +
			"soloLectura;" +
			"usuarios;"
				),			
	@View(name="Simple",
		members="nombre")	
})

@Tab(properties="nombre, orden, entidad.entidad, soloLectura, origen.nombre, destino.nombre, condicion1, destino1.nombre, condicion2, destino2.nombre")


public class TransicionEstado extends ObjetoNegocio{

	public static TransicionEstado buscarTransicionDefault(String tipoEntidad, String estadoOrigen, String usuario){
		String sql = "from UsuarioTransicion u where " +
				"u.transicion.entidad.entidad = :entidad and ";
		if (!estadoOrigen.isEmpty()){
			sql += "u.transicion.origen.id = :origen ";
		}
		else {
			sql += "u.transicion.origen.id is null ";
		}	
		sql += "and u.usuarioHabilitado.name = :usuario ";
		sql += "order by u.transicion.orden asc";
		
		Query query = (Query)XPersistence.getManager().createQuery(sql);
		query.setParameter("entidad", tipoEntidad);
		if (!estadoOrigen.isEmpty()){
			query.setParameter("origen", estadoOrigen);
		}
		query.setParameter("usuario", usuario);
		query.setMaxResults(1);
		
		TransicionEstado transicion = null;
		try{
			UsuarioTransicion usuarioTransicion = (UsuarioTransicion) query.getSingleResult();
			transicion = usuarioTransicion.getTransicion();
		}
		catch(NoResultException ex){
		}
		return transicion;
	}
	
	public static boolean tieneTransiciones(String tipoEntidad, EstadoEntidad origen) {
		String id = null;
		if (origen != null) id = origen.getId();
		
		return TransicionEstado.tieneTransiciones(tipoEntidad, id);
	}
	
	public static boolean tieneTransiciones(String tipoEntidad, String idOrigen) {	
		String sql = "from TransicionEstado t where " +
				"t.entidad.entidad = :entidad and ";
		if (!Is.emptyString(idOrigen)){
			sql += "t.origen.id = :origen ";
		}
		else {
			sql += "t.origen.id is null ";
		}	
		
		Query query = (Query)XPersistence.getManager().createQuery(sql);
		query.setParameter("entidad", tipoEntidad);
		if (!Is.emptyString(idOrigen)){
			query.setParameter("origen", idOrigen);
		}
		query.setMaxResults(1);
		
		try{
			query.getSingleResult();
			return true;
		}
		catch(NoResultException ex){
			return false;
		}
	}
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate
	@DescriptionsList(descriptionProperties="entidad")
	private ConfiguracionEntidad entidad;

	@SearchKey
	@Column(length=50) @Required
	private String nombre;
	
	private int orden;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate
	@DescriptionsList(
			depends="entidad",
			condition="${entidad.entidad} = ?" )
	private EstadoEntidad origen;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate
	@DescriptionsList(
			depends="entidad",
			condition="${entidad.entidad} = ?" )
	private EstadoEntidad destino;
	
	private CondicionTransicionar condicion1;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate
	@DescriptionsList(
			depends="entidad",
			condition="${entidad.entidad} = ?" )
	private EstadoEntidad destino1;
	
	private CondicionTransicionar condicion2;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate
	@DescriptionsList(
			depends="entidad",
			condition="${entidad.entidad} = ?" )
	private EstadoEntidad destino2;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean soloLectura = false;
	
	@OneToMany(mappedBy="transicion", cascade=CascadeType.ALL)
	@ListProperties("usuarioHabilitado.name, usuarioHabilitado.active, usuarioHabilitado.givenName, usuarioHabilitado.familyName, usuarioHabilitado.middleName, usuarioHabilitado.nickName")
	private Collection<UsuarioTransicion> usuarios = new ArrayList<UsuarioTransicion>();

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public ConfiguracionEntidad getEntidad() {
		return entidad;
	}

	public void setEntidad(ConfiguracionEntidad entidad) {
		this.entidad = entidad;
	}

	public EstadoEntidad getOrigen() {
		return origen;
	}

	public void setOrigen(EstadoEntidad origen) {
		this.origen = origen;
	}

	public EstadoEntidad getDestino1() {
		return destino1;
	}

	public void setDestino1(EstadoEntidad destino1) {
		this.destino1 = destino1;
	}

	public CondicionTransicionar getCondicion1() {
		return condicion1;
	}

	public void setCondicion1(CondicionTransicionar condicion1) {
		this.condicion1 = condicion1;
	}

	public EstadoEntidad getDestino2() {
		return destino2;
	}

	public void setDestino2(EstadoEntidad destino2) {
		this.destino2 = destino2;
	}

	public Boolean getSoloLectura() {
		return soloLectura;
	}

	public void setSoloLectura(Boolean soloLectura) {
		this.soloLectura = soloLectura;
	}


	public Collection<UsuarioTransicion> getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(Collection<UsuarioTransicion> usuarios) {
		this.usuarios = usuarios;
	}
	
	public EstadoEntidad getDestino() {
		return destino;
	}

	public void setDestino(EstadoEntidad destino) {
		this.destino = destino;
	}

	public CondicionTransicionar getCondicion2() {
		return condicion2;
	}

	public void setCondicion2(CondicionTransicionar condicion2) {
		this.condicion2 = condicion2;
	}

	public int getOrden() {
		return orden;
	}

	public void setOrden(int orden) {
		this.orden = orden;
	}
	
	public void transicionar(ITransicionable objeto){
		EstadoEntidad destino = objeto.ejecutarTransicion(this);
		objeto.setSubestado(destino);
		objeto.setUltimaTransicion(this);
	}
	
	@Override
	public void propiedadesSoloLecturaAlEditar(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLecturaAlEditar(propiedadesSoloLectura, propiedadesEditables, configuracion);
		
		propiedadesSoloLectura.add("entidad");
	}
}
