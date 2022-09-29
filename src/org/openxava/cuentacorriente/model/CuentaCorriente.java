package org.openxava.cuentacorriente.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;


@Entity

@Views({
	@View(members="empresa, clasificador, fechaProbable;" + 
				"fecha, numero, tipo;" +
				"monedaOriginal, importeOriginal, saldoOriginal;" + 
				"saldo1, saldo2, pendiente"),
	@View(name="Simple", 
		members="empresa, clasificador, fechaProbable;" + 
				"fecha, numero, tipo;" +
				"monedaOriginal, importeOriginal, saldoOriginal;" + 
				"saldo1, saldo2, pendiente"), 
	@View(name="CambioClasificador", members="clasificador"),
	@View(name="CambioFechaProbable", members="fechaProbable")	
})

@Tab(
		properties="empresa.nombre, fecha, numero, tipo, anulado, ingreso, egreso, importe1, saldo1, cotizacion2, importe2, saldo2, monedaOriginal.nombre, importeOriginal, saldoOriginal, cotizacion, pendiente, diferenciaCambio",
		filter=EmpresaFilter.class,
		baseCondition=EmpresaFilter.BASECONDITION,
		defaultOrder="${fechaCreacion} desc")

public class CuentaCorriente extends ObjetoNegocio implements IGeneradoPor{
	
	public static CuentaCorriente crearCuentaCorriente(ITransaccionCtaCte transaccion){
		if (transaccion.generaCtaCte()){
			CuentaCorriente ctacte = transaccion.CtaCteNuevaCuentaCorriente();		
			Transaccion tr = transaccion.CtaCteTransaccion();
			
			ctacte.setOperadorCtaCte(transaccion.CtaCteOperadorComercial());
			IResponsableCuentaCorriente responsable = transaccion.CtaCteResponsable();
			if (responsable != null){
				ctacte.setResponsableCtaCte(responsable);
			}
			ctacte.setFecha(transaccion.CtaCteFecha());
			ctacte.setFechaVencimiento(transaccion.CtaCteFechaVencimiento());
			ctacte.setTipo(transaccion.CtaCteTipo());
			ctacte.setNumero(transaccion.CtaCteNumero());
			ctacte.setMonedaOriginal(tr.getMoneda());
			Integer coeficiente = transaccion.CtaCteCoeficiente();
			ctacte.setImporteOriginal(transaccion.CtaCteImporte());
			if (coeficiente > 0){
				if (ctacte.getImporteOriginal().compareTo(BigDecimal.ZERO) < 0){
					throw new ValidationException("Cuenta corriente: El importe no puede ser negativo");
				}
			}
			else if (coeficiente < 0){
				if (ctacte.getImporteOriginal().compareTo(BigDecimal.ZERO) > 0){
					throw new ValidationException("Cuenta corriente: El importe no puede ser negativo");
				}
			}
			ctacte.setNetoOriginal(transaccion.CtaCteNeto());
			ctacte.setSaldoOriginal(transaccion.CtaCteImporte());
			ctacte.setCotizacion(tr.getCotizacion());
			ctacte.setEmpresa(tr.getEmpresa());
			ctacte.setMoneda1(tr.getMoneda1());
			ctacte.setMoneda2(tr.getMoneda2());
			ctacte.setCotizacion2(tr.getCotizacion2());

			if (ctacte.getMonedaOriginal().equals(ctacte.getMoneda1())){
				ctacte.setImporte1(transaccion.CtaCteImporte());
				ctacte.setNeto1(transaccion.CtaCteNeto());				
			}
			else{
				ctacte.setImporte1(tr.calcularImporte1(transaccion.CtaCteImporte()));
				ctacte.setNeto1(tr.calcularImporte1(transaccion.CtaCteNeto()));
			}
			
			if (ctacte.getMonedaOriginal().equals(ctacte.getMoneda2())){
				ctacte.setImporte2(transaccion.CtaCteImporte());
				ctacte.setNeto2(transaccion.CtaCteNeto());				
			}
			else{
				// Dolares se calculan con respecto a pesos cuando la moneda original no es dólares.
				ctacte.setImporte2(tr.calcularImporte2(ctacte.getImporte1()));
				ctacte.setNeto2(tr.calcularImporte2(ctacte.getNeto1()));
			}			
			ctacte.setSaldo1(ctacte.getImporte1());
			ctacte.setSaldo2(ctacte.getImporte2());
			ctacte.setIdTransaccion(tr.getId());
			ctacte.setTipoEntidad(tr.getClass().getSimpleName());
			ctacte.setDiferenciaCambio(transaccion.generadaPorDiferenciaCambio());
			if (ctacte.getDiferenciaCambio()){
				if (ctacte.getMonedaOriginal().equals(ctacte.getMoneda1())){
					ctacte.setImporte2(BigDecimal.ZERO);
					ctacte.setSaldo2(BigDecimal.ZERO);
				}
				else if (ctacte.getMonedaOriginal().equals(ctacte.getMoneda2())){
					ctacte.setImporte1(BigDecimal.ZERO);
					ctacte.setSaldo1(BigDecimal.ZERO);
				}
				else{
					throw new ValidationException("No se puede generar diferencia de cambio en la moneda " + ctacte.getMonedaOriginal().toString());
				}
			}
			XPersistence.getManager().persist(ctacte);
			
			// despues de persistir, sino podría fallar si la transaccion es nueva.
			transaccion.CtaCteReferenciarCuentaCorriente(ctacte);
			
			if (transaccion.generadaPorDiferenciaCambio()){
				Collection<DiferenciaCambioVenta> detalleDifCambio = new LinkedList<DiferenciaCambioVenta>();
				transaccion.detalleDiferenciaCambio(detalleDifCambio);
				Collection<ImputacionVenta> imputaciones = new LinkedList<ImputacionVenta>();
				Map<String, Object> repetidos = new HashMap<String, Object>();
				for(DiferenciaCambioVenta diferenciaCambio: detalleDifCambio){
					ImputacionVenta imputacion = diferenciaCambio.generarImputacionDiferenciaCambio(ctacte);
					String key = imputacion.getOrigen().getId() + imputacion.getDestino().getId();
					if (!repetidos.containsKey(key)){
						repetidos.put(key, null);
						
						XPersistence.getManager().persist(imputacion);
						imputaciones.add(imputacion);						
					}
					
				}
				for(ImputacionVenta imputacion: imputaciones){
					imputacion.confirmarTransaccion();
				}
			}		
			return ctacte;
		}
		else{
			return null;
		}
	}
	
