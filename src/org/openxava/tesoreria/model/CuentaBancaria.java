package org.openxava.tesoreria.model;

import java.util.Collection;
import java.util.Collections;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.base.validators.*;
import org.openxava.conciliacionbancaria.model.ExtractoBancario;
import org.openxava.conciliacionbancaria.model.GrupoConciliacion;
import org.openxava.conciliacionbancaria.model.ResumenExtractoBancario;
import org.openxava.contabilidad.model.*;
import org.openxava.negocio.filter.SucursalEmpresaFilter;
import org.openxava.negocio.model.*;
import org.openxava.negocio.validators.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(members="codigo, activo;" + 
			"nombre;" +
			"empresa, sucursal, principal;" +
			"banco, tipo;" + 
			"efectivo, numeroCuenta, sucursalBancaria;" +
			"valoresPosibles;" +
			"cuentaContableFinanzas;" + 
			"Chequera[proximoNumeroChequera, ultimoNumeroChequera; multiplesChequeras; chequeras];" 
			),
	@View(name="Simple", members="codigo, nombre"),
	@View(name="SimpleConEmpresa", members="codigo, nombre, empresa"),
	@View(name="ConciliacionBancaria", 
		members="Conciliacion{movimientosFinancieros;" +
					"extractoBancario;}" + 
				"Cuenta{codigo, nombre;" + 
					"resumenes;}" + 
				"GrupoConciliacion{grupoConciliacion}")
})

@Tabs({
	@Tab(filter=EmpresaFilter.class,
			baseCondition=EmpresaFilter.BASECONDITION + " and " + ObjetoEstatico.CONDITION_ACTIVOS ),
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		filter=EmpresaFilter.class,
		baseCondition=EmpresaFilter.BASECONDITION + " and " + ObjetoEstatico.CONDITION_INACTIVOS),
	@Tab(name="ConciliacionBancaria", 
		properties="codigo, nombre, banco.nombre",
		filter=SucursalEmpresaFilter.class,
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL + " and " + ObjetoEstatico.CONDITION_ACTIVOS)
})

@EntityValidators({
	@EntityValidator(
			value=UnicidadValidator.class, 
			properties= {
				@PropertyValue(name="id"), 
				@PropertyValue(name="atributo", value="codigo"),
				@PropertyValue(name="valor", from="codigo"),
				@PropertyValue(name="modelo", value="Tesoreria"),
				@PropertyValue(name="idMessage", value="codigo_repetido_tesoreria")
			}
	),
	@EntityValidator(
			value=PrincipalSucursalEmpresaValidator.class, 
			properties= {
				@PropertyValue(name="idEntidad", from="id"), 
				@PropertyValue(name="modelo", value="CuentaBancaria"),
				@PropertyValue(name="empresa", from="empresa"),
				@PropertyValue(name="sucursal", from="sucursal"),
				@PropertyValue(name="principal")
			}
	)
})

public class CuentaBancaria extends Tesoreria implements IChequera{
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate
	@NoModify
	@Required
	@DescriptionsList(descriptionProperties="codigo, nombre")
	private Banco banco;
	
