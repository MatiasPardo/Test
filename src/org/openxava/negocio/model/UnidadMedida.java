package org.openxava.negocio.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.jpa.*;
import org.openxava.negocio.validators.*;
import org.openxava.util.*;
import org.openxava.validators.*;


@Entity

@Views({
	@View(name="Simple", members="codigo, nombre")
})

@EntityValidators({
	@EntityValidator(
		value=PrincipalValidator.class, 
		properties= {
			@PropertyValue(name="idEntidad", from="id"), 
			@PropertyValue(name="modelo", value="UnidadMedida"),
			@PropertyValue(name="principal")
		}
	)
	
})

public class UnidadMedida extends ObjetoEstatico{

	public static final String CONDITIONDESCRIPTIONLIST = "(" +
			"${id} = (select p.unidadMedida.id from Producto p where p.id = ?) or " +								  
			"${id} IN (select u.id from Producto q join q.unidadesMedida u where q.id = ?)" 
		+ ")";

	public static final String DEPENDSDESCRIPTIONLIST = "this.producto.id, this.producto.id";
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean principal = Boolean.FALSE;
	
	@DefaultValueCalculator(value=TrueCalculator.class)
	private Boolean utilizaDecimales = Boolean.TRUE;


	@ListProperties("origen.nombre, equivalencia, destino.nombre, descripcion")
	@ReadOnly
	public Collection<EquivalenciaUnidadesMedida> getEquivalencias(){
		if (!Is.emptyString(this.getId())){
			try{
				Collection<EquivalenciaUnidadesMedida> equivalencias = new ArrayList<EquivalenciaUnidadesMedida>();
				
				Query query = XPersistence.getManager().createQuery("from EquivalenciaUnidadesMedida e where e.origen.id = :id");
				query.setParameter("id", this.getId());
				@SuppressWarnings("unchecked")
				List<EquivalenciaUnidadesMedida> resultado = (List<EquivalenciaUnidadesMedida>)query.getResultList();	
				equivalencias.addAll(resultado);			
				
				query = XPersistence.getManager().createQuery("from EquivalenciaUnidadesMedida e where e.destino.id = :id");
				query.setParameter("id", this.getId());
				@SuppressWarnings("unchecked")
				List<EquivalenciaUnidadesMedida> resultado2 = (List<EquivalenciaUnidadesMedida>)query.getResultList();
				equivalencias.addAll(resultado2);
				
				return equivalencias;
			}
			catch(Exception e){
				return Collections.emptyList();
			}
		}
		else{
			return Collections.emptyList();
		}
	}
		
	public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}

	public Boolean getUtilizaDecimales() {
		return utilizaDecimales;
	}

	public void setUtilizaDecimales(Boolean utilizaDecimales) {
		this.utilizaDecimales = utilizaDecimales;
	}

	public BigDecimal convertir(BigDecimal cantidad, UnidadMedida unidadMedida) {
		BigDecimal cantidadConvertida = BigDecimal.ZERO;
		if (unidadMedida == null){
			throw new ValidationException("No se puede convertir unidades de medida: Parámetro UnidadMedida vacío");
		}
		else if (!this.equals(unidadMedida)){
			String sql = "from EquivalenciaUnidadesMedida e where " + 
						"(e.origen = :unidad1 and e.destino = :unidad2) or (e.origen = :unidad2 and e.destino = :unidad1)"; 
			Query query = XPersistence.getManager().createQuery(sql);
			query.setParameter("unidad1", this);
			query.setParameter("unidad2", unidadMedida);
			query.setMaxResults(1);
			try{
				EquivalenciaUnidadesMedida equivalencia = (EquivalenciaUnidadesMedida)query.getSingleResult();
				BigDecimal valorEquivalencia = equivalencia.getEquivalencia();
				if (equivalencia.getOrigen().equals(unidadMedida)){
					valorEquivalencia = new BigDecimal(1).divide(valorEquivalencia, 16, RoundingMode.HALF_EVEN);					
				}
				cantidadConvertida = cantidad.multiply(valorEquivalencia);
			}
			catch(NoResultException e){
				
			}
		}
		else{
			cantidadConvertida = cantidad;
		}
		
		if (!unidadMedida.utilizaDecimales){
			cantidadConvertida = cantidadConvertida.setScale(0, RoundingMode.UP);
		}
		return cantidadConvertida;
	}
	
	@Override
	public Boolean soloLectura(){
		Boolean soloLectura = super.soloLectura();
		if (!soloLectura){
			if (!Is.emptyString(this.getId())){
				soloLectura = true;
			}
		}
		return soloLectura;
	}
}
