package org.openxava.tesoreria.actions;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.openxava.annotations.DefaultValueCalculator;
import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.SearchAction;
import org.openxava.annotations.Tab;
import org.openxava.annotations.Tabs;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.base.model.ITransaccionValores;
import org.openxava.base.model.ObjetoNegocio;
import org.openxava.base.model.Transaccion;
import org.openxava.calculators.CurrentDateCalculator;
import org.openxava.compras.model.Proveedor;
import org.openxava.negocio.filter.SucursalEmpresaFilter;
import org.openxava.negocio.model.OperadorComercial;
import org.openxava.negocio.model.Sucursal;
import org.openxava.tesoreria.model.Banco;
import org.openxava.tesoreria.model.Chequera;
import org.openxava.tesoreria.model.ConceptoTesoreria;
import org.openxava.tesoreria.model.CuentaBancaria;
import org.openxava.tesoreria.model.IChequera;
import org.openxava.tesoreria.model.IItemMovimientoValores;
import org.openxava.tesoreria.model.ItemTransCambiarValor;
import org.openxava.tesoreria.model.MovimientoValores;
import org.openxava.tesoreria.model.Tesoreria;
import org.openxava.tesoreria.model.TipoMovAnulacionEgrValores;
import org.openxava.tesoreria.model.TipoMovEgresoValores;
import org.openxava.tesoreria.model.TipoMovimientoValores;
import org.openxava.tesoreria.model.TipoValorConfiguracion;
import org.openxava.tesoreria.model.Valor;
import org.openxava.util.Messages;

@Entity

@Views({
	@View(members=
		"Principal[#" + 				
				"empresa, usuario, fechaCreacion;" +
				"numero, fecha, estado;" + 				
				"];" +
		"observaciones;" +		
		"cheque;" + 		
		"nuevo[" +
			"cuenta;" + 
			"numeroChequeNuevo;" +
			"fechaEmision, fechaVencimiento;" + 
			"chequera;" +
		"]"
		 
	),
	@View(name="Simple", members="numero")
})

@Tabs({
	@Tab(
		properties="empresa.nombre, fecha, numero, estado, cheque.numero, numeroChequeNuevo",
		filter=SucursalEmpresaFilter.class,		
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL,
		defaultOrder="${fechaCreacion} desc")
})