	private TipoCuentaBancaria tipo;
		
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre",
					condition="${comportamiento} = 0")
	@NoCreate @NoModify @Required
	private TipoValorConfiguracion efectivo;
		
	private Long proximoNumeroChequera;
	
	private Long ultimoNumeroChequera;
		
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@Required
	@ReferenceView("Simple")
	@NoCreate @NoModify
	private CuentaContable cuentaContableFinanzas;
	
	@OneToMany(mappedBy="cuenta", cascade=CascadeType.ALL)
	@ListProperties(value="codigo, activo, proximoNumero, primerNumero, ultimoNumero")
	@ReadOnly
	private Collection<Chequera> chequeras;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre", 
			 depends="this.banco", condition="${banco.id} = ?")
	private SucursalBanco sucursalBancaria;
	
	@Column(length=25)
	private String numeroCuenta;
	
	public Banco getBanco() {
		return banco;
	}

	public void setBanco(Banco banco) {
		this.banco = banco;
	}

	public TipoCuentaBancaria getTipo() {
		return tipo;
	}

	public void setTipo(TipoCuentaBancaria tipo) {
		this.tipo = tipo;
	}

	public TipoValorConfiguracion getEfectivo() {
		return efectivo;
	}

	public void setEfectivo(TipoValorConfiguracion efectivo) {
		this.efectivo = efectivo;
	}

	public Long getProximoNumeroChequera() {
		return proximoNumeroChequera;
	}

	public void setProximoNumeroChequera(Long proximoNumeroChequera) {
		this.proximoNumeroChequera = proximoNumeroChequera;
	}

	public Long getUltimoNumeroChequera() {
		return ultimoNumeroChequera;
	}

	public void setUltimoNumeroChequera(Long ultimoNumeroChequera) {
		this.ultimoNumeroChequera = ultimoNumeroChequera;
	}

	public CuentaContable getCuentaContableFinanzas() {
		return cuentaContableFinanzas;
	}

	public void setCuentaContableFinanzas(CuentaContable cuentaContableFinanzas) {
		this.cuentaContableFinanzas = cuentaContableFinanzas;
	}

	@Override
	public TipoValorConfiguracion consolidaCon(TipoValorConfiguracion tipoValorConfiguracion) {	
		if (tipoValorConfiguracion.getMoneda().equals(this.getEfectivo().getMoneda())){
			return this.getEfectivo();
		}
		else{
			return super.consolidaCon(tipoValorConfiguracion);
		}
	}
	
	@Override
	public boolean permiteTipoValor(TipoValorConfiguracion tipoValor){
		boolean permite = super.permiteTipoValor(tipoValor);
		if (permite){
			permite = tipoValor.getComportamiento().permiteCuentaBancaria();
		}
		return permite;
	}

	public String numerarCheque(TipoValorConfiguracion tipoValor) {
		Long numero = this.getProximoNumeroChequera();
		Long ultimoNumero = this.getUltimoNumeroChequera();
		if ((numero != null) && (ultimoNumero != null)){
			if (numero.compareTo(ultimoNumero) <= 0){
				this.setProximoNumeroChequera(this.getProximoNumeroChequera() + 1);
				return formatearNumeroCheque(numero);
				
			}
			else{
				throw new ValidationException("Ya se utilizó el último número de la chequera en " + this.toString());
			}
		}
		else{
			throw new ValidationException("No esta configurado la numeración para la chequera en " + this.toString());
		}
		
	}

	private String formatearNumeroCheque(Long numero) {		 
		return numero.toString();
	}
	
	public void notificarUsoCheque(String numeroCheque){
		try{
			Long nro = Long.parseLong(numeroCheque);
			if (this.getProximoNumeroChequera() < nro){
				this.setProximoNumeroChequera(nro + 1);
			}
		}
		catch(Exception e){			
		}
		
	}
	
	@Override
	public Boolean esCuentaBancaria() {
		return Boolean.TRUE;
	}
	
	@Override
	public TipoValorConfiguracion tipoValorEfectivo(Moneda moneda){
		TipoValorConfiguracion tipo = null;
		if (this.getEfectivo() != null){
			if (this.getEfectivo().getMoneda().equals(moneda)){
				tipo = this.getEfectivo();
			}
		}
		
		if (tipo == null){
			tipo = super.tipoValorEfectivo(moneda);
		}
		
		return tipo;
	}
		

	@ReadOnly
	@ListProperties(value="conciliado, fechaComprobante, tipoComprobante, numeroComprobante, importeOriginal, detalle, anulacion, numero, fechaCreacion, usuario")
	@OrderBy("fechaCreacion desc")
	@Condition("${tesoreria.id} = ${this.id}")
	@RowStyle(style="pendiente-ejecutado", property="conciliado", value="true")
	@ListAction("ItemConciliacionBancaria.AnularConciliar")
	public Collection<MovimientoValores> getMovimientosFinancieros() {
		return null;
	}
	
	@ReadOnly
	@ListProperties(value="fecha, concepto, importe, credito, debito, saldo, observaciones, resumen.fechaCreacion, resumen.usuario, conciliado")
	@OrderBy("fecha desc, nroFila asc")
	@Condition("${resumen.cuenta.id} = ${this.id}")
	@RowStyle(style="pendiente-ejecutado", property="conciliado", value="true")
	@ListAction("ItemConciliacionBancaria.AnularConciliar")
	public Collection<ExtractoBancario> getExtractoBancario() {
		return null;
	}
	
	@OneToMany(mappedBy="cuenta", cascade=CascadeType.ALL)
	@ListProperties(value="fechaCreacion, usuario, desde, hasta")
	@OrderBy("fechaCreacion desc")
	private Collection<ResumenExtractoBancario> resumenes;
		
	public Collection<ResumenExtractoBancario> getResumenes() {
		return resumenes;
	}

	public void setResumenes(Collection<ResumenExtractoBancario> resumenes) {
		this.resumenes = resumenes;
	}
		
	@SuppressWarnings("unchecked")
	public Collection<Chequera> getChequeras() {
		return chequeras == null ? Collections.EMPTY_LIST : this.chequeras;
	}

	public void setChequeras(Collection<Chequera> chequeras) {
		this.chequeras = chequeras;
	}

	public SucursalBanco getSucursalBancaria() {
		return sucursalBancaria;
	}

	public void setSucursalBancaria(SucursalBanco sucursalBancaria) {
		this.sucursalBancaria = sucursalBancaria;
	}

	public String getNumeroCuenta() {
		return numeroCuenta;
	}

	public void setNumeroCuenta(String numeroCuenta) {
		this.numeroCuenta = numeroCuenta;
	}
	
	@ReadOnly
	@ListProperties(value="id, fechaCreacion, usuario, anulado")
	@OrderBy("fechaCreacion desc")
	@Condition("${cuenta.id} = ${this.id}")
	@ListAction("ItemConciliacionBancaria.AnularConciliar")
	public Collection<GrupoConciliacion> getGrupoConciliacion() {
		return null;
	}
}
