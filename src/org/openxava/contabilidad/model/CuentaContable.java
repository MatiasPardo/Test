package org.openxava.contabilidad.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.FalseCalculator;
import org.openxava.contabilidad.actions.*;
import org.openxava.contabilidad.calculators.TipoCuentaInflacionCalculator;
import org.openxava.jpa.*;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;

@Entity

@Views({
	@View(members=
			 "imputa, centroCostoObligatorio;" +
			 "codigo, nombre;" +
			 "activo;" + 		 
			 "inflacion"),
	@View(name="Simple",
		members="codigo, nombre"),
	@View(name="Mayor",
		members="codigo, nombre")
})

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

public class CuentaContable extends ObjetoEstatico{
	static final int MAXIMONIVELES = 5;
	
	@Required
	@DefaultValueCalculator(TipoCuentaInflacionCalculator.class)
	private TipoCuentaInflacion inflacion;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean centroCostoObligatorio = Boolean.FALSE;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate
	@ReferenceView("Simple")
	@OnChange(OnChangeImputaCuentaContableAction.class)
	private CuentaContableNoImputable imputa;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@ReadOnly
	private CuentaContableNoImputable nivel1;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@ReadOnly
	private CuentaContableNoImputable nivel2;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@ReadOnly
	private CuentaContableNoImputable nivel3;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@ReadOnly
	private CuentaContableNoImputable nivel4;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@ReadOnly
	private CuentaContableNoImputable nivel5;

	public CuentaContableNoImputable getImputa() {
		return imputa;
	}

	public void setImputa(CuentaContableNoImputable imputa) {
		this.imputa = imputa;
		this.sincronizarNiveles();
	}

	public CuentaContableNoImputable getNivel1() {
		return nivel1;
	}

	public void setNivel1(CuentaContableNoImputable nivel1) {
		this.nivel1 = nivel1;
	}

	public CuentaContableNoImputable getNivel2() {
		return nivel2;
	}

	public void setNivel2(CuentaContableNoImputable nivel2) {
		this.nivel2 = nivel2;
	}

	public CuentaContableNoImputable getNivel3() {
		return nivel3;
	}

	public void setNivel3(CuentaContableNoImputable nivel3) {
		this.nivel3 = nivel3;
	}

	public CuentaContableNoImputable getNivel4() {
		return nivel4;
	}

	public void setNivel4(CuentaContableNoImputable nivel4) {
		this.nivel4 = nivel4;
	}

	public CuentaContableNoImputable getNivel5() {
		return nivel5;
	}

	public void setNivel5(CuentaContableNoImputable nivel5) {
		this.nivel5 = nivel5;
	}

	public void sincronizarNiveles() {
		int cantidadNiveles = 0;
		CuentaContableNoImputable[] niveles = new CuentaContableNoImputable[MAXIMONIVELES]; 
		if (imputa != null){
			CuentaContableNoImputable nivel = imputa;
			niveles[0] = nivel;
			cantidadNiveles = 1;
			for(int i=1; i < MAXIMONIVELES; i++){
				if (nivel.getImputa() != null){
					nivel = nivel.getImputa();
					niveles[i] = nivel;
					cantidadNiveles++;
				}
				else{
					break;
				}
			}
		}
		
		if (cantidadNiveles > 0){
			this.setNivel1(niveles[cantidadNiveles-1]);
			cantidadNiveles--;
		}
		else{
			this.setNivel1(null);
		}
		if (cantidadNiveles > 0){
			this.setNivel2(niveles[cantidadNiveles-1]);
			cantidadNiveles--;
		}
		else{
			this.setNivel2(null);
		}
		if (cantidadNiveles > 0){
			this.setNivel3(niveles[cantidadNiveles-1]);
			cantidadNiveles--;
		}
		else{
			this.setNivel3(null);
		}
		if (cantidadNiveles > 0){
			this.setNivel4(niveles[cantidadNiveles-1]);
			cantidadNiveles--;
		}
		else{
			this.setNivel4(null);
		}
		if (cantidadNiveles > 0){
			this.setNivel5(niveles[cantidadNiveles-1]);
			cantidadNiveles--;
		}
		else{
			this.setNivel5(null);
		}		
	}

	public TipoCuentaInflacion getInflacion() {
		return inflacion;
	}

	public void setInflacion(TipoCuentaInflacion inflacion) {
		this.inflacion = inflacion;
	}

