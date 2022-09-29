package org.openxava.negocio.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.validators.*;

@Entity

@Views({
	@View(name="Simple",
	members="codigoPostal, ciudad;" +
			"provincia")
})

@Tab(properties="codigoPostal, codigo, ciudad, provincia.provincia")


public class Ciudad {
	@Id
	@Hidden
	private long codigo;
	
	@SearchKey
	@Column(length=8)
	private String codigoPostal;
	
	@ReadOnly
	@Column(length=100)
	private String ciudad;
	
	@ReadOnly
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoFrame
	@ReferenceView("Simple")
    private Provincia provincia;
	
	public long getCodigo() {
		return codigo;
	}

	public void setCodigo(long codigo) {
		this.codigo = codigo;
	}
	
	public String getCiudad() {
		return ciudad;
	}

	public void setCiudad(String ciudad) {
		this.ciudad = ciudad;
	}

	public Provincia getProvincia() {
		return provincia;
	}

	public void setProvincia(Provincia provincia) {
		this.provincia = provincia;
	}

	public String getCodigoPostal() {
		return codigoPostal;
	}

	public void setCodigoPostal(String codigoPostal) {
		this.codigoPostal = codigoPostal;
	}
	
	public static Ciudad buscarPor(Integer idProvinciaAfip, String ciudad, String codigoPostal){
		Ciudad objetoCiudad = null;
		Query query = null;
		if (idProvinciaAfip == 0){
			// es CAPITAL FEDERAL. Los codigos postales están cargados por nombre de calle
			// dificil obtener la calle de la direccion, se busca el primer elemento que coincida con el codigo postal
			query = XPersistence.getManager().createQuery(" from Ciudad where " +
					"codigoPostal = :codigoPostal and " +
					"provincia.codigoAfip = :provincia");
			query.setParameter("codigoPostal", codigoPostal);
			query.setParameter("provincia", idProvinciaAfip);
		}
		else{
			// En el resto del pais se busca por ciudad
			query = XPersistence.getManager().createQuery(" from Ciudad where " +
					"codigoPostal = :codigoPostal and " +
					"provincia.codigoAfip = :provincia and " + 
					"ciudad LIKE :ciudad");
			query.setParameter("codigoPostal", codigoPostal);
			query.setParameter("provincia", idProvinciaAfip);
			query.setParameter("ciudad", "%" + ciudad + "%");
		}
		query.setMaxResults(1);
		try{
			objetoCiudad = (Ciudad)query.getSingleResult();
	    }
	    catch(javax.persistence.NoResultException noResultEx){
	    	if (idProvinciaAfip != 0){
	    		// En cualquier provincia menos en capital, se intenta buscar por codigo postal, si solo hay una ciudad que coincida, si hay varias, no se devuelve ninguna
	    		query = XPersistence.getManager().createQuery(" from Ciudad where " +
						"codigoPostal = :codigoPostal and " +
						"provincia.codigoAfip = :provincia");
				query.setParameter("codigoPostal", codigoPostal);
				query.setParameter("provincia", idProvinciaAfip);
				try{
					objetoCiudad = (Ciudad)query.getSingleResult();
				}
				catch(javax.persistence.NonUniqueResultException noUniqueEx){
					 
				}
				catch(javax.persistence.NoResultException noResultEx2){
					 
				}
	    	}
	    }
		return objetoCiudad;
	}
	
	public static Ciudad buscarPorCodigoPostal(String codigoPostal) {
		if (Is.emptyString(codigoPostal)){
			throw new ValidationException("Falta asignar el código postal");
		}
		Query query = XPersistence.getManager().createQuery("from Ciudad where codigoPostal = :codigoPostal");
		query.setParameter("codigoPostal", codigoPostal);
		query.setMaxResults(1);
		List<?> res = query.getResultList();
		if (!res.isEmpty()){
			return (Ciudad)res.get(0);
		}
		else{
			return null;
		}
	}
}
