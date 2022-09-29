package org.openxava.clasificadores.model;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;

@Entity

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

public class Clasificador extends ObjetoEstatico{
	
	public static final String CONDICION =" and ${activo} = true"; 
	
	public static Clasificador buscar(String codigo, String entidad, Integer numero){
		Query query = XPersistence.getManager().createQuery("from Clasificador where codigo = :codigo and tipoClasificador.modulo = :entidad and tipoClasificador.numero = :numero");
		query.setParameter("codigo", codigo);
		query.setParameter("entidad", entidad);
		query.setParameter("numero", numero);
		query.setMaxResults(1);
		try{
			return (Clasificador)query.getSingleResult();
		}
		catch(NoResultException e){
			return null;
		}
	}
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@Required	
	@ReferenceView("Simple")
	private TipoClasificador tipoClasificador;

	public TipoClasificador getTipoClasificador() {
		return tipoClasificador;
	}

	public void setTipoClasificador(TipoClasificador tipoClasificador) {
		this.tipoClasificador = tipoClasificador;
	}
}
