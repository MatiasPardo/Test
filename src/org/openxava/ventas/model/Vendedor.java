package org.openxava.ventas.model;

import java.math.*;
import java.util.List;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.base.validators.*;
import org.openxava.calculators.*;
import org.openxava.compras.model.Proveedor;
import org.openxava.cuentacorriente.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.validators.*;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;

import com.openxava.naviox.model.*;

@Entity

@Views({
	@View(members="codigo, activo; nombre; usuarioSistema, gerencia;" +
			"Comisiones[codigoProveedorCtaCte; porcentajeComisiones;]"
			),
	@View(name="Simple",
		members="codigo, nombre")	
})

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

@EntityValidators({
	@EntityValidator(
			value=UnicidadValidator.class, 
			properties= {
				@PropertyValue(name="id"), 
				@PropertyValue(name="atributo", value="codigo"),
				@PropertyValue(name="valor", from="codigo"),
				@PropertyValue(name="modelo", value="Vendedor"),
				@PropertyValue(name="idMessage", value="codigo_repetido")
				
			}
	),
	@EntityValidator(
			value=UsuarioAsociacionUnicaValidator.class, 
			properties= {
				@PropertyValue(name="id"), 
				@PropertyValue(name="atributoUsuario", value="usuarioSistema"),
				@PropertyValue(name="modelo", value="Vendedor"),
				@PropertyValue(name="usuario", from="usuarioSistema")								
			}
	),
})


public class Vendedor extends ObjetoEstatico implements IResponsableCuentaCorriente{

	public static Vendedor buscarVendedorUsuario(String usuario){
		Query query = XPersistence.getManager().createQuery("from Vendedor where usuarioSistema.name = :usuario");
		query.setParameter("usuario", usuario);
		query.setMaxResults(1);
		try{
			Vendedor vendedor = (Vendedor)query.getSingleResult();
			return vendedor;
		}
		catch(Exception e){
			return null;
		}
	}
	
	@OneToOne(fetch=FetchType.LAZY, optional=true, orphanRemoval=false)
	@DescriptionsList(descriptionProperties="name")
	@NoCreate @NoModify	
	private User usuarioSistema;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean gerencia = Boolean.FALSE;

	@Column(length=20)
	private String codigoProveedorCtaCte;
	
	@Min(value=0, message="No puede menor a 0")
	@Max(value=100, message="No puede ser mayor a 100")
	@DefaultValueCalculator(value=ZeroBigDecimalCalculator.class)
	private BigDecimal porcentajeComisiones = BigDecimal.ZERO;
	
	public User getUsuarioSistema() {
		return usuarioSistema;
	}

	public void setUsuarioSistema(User usuarioSistema) {
		this.usuarioSistema = usuarioSistema;
	}

	public Boolean getGerencia() {
		return gerencia;
	}

	public void setGerencia(Boolean gerencia) {
		this.gerencia = gerencia;
	}
		
	public String getCodigoProveedorCtaCte() {
		return codigoProveedorCtaCte;
	}

	public void setCodigoProveedorCtaCte(String codigoProveedorCtaCte) {
		this.codigoProveedorCtaCte = codigoProveedorCtaCte;
	}

	public BigDecimal getPorcentajeComisiones() {
		return porcentajeComisiones == null ? BigDecimal.ZERO : this.porcentajeComisiones;
	}

	public void setPorcentajeComisiones(BigDecimal porcentajeComisiones) {
		this.porcentajeComisiones = porcentajeComisiones;
	}
	
	public Proveedor buscarProveedorCtaCte(){
		if (!Is.emptyString(this.getCodigoProveedorCtaCte())){
			Query query = XPersistence.getManager().createQuery("from Proveedor where codigo = :codigo");
			query.setParameter("codigo", this.getCodigoProveedorCtaCte());
			query.setFlushMode(FlushModeType.COMMIT);
			query.setMaxResults(1);
			List<?> result = query.getResultList();
			if (!result.isEmpty()){
				return (Proveedor)result.get(0);
			}
			else{
				throw new ValidationException("El vendedor " + this.getNombre() + " tiene un código de proveedor inexistente: " + this.getCodigoProveedorCtaCte());
			}
		}
		else{
			return null;
		}
	}
}