	public BigDecimal saldoInicialFecha(Date fecha){
		BigDecimal saldo = null;
		
		String sql = "select sum(debe - haber) from " + Esquema.concatenarEsquema("itemasiento") + " i " +
					"join " + Esquema.concatenarEsquema("asiento") + " a on a.id = i.asiento_id and a.estado = :confirmado and a.fecha < :fecha " +
					"where i.cuenta_id = :id";
		Query query = XPersistence.getManager().createNativeQuery(sql);
		query.setParameter("confirmado", Estado.Confirmada.ordinal());
		query.setParameter("fecha", fecha);
		query.setParameter("id", this.getId());
		List<?> results = query.getResultList();
		if (!results.isEmpty()){
			saldo = (BigDecimal)results.get(0);
		}
		if (saldo == null) saldo = BigDecimal.ZERO;
		return saldo;
	}
	
	@Override
	public void cambiarEstado() {
		super.cambiarEstado();
		
		if (!this.getActivo()){
			this.validarCuentaInactiva();
		}
	}
	
	private void validarCuentaInactiva(){
		Query query = XPersistence.getManager().createQuery("from Producto where cuentaContableVentas.id = :idVentas or cuentaContableCompras.id = :idCompras");
		query.setParameter("idVentas", this.getId());
		query.setParameter("idCompras", this.getId());
		query.setMaxResults(1);
		if (!query.getResultList().isEmpty()){
			throw new ValidationException("No se puede desactivar: hay productos/conceptos que tiene asociada la cuenta contable");
		}
		
		query = XPersistence.getManager().createQuery("from Cliente where cuentaContableVentas.id = :id");
		query.setParameter("id", this.getId());
		query.setMaxResults(1);
		if (!query.getResultList().isEmpty()){
			throw new ValidationException("No se puede desactivar: hay clientes que tienen asociada la cuenta contable");
		}
		
		query = XPersistence.getManager().createQuery("from Proveedor where cuentaContableCompras.id = :id");
		query.setParameter("id", this.getId());
		query.setMaxResults(1);
		if (!query.getResultList().isEmpty()){
			throw new ValidationException("No se puede desactivar: hay proveedores que tienen asociada la cuenta contable");
		}
		
		query = XPersistence.getManager().createQuery("from CuentaBancaria where cuentaContableFinanzas.id = :id");
		query.setParameter("id", this.getId());
		query.setMaxResults(1);
		if (!query.getResultList().isEmpty()){
			throw new ValidationException("No se puede desactivar: hay cuentas bancarias que tienen asociada la cuenta contable");
		}
		
		query = XPersistence.getManager().createQuery("from TipoValorConfiguracion where cuentaContableFinanzas.id = :id");
		query.setParameter("id", this.getId());
		query.setMaxResults(1);
		if (!query.getResultList().isEmpty()){
			throw new ValidationException("No se puede desactivar: hay tipos de valores que tienen asociada la cuenta contable");
		}
		
		query = XPersistence.getManager().createQuery("from ConceptoTesoreria where cuentaContableFinanzas.id = :id");
		query.setParameter("id", this.getId());
		query.setMaxResults(1);
		if (!query.getResultList().isEmpty()){
			throw new ValidationException("No se puede desactivar: hay conceptos de tesorería que tienen asociada la cuenta contable");
		}
		
		query = XPersistence.getManager().createQuery("from Impuesto where cuentaContableImpuestos.id = :id");
		query.setParameter("id", this.getId());
		query.setMaxResults(1);
		if (!query.getResultList().isEmpty()){
			throw new ValidationException("No se puede desactivar: hay impuestos que tienen asociada la cuenta contable");
		}
				
		Esquema esquema = Esquema.getEsquemaApp();
		if (Is.equal(esquema.getCuentaContableCompras(), this)){
			throw new ValidationException("No se puede desactivar: es la cuenta contable compras por defecto en el esquema");
		}
		
		if (Is.equal(esquema.getCuentaContableVentas(), this)){
			throw new ValidationException("No se puede desactivar: es la cuenta contable ventas por defecto en el esquema");
		}
		
		if (Is.equal(esquema.getCuentaContableComprasProducto(), this)){
			throw new ValidationException("No se puede desactivar: es la cuenta contable compras producto por defecto en el esquema");
		}
		
		if (Is.equal(esquema.getCuentaContableVentasProducto(), this)){
			throw new ValidationException("No se puede desactivar: es la cuenta contable ventas producto por defecto en el esquema");
		}
	}
	
	public Boolean getCentroCostoObligatorio() {
		return this.centroCostoObligatorio == null ? Boolean.FALSE : this.centroCostoObligatorio;
	}

	public void setCentroCostoObligatorio(Boolean centroCostoObligatorio) {
		if (centroCostoObligatorio != null){
			this.centroCostoObligatorio = centroCostoObligatorio;
		}
		else{
			this.centroCostoObligatorio = Boolean.FALSE;
		}
	}
}