public class CambioChequePropio extends Transaccion implements ITransaccionValores, IItemMovimientoValores{
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Cambio Cheque";
	}
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView("ChequePropio")	
	@SearchAction("CambioChequePropio.buscarChequeParaCambiar")
	private Valor cheque;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private CuentaBancaria cuenta;
		
	@Column(length=30)
	private String numeroChequeNuevo;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@NoModify @NoCreate
	@ReferenceView("Egreso")
	@SearchAction("CambioChequePropio.buscarChequera")	
	private Chequera chequera;
	
	@DefaultValueCalculator(value=CurrentDateCalculator.class)
	private Date fechaEmision = new Date();
	
	private Date fechaVencimiento = new Date();

	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify @ReadOnly
	@ReferenceView("Simple")
	private Valor referencia;
	
	public Valor getReferencia() {
		return referencia;
	}

	public void setReferencia(Valor referencia) {
		this.referencia = referencia;
	}

	public CuentaBancaria getCuenta() {
		return cuenta;
	}

	public void setCuenta(CuentaBancaria cuenta) {
		this.cuenta = cuenta;
	}

	public Valor getCheque() {
		return cheque;
	}

	public void setCheque(Valor cheque) {
		this.cheque = cheque;
	}

	public String getNumeroChequeNuevo() {
		return numeroChequeNuevo;
	}

	public void setNumeroChequeNuevo(String numeroChequeNuevo) {
		this.numeroChequeNuevo = numeroChequeNuevo;
	}

	public Chequera getChequera() {
		return chequera;
	}

	public void setChequera(Chequera chequera) {
		this.chequera = chequera;
	}

	public Date getFechaEmision() {
		return fechaEmision;
	}

	public void setFechaEmision(Date fechaEmision) {
		this.fechaEmision = fechaEmision;
	}

	public Date getFechaVencimiento() {
		return fechaVencimiento;
	}

	public void setFechaVencimiento(Date fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}

	@Override
	protected void asignarSucursal(){
		this.setSucursal(this.getCheque().getSucursal());
	}
	
	protected void validacionesPreGrabarTransaccion(Messages errores){
		super.validacionesPreGrabarTransaccion(errores);
		
		if (this.getCheque() == null){
			errores.add("Cheque no asignado");
		}
		if (this.getCuenta() == null){
			errores.add("Cuenta no asignada");
		}
		if (this.getSucursal() == null){
			errores.add("Sucursal no asignada");
		}
		
		if (errores.isEmpty()){
			if (!this.getSucursal().equals(this.getCuenta().getSucursal())){
				errores.add("No coinciden la sucursal del cheque con la de la cuenta");
			}
			if (!this.getCheque().getEmpresa().equals(this.getEmpresa())){
				errores.add("No coincide la empresa del cheque");
			}
			if (!this.getCuenta().getEmpresa().equals(this.getEmpresa())){
				errores.add("No coincide la empresa de la cuenta");
			}
		}
		this.setMoneda(this.getCheque().getMoneda());
	}
	
	public void recalcularTotales(){
		super.recalcularTotales();		
	}
		
	@Override
	public void movimientosValores(List<IItemMovimientoValores> lista) {
		// genera el cheque nuevo
		lista.add(this);
		
		// se cambia el cheque
		ItemTransCambiarValor itemMovValores = new ItemTransCambiarValor();
		itemMovValores.asignarReferenciaValor(this.getCheque());
		lista.add(itemMovValores);
	}

	@Override
	public boolean revierteFinanzasAlAnular() {
		return true;
	}

	@Override
	public boolean actualizarFinanzasAlConfirmar() {
		return true;
	}

	@Override
	public void antesPersistirMovimientoFinanciero(MovimientoValores item) {
	}

	@Override
	public void despuesPersistirMovimientoFinanciero(MovimientoValores item, boolean revierte) {		
	}
	
	@Override
	public OperadorComercial operadorFinanciero() {
		return this.getCheque().getProveedor();	
	}

	@Override
	@Hidden
	public Sucursal getSucursalDestino() {		
		return null;
	}

	@Override
	@Hidden
	public TipoValorConfiguracion getTipoValor() {
		return this.getCheque().getTipoValor();
	}

	@Override
	public Banco getBanco() {
		return this.getCuenta().getBanco();
	}

	@Override
	public void setBanco(Banco banco) {		
	}

	@Override
	@Hidden
	public String getDetalle() {
		return this.getCheque().getDetalle();
	}

	@Override
	public void setDetalle(String detalle) {	
	}

	@Override
	@Hidden
	public String getNumeroValor() {
		return this.getNumeroChequeNuevo();
	}

	@Override
	public void setNumeroValor(String numeroValor) {
		this.setNumeroChequeNuevo(numeroValor);		
	}

	@Override
	@Hidden
	public String getFirmante() {
		return null;
	}

	@Override
	@Hidden
	public String getCuitFirmante() {
		return null;
	}

	@Override
	@Hidden
	public String getNroCuentaFirmante() {
		return null;
	}

	@Override
	public Tesoreria tesoreriaAfectada() {
		return this.getCuenta();
	}

	@Override
	public IChequera chequera() {
		return this.getChequera();
	}

	@Override
	public Tesoreria transfiere() {
		return null;
	}

	@Override
	public Valor referenciaValor() {
		return this.getReferencia();
	}

	@Override
	public void asignarReferenciaValor(Valor valor) {
		this.setReferencia(valor);		
	}

	@Override
	public BigDecimal importeOriginalValores() {
		return this.getCheque().getImporte();
	}

	@Override
	public BigDecimal importeMonedaTrValores(Transaccion transaccion) {
		return this.getCheque().getImporte();
	}

	@Override
	public TipoMovimientoValores tipoMovimientoValores(boolean reversion) {
		if (!reversion){
			return new TipoMovEgresoValores();
		}
		else{
			return new TipoMovAnulacionEgrValores();
		}
	}

	@Override
	public void asignarOperadorComercial(Valor valor, Transaccion transaccion) {
		valor.setProveedor((Proveedor)this.operadorFinanciero());
	}

	@Override
	public OperadorComercial operadorComercialValores(Transaccion transaccion) {
		return this.operadorFinanciero();
	}

	@Override
	public ConceptoTesoreria conceptoTesoreria() {
		return null;
	}

	@Override
	public boolean noGenerarDetalle() {
		return false;
	}

	@Override
	public ObjetoNegocio itemTrValores() {
		return this;
	}
}
