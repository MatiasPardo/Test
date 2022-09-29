package org.openxava.negocio.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.inventario.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.validators.*;
import org.openxava.util.*;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.Cliente;
import org.openxava.ventas.model.SucursalCliente;

import com.openxava.naviox.model.*;

@Entity

@Views({
	@View(members="codigo;nombre; activo, principal;" + 
			"mail, telefono;" + 
			"domicilio;" + 			
			"usuarios;"),
	@View(name="Simple", members="codigo, nombre;")
})

@EntityValidators({
	@EntityValidator(
		value=PrincipalValidator.class, 
		properties= {
			@PropertyValue(name="idEntidad", from="id"), 
			@PropertyValue(name="modelo", value="Sucursal"),
			@PropertyValue(name="principal")
		}
	)	
})

public class Sucursal extends ObjetoEstatico{

	public static Sucursal sucursalDefault(){
		Sucursal sucursalDefault = null;
		List<Sucursal> sucursales = new LinkedList<Sucursal>();
		Sucursal.buscarSucursalesHabilitadas(sucursales);
		
		if (sucursales.size() == 1){
			sucursalDefault = sucursales.get(0);
		}
		else{
			for(Sucursal s: sucursales){
				if (s.getPrincipal()){
					sucursalDefault = s;
					break;
				}
			}
		}
		return sucursalDefault;
	}
	
	public static void buscarSucursalesHabilitadas(List<Sucursal> sucursales) {
		if (Esquema.getEsquemaApp().getSucursalUnica()){
			Query query = XPersistence.getManager().createQuery("from Sucursal");
			@SuppressWarnings("unchecked")
			List<Sucursal> res = query.getResultList();
			sucursales.addAll(res);
		}
		else{
			String sql = "from Sucursal e, User u where " +
					"u member of e.usuarios and u.name = :user";
			Query query = XPersistence.getManager().createQuery(sql);
			query.setParameter("user", Users.getCurrent());
			try{
				@SuppressWarnings("unchecked")
				Collection<Object[]> resultList = query.getResultList();
				for(Object[] array: resultList){
					sucursales.add((Sucursal)array[0]);
				}
			}
			catch(ElementNotFoundException e){			
			}
		}		
		
	}
	
	private Boolean principal = Boolean.FALSE;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
    @NoSearch
    @ReferenceView("Simple")
	@AsEmbedded
    private Domicilio domicilio;
	
	@Column(length=50)
	@Stereotype("EMAIL")
	@Hidden
	private String mail;
	
	@Column(length=20)
	@Hidden
	private String telefono;
	
	@ManyToMany(fetch=FetchType.LAZY)
	@ListProperties("name, familyName, givenName, jobTitle, middleName, nickName")
	@OrderBy("name asc")
	private Collection<User> usuarios;

	public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}

	public Domicilio getDomicilio() {
		return domicilio;
	}

	public void setDomicilio(Domicilio domicilio) {
		this.domicilio = domicilio;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public Collection<User> getUsuarios() {
		return usuarios;
	}

	public void setUsuarios(Collection<User> usuarios) {
		this.usuarios = usuarios;
	}
	
	public boolean usuarioHabilitado(){
		if (Esquema.getEsquemaApp().getSucursalUnica()){
			return true;
		}
		else{
			boolean habilitado = false;
			List<Sucursal> sucursales = new LinkedList<Sucursal>();
			Sucursal.buscarSucursalesHabilitadas(sucursales);
			for(Sucursal sucursal: sucursales){
				if (sucursal.equals(this)){
					habilitado = true;
					break;
				}
			}
			return habilitado;
		}	
	}
	
	public Deposito depositoPrincipal(){
		Query query = XPersistence.getManager().createQuery("from Deposito where sucursal.id = :id and principal = :principal");
		query.setParameter("id", this.getId());
		query.setParameter("principal", Boolean.TRUE);
		query.setMaxResults(1);
		List<?> results = query.getResultList();
		if (!results.isEmpty()){
			return (Deposito)results.get(0);
		}
		else{
			return null;
		}
	}

	public String getTelefono() {
		return telefono;
	}

	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}

	public Cliente clienteAsociado() {
		Cliente cliente = SucursalCliente.buscarCliente(this);
		if (cliente == null){
			throw new ValidationException("La sucursal no tiene cliente asociado");
		}
		return cliente;
	}
}