	public static CuentaCorriente buscarCuentaCorriente(ITransaccionCtaCte transaccion){		
		Query query = XPersistence.getManager().createQuery("from CuentaCorriente where idTransaccion = :id");
		query.setParameter("id", transaccion.CtaCteTransaccion().getId());
		query.setMaxResults(1);
		List<?> result = query.getResultList();
		CuentaCorriente ctacte = null;
		if (!result.isEmpty()){
			ctacte = (CuentaCorriente)result.get(0);
		}		
		return ctacte;
	}
	
	public static CuentaCorriente anularCuentaCorriente(ITransaccionCtaCte transaccion){
		if (transaccion.generaCtaCte()){
			CuentaCorriente ctacte = buscarCuentaCorriente(transaccion);
			if (ctacte != null){
				// si la transacción generó imputaciones, se anulan 
				Collection<Imputacion> imputaciones = new LinkedList<Imputacion>();
				transaccion.imputacionesGeneradas(imputaciones);
				for (Imputacion imputacion: imputaciones){
					if (imputacion.getEstado().equals(Estado.Confirmada)){
						imputacion.anularTransaccion();
					}
				}
					
				ctacte.anular();
				return ctacte;
			}
			else{
				throw new ValidationException("No se puede anular. El comprobante " + transaccion.toString() + " no impacto en la cuenta corriente.");
			}
		}
		else{
			return null;
		}
	}
	
	public OperadorComercial operadorCtaCte(){
		return null;
	}
	
	protected void setOperadorCtaCte(OperadorComercial operador){		
	}
	
	protected IResponsableCuentaCorriente getResponsableCtaCte(){
		return null;
	}
	
	protected void setResponsableCtaCte(IResponsableCuentaCorriente responsable){		
	}
	
	@ReadOnly
	private Date fecha;
	
	@ReadOnly
	private Date fechaVencimiento;
	
	@ReadOnly(notForViews="CambioFechaProbable")
	private Date fechaProbable;
	
	@Column(length=25) 
	@ReadOnly 
	private String tipo = "";
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@ReadOnly
	private Empresa empresa;
	
	@Column(length=20) 
	@ReadOnly
	@SearchKey
	private String numero = "";
	
	@ReadOnly
	private Boolean anulado = Boolean.FALSE;
	
	@ReadOnly
	private Boolean pendiente = Boolean.TRUE;
			
	@ReadOnly
	private BigDecimal ingreso;
	
	@ReadOnly
	private BigDecimal egreso;
	
	@org.hibernate.annotations.Formula("(case when importe2 >= 0 then importe2 else 0 end)")
	private BigDecimal ingreso2;
	
	@org.hibernate.annotations.Formula("(case when importe2 < 0 then importe2 * -1 else 0 end)")
	private BigDecimal egreso2;
	
	@ReadOnly
	private BigDecimal importeOriginal;
	
	@ReadOnly
	private BigDecimal saldoOriginal;
	
	@ReadOnly
	@Hidden
	private BigDecimal netoOriginal;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@ReadOnly
	private Moneda monedaOriginal;
	
	@ReadOnly
	private BigDecimal cotizacion;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@ReadOnly
	private Moneda moneda1;
	
	@ReadOnly
	private BigDecimal importe1;
		
	@ReadOnly
	private BigDecimal saldo1;
	
	@ReadOnly
	@Hidden
	private BigDecimal neto1;
	
	@ReadOnly
	private BigDecimal cotizacion2;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@ReadOnly
	private Moneda moneda2;
	
	@ReadOnly
	private BigDecimal importe2;
	
	@ReadOnly
	@Hidden
	private BigDecimal neto2;
	
	@ReadOnly
	private BigDecimal saldo2;
	
	private Boolean diferenciaCambio = Boolean.FALSE;
	
	@ReadOnly
	@Hidden
	@Column(length=32)
	@Required
	private String idTransaccion;
	
	@ReadOnly
	@Hidden
	@Column(length=100)
	@Required
	private String tipoEntidad;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly(notForViews="CambioClasificador")
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre", forTabs="combo")
	// no funciona 
	//@Action(value="ModificarAtributoConAuditoria.Cambiar", alwaysEnabled=true, notForViews="CambioClasificador")
	private ClasificadorCuentaCorriente clasificador;
	
	public Date getFecha() {
		return fecha;
	}

	public void setFecha(Date fecha) {
		this.fecha = fecha;
	}

	public Date getFechaVencimiento() {
		return fechaVencimiento;
	}

	public void setFechaVencimiento(Date fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}

	public Date getFechaProbable() {
		return fechaProbable;
	}

	public void setFechaProbable(Date fechaProbable) {
		this.fechaProbable = fechaProbable;
	}

	public String getTipo() {
		return tipo == null ? "" : tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getNumero() {
		return numero == null ? "": numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}
	
	public BigDecimal getIngreso() {
		return ingreso;
	}

	public void setIngreso(BigDecimal ingreso) {
		this.ingreso = ingreso;
	}

	public BigDecimal getEgreso() {
		return egreso;
	}

	public void setEgreso(BigDecimal egreso) {
		this.egreso = egreso;
	}

	public BigDecimal getIngreso2() {
		return ingreso2;
	}

	public BigDecimal getEgreso2() {
		return egreso2;
	}

	public BigDecimal getImporteOriginal() {
		return importeOriginal == null ? BigDecimal.ZERO : this.importeOriginal;
	}

	public void setImporteOriginal(BigDecimal importeOriginal) {
		this.importeOriginal = aplicarRedondeo(importeOriginal);
	}
	
	public BigDecimal getNetoOriginal() {
		return netoOriginal == null ? BigDecimal.ZERO : this.netoOriginal;
	}

	public void setNetoOriginal(BigDecimal netoOriginal) {
		this.netoOriginal = aplicarRedondeo(netoOriginal);
	}

	public Moneda getMonedaOriginal() {
		return monedaOriginal;
	}

	public void setMonedaOriginal(Moneda monedaOriginal) {
		this.monedaOriginal = monedaOriginal;
	}

	public Moneda getMoneda1() {
		return moneda1;
	}

	public void setMoneda1(Moneda moneda1) {
		this.moneda1 = moneda1;
	}

	public BigDecimal getImporte1() {
		return importe1 == null ? BigDecimal.ZERO : this.importe1;
	}

	public void setImporte1(BigDecimal importe1) {
		this.importe1 = aplicarRedondeo(importe1);
		if (this.getImporte1().compareTo(BigDecimal.ZERO) >= 0){
			this.setIngreso(this.getImporte1());
			this.setEgreso(BigDecimal.ZERO);
		}
		else{
			this.setIngreso(BigDecimal.ZERO);
			this.setEgreso(this.getImporte1().abs());
		}
	}

	public BigDecimal getSaldo1() {
		return saldo1 == null ? BigDecimal.ZERO : saldo1;
	}

	public void setSaldo1(BigDecimal saldo1) {
		if (saldo1 != null){
			if (this.getImporteOriginal().compareTo(BigDecimal.ZERO) != 0){
				if (this.getImporteOriginal().compareTo(BigDecimal.ZERO) > 0){
					// se verifica que el saldo no sea negativo
					if (saldo1.compareTo(BigDecimal.ZERO) < 0){
						this.saldo1 = BigDecimal.ZERO;
					}
					else{
						this.saldo1 = aplicarRedondeo(saldo1);
					}
				}
				else{
					// se verifica que el saldo no sea positivo
					if (saldo1.compareTo(BigDecimal.ZERO) > 0){
						this.saldo1 = BigDecimal.ZERO;
					}
					else{
						this.saldo1 = aplicarRedondeo(saldo1);
					}
				}
			}
			else{
				this.saldo1 = aplicarRedondeo(saldo1);	
			}
		}
	}

	public Moneda getMoneda2() {
		return moneda2;
	}

	public void setMoneda2(Moneda moneda2) {
		this.moneda2 = moneda2;
	}

	public BigDecimal getImporte2() {
		return importe2 == null ? BigDecimal.ZERO : this.importe2;
	}

	public void setImporte2(BigDecimal importe2) {
		this.importe2 = aplicarRedondeo(importe2);
	}

	public BigDecimal getSaldo2() {
		return saldo2 == null ? BigDecimal.ZERO : this.saldo2;
	}

	public void setSaldo2(BigDecimal saldo2) {
		if (saldo2 != null){
			if (this.getImporteOriginal().compareTo(BigDecimal.ZERO) != 0){
				if (this.getImporteOriginal().compareTo(BigDecimal.ZERO) > 0){
					// se verifica que el saldo no sea negativo
					if (saldo2.compareTo(BigDecimal.ZERO) < 0){
						this.saldo2 = BigDecimal.ZERO;
					}
					else{
						this.saldo2 = aplicarRedondeo(saldo2);
					}
				}
				else{
					// se verifica que el saldo no sea positivo
					if (saldo2.compareTo(BigDecimal.ZERO) > 0){
						this.saldo2 = BigDecimal.ZERO;
					}
					else{
						this.saldo2 = aplicarRedondeo(saldo2);
					}
				}
			}
			else{
				this.saldo2 = aplicarRedondeo(saldo2);	
			}
		}
	}

	public BigDecimal getCotizacion() {
		return cotizacion == null ? BigDecimal.ZERO : this.cotizacion;
	}

	public void setCotizacion(BigDecimal cotizacion) {
		this.cotizacion = cotizacion;
	}

	public BigDecimal getCotizacion2() {
		return cotizacion2 == null ? BigDecimal.ZERO : this.cotizacion2;
	}

	public void setCotizacion2(BigDecimal cotizacion2) {
		this.cotizacion2 = cotizacion2;
	}
	
	public Boolean getAnulado() {
		return anulado;
	}

	public void setAnulado(Boolean anulado) {
		this.anulado = anulado;
	}
			
	public String getIdTransaccion() {
		return idTransaccion;
	}

	public void setIdTransaccion(String idTransaccion) {
		this.idTransaccion = idTransaccion;
	}

	public String getTipoEntidad() {
		return tipoEntidad;
	}

	public void setTipoEntidad(String tipoEntidad) {
		this.tipoEntidad = tipoEntidad;
	}

	public void anular(){
		if (! tieneImputaciones()){
			if (!this.getAnulado()){
				this.setAnulado(Boolean.TRUE);
				this.setImporte1(BigDecimal.ZERO);
				this.setImporte2(BigDecimal.ZERO);
				this.setSaldoOriginal(BigDecimal.ZERO);
				this.setSaldo1(BigDecimal.ZERO);
				this.setSaldo2(BigDecimal.ZERO);
				if (this.getIngreso().compareTo(BigDecimal.ZERO) != 0){
					this.setEgreso(this.getIngreso());
				}
				else{
					this.setIngreso(this.getEgreso());
				}
			}
			else{
				throw new ValidationException("No se puede anular " + this.toString() + ". Ya esta anulado");
			}
		}
		else{
			throw new ValidationException("No se puede anular " + this.toString() + ". Tiene imputaciones");
		}
	}
	
	private boolean tieneImputaciones(){
		boolean tieneImputaciones = !(this.getImporteOriginal().compareTo(this.getSaldoOriginal()) == 0);
		if (this.getMonedaOriginal().equals(this.getMoneda1())){
			tieneImputaciones = !(this.getImporte1().compareTo(this.getSaldo1()) == 0);
		}
		else if (this.getMonedaOriginal().equals(this.getMoneda2())){
			tieneImputaciones = !(this.getImporte2().compareTo(this.getSaldo2()) == 0);
		}
		return tieneImputaciones;
	}
	
	@Override
	public String toString(){
		return this.getTipo() + " " + this.getNumero();
	}

	public Boolean getDiferenciaCambio() {
		return diferenciaCambio;
	}

	public void setDiferenciaCambio(Boolean diferenciaCambio) {
		this.diferenciaCambio = diferenciaCambio;
	}
	
	public BigDecimal saldoEnMoneda(Moneda moneda) {
		BigDecimal saldo = null;
		if (moneda.equals(this.getMoneda1())){
			saldo = this.getSaldo1();
			saldo = saldo.add(this.diferenciasCambioMoneda1NoImputadas());
		}
		else if (moneda.equals(this.getMoneda2())){
			saldo = this.getSaldo2();
		}
		else if (moneda.equals(this.getMonedaOriginal())){
			saldo = this.getSaldoOriginal();
			//saldo = this.getSaldo1().divide(this.getCotizacion(), 2, RoundingMode.HALF_EVEN);
		}
		else{
			saldo = this.getSaldoOriginal().multiply(Cotizacion.buscarCotizacion(this.getMonedaOriginal(), moneda, this.getFecha()));
			//saldo = this.getSaldo1().multiply(Cotizacion.buscarCotizacion(this.getMoneda1(), moneda, this.getFecha()));
		}
		return saldo.setScale(2, RoundingMode.HALF_EVEN);
	}
	
	// Las diferencias de cambio en moneda 1 como no se imputan en el momento, se deben agregar en forma temporal, 
	// para que se consideren en el saldo de moneda 1
	public void agregarDifenciasCambiosMoneda1NoImputadas(BigDecimal difCambio){
	}
	
	protected BigDecimal diferenciasCambioMoneda1NoImputadas(){
		return BigDecimal.ZERO;
	}
	
	// no tiene saldo ni tampoco diferencias de cambio
	public Boolean saldado(){
		int comparacion = this.getImporteOriginal().compareTo(BigDecimal.ZERO);
		if (comparacion > 0){
			if ((this.getMonedaOriginal().equals(this.getMoneda1()) || this.getMonedaOriginal().equals(this.getMoneda2()))){
				if ((this.getSaldo1().compareTo(BigDecimal.ZERO) <= 0) && (this.getSaldo2().compareTo(BigDecimal.ZERO) <= 0)){
					return Boolean.TRUE;
				}
				else{
					return Boolean.FALSE;
				}
			}
			else{
				if ((this.getSaldo1().compareTo(BigDecimal.ZERO) <= 0) && (this.getSaldoOriginal().compareTo(BigDecimal.ZERO) <= 0)){
					return Boolean.TRUE;
				}
				else{
					return Boolean.FALSE;
				} 
			}
		}
		else if (comparacion < 0){
			if ((this.getMonedaOriginal().equals(this.getMoneda1()) || this.getMonedaOriginal().equals(this.getMoneda2()))){
				if ((this.getSaldo1().compareTo(BigDecimal.ZERO) >= 0) && (this.getSaldo2().compareTo(BigDecimal.ZERO) >= 0)){
					return Boolean.TRUE;
				}
				else{
					return Boolean.FALSE;
				}
			}
			else{
				if ((this.getSaldo1().compareTo(BigDecimal.ZERO) >= 0) && (this.getSaldoOriginal().compareTo(BigDecimal.ZERO) >= 0)){
					return Boolean.TRUE;
				}
				else{
					return Boolean.FALSE;
				}
			}
		}
		else{
			return Boolean.FALSE;
		}
	}
	
	public Boolean saldadoMonedaOriginal(){
		if (this.getMonedaOriginal().equals(this.getMoneda1())){
			if (this.getSaldo1().compareTo(BigDecimal.ZERO) != 0){
				return Boolean.FALSE;
			}
			else{
				return Boolean.TRUE;
			}
		}
		else if (this.getMonedaOriginal().equals(this.getMoneda2())){
			if (this.getSaldo2().compareTo(BigDecimal.ZERO) != 0){
				return Boolean.FALSE;
			}
			else{
				return Boolean.TRUE;
			}			
		}
		else{
			if (this.getSaldoOriginal().compareTo(BigDecimal.ZERO) != 0){
				return Boolean.FALSE;
			}
			else{
				return Boolean.TRUE;
			}	
			
		}
	}
	
	public Boolean saldadoEnMoneda(Moneda moneda){
		if (this.getMoneda1().equals(moneda)){
			if (this.getSaldo1().compareTo(BigDecimal.ZERO) != 0){
				return Boolean.FALSE;
			}
			else{
				return Boolean.TRUE;
			}
		}
		else if (this.getMoneda2().equals(moneda)){
			if (this.getSaldo2().compareTo(BigDecimal.ZERO) != 0){
				return Boolean.FALSE;
			}
			else{
				return Boolean.TRUE;
			}			
		}
		else{
			return saldadoMonedaOriginal();
		}
	}
	
	@Override
	protected void onPrePersist() {
		super.onPrePersist();
		
		this.actualizarSaldoOriginalMoneda1y2();
		this.setPendiente(!this.saldadoMonedaOriginal());
		
		this.setFechaProbable(this.getFechaVencimiento());
	}
	
	@PreUpdate
	protected void onPreUpdate() {
		super.onPreUpdate();
		
		this.actualizarSaldoOriginalMoneda1y2();
		this.setPendiente(!this.saldadoMonedaOriginal());
	}

	public Boolean getPendiente() {
		return pendiente;
	}

	public void setPendiente(Boolean pendiente) {
		this.pendiente = pendiente;
	}
	
	public Boolean getPendienteDiferenciaCambio() {
		return this.diferenciasCambioMoneda1NoImputadas().compareTo(BigDecimal.ZERO) != 0;
	}

	public boolean ingresa(){
		if (this.getIngreso().compareTo(BigDecimal.ZERO) > 0){
			return true;
		}
		else{
			return false;
		}
	}
	
	public Imputacion crearImputacion(CuentaCorriente origen, CuentaCorriente destino){
		throw new ValidationException("No implementar crearImputacion");
	}
	
	public Transaccion buscarTransaccion(){
		if (!Is.emptyString(this.getIdTransaccion()) && (!Is.emptyString(this.getTipoEntidad()))){
			Query query = XPersistence.getManager().createQuery("from " + this.getTipoEntidad() + " where id = :id");
			query.setParameter("id", this.getIdTransaccion());
			query.setMaxResults(1);
			List<?> list = query.getResultList();
			if (!list.isEmpty()){
				return (Transaccion)list.get(0);
			}
			else{
				return null;	
			}
		}
		else{
			return null;
		}
	}

	public BigDecimal getNeto1() {
		return neto1 == null ? BigDecimal.ZERO : this.neto1;
	}

	public void setNeto1(BigDecimal neto1) {
		this.neto1 = aplicarRedondeo(neto1);
	}

	public BigDecimal getNeto2() {
		return neto2 == null ? BigDecimal.ZERO : this.neto2;
	}

	public void setNeto2(BigDecimal neto2) {
		this.neto2 = aplicarRedondeo(neto2);
	}

	@Override
	public String generadaPorId() {
		return this.getIdTransaccion();
	}

	@Override
	public String generadaPorTipoEntidad() {
		return this.getTipoEntidad();
	}
	
	private BigDecimal aplicarRedondeo(BigDecimal importe){
		if (importe != null){
			return importe.setScale(2, RoundingMode.HALF_EVEN);
		}
		else{
			return importe;
		}
	}

	public BigDecimal getSaldoOriginal() {
		return saldoOriginal == null ? BigDecimal.ZERO : saldoOriginal;
	}

	public void setSaldoOriginal(BigDecimal saldoOriginal) {
		if (saldoOriginal != null){
			if (this.getImporteOriginal().compareTo(BigDecimal.ZERO) != 0){
				if (this.getImporteOriginal().compareTo(BigDecimal.ZERO) > 0){
					// se verifica que el saldo no sea negativo
					if (saldoOriginal.compareTo(BigDecimal.ZERO) < 0){
						this.saldoOriginal = BigDecimal.ZERO;
					}
					else{
						this.saldoOriginal = aplicarRedondeo(saldoOriginal);
					}
				}
				else{
					// se verifica que el saldo no sea positivo
					if (saldoOriginal.compareTo(BigDecimal.ZERO) > 0){
						this.saldoOriginal = BigDecimal.ZERO;
					}
					else{
						this.saldoOriginal = aplicarRedondeo(saldoOriginal);
					}
				}
			}
			else{
				this.saldoOriginal = aplicarRedondeo(saldoOriginal);	
			}
		}
	}

	public boolean tercerMoneda(){
		if (!this.getMonedaOriginal().equals(this.getMoneda1()) && !this.getMonedaOriginal().equals(this.getMoneda2())){
			return true;
		}
		else{
			return false;
		}
	}
	
	private void actualizarSaldoOriginalMoneda1y2(){
		// Ojo: se llama en el prepersist y preupdate
		if (this.getMonedaOriginal().equals(this.getMoneda1())){
			this.setSaldoOriginal(this.getSaldo1());
		}
		else if (this.getMonedaOriginal().equals(this.getMoneda2())){
			this.setSaldoOriginal(this.getSaldo2());
		}
		else{
			// tercer moneda: la moneda 2 siempre es una expresión de la moneda 1
			this.setSaldo2(this.getSaldo1().divide(this.getCotizacion2(), 2, RoundingMode.HALF_EVEN));
		}
	}

	public ClasificadorCuentaCorriente getClasificador() {
		return clasificador;
	}

	public void setClasificador(ClasificadorCuentaCorriente clasificador) {
		this.clasificador = clasificador;
	}
}
