package org.openxava.tesoreria.model;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Min;

import org.openxava.annotations.*;
import org.openxava.base.model.EmpresaExterna;
import org.openxava.base.model.ITransaccionValores;
import org.openxava.base.model.ObjetoNegocio;
import org.openxava.base.model.Transaccion;
import org.openxava.contabilidad.model.GeneradorItemContablePorTr;
import org.openxava.contabilidad.model.IGeneradorItemContable;
import org.openxava.contabilidad.model.ITransaccionContable;
import org.openxava.contabilidad.model.TipoCuentaContable;
import org.openxava.negocio.filter.SucursalEmpresaFilter;
import org.openxava.negocio.model.OperadorComercial;
import org.openxava.negocio.model.Sucursal;
import org.openxava.util.Messages;

@Entity

@Views({
	@View(members=
		"Principal[#" + 
				"descripcion;" +
				"empresa, cotizacion;" +
				"numero, fecha, estado, fechaCreacion, usuario;" + 				
				"observaciones];" +
		"Canje[" +		
			"cuenta;caja;tipoValor, importe;" +
			"numeroCheque" + 
		"]"
		 
	),
	@View(name="Simple", members="numero")
})

@Tabs({
	@Tab(
		properties="empresa.nombre, fecha, numero, estado, tipoValor.nombre, importe, tipoValor.moneda.nombre, cuenta.nombre, caja.nombre",
		filter=SucursalEmpresaFilter.class,
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL,
		defaultOrder="${fechaCreacion} desc")
})

public class CanjeValores extends Transaccion implements ITransaccionValores, ITransaccionContable, IItemMovimientoValores{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView("Simple")
	@DescriptionsList(descriptionProperties=("codigo, nombre"),
		depends="empresa",
		condition="${empresa.id} = ?", forTabs="combo")
	private Caja caja;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView("Simple")
	@DescriptionsList(descriptionProperties=("codigo, nombre"),
		depends="empresa",
		condition="${empresa.id} = ?", forTabs="combo")
	private CuentaBancaria cuenta;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify	
	@DescriptionsList(descriptionProperties=("codigo, nombre"), 					  
					  condition="${comportamiento} IN (0, 2)" )
	private TipoValorConfiguracion tipoValor;
	
	@Min(value=0, message="No puede menor a 0")
	@Stereotype("MONEY")
	private BigDecimal importe;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify @ReadOnly
	@ReferenceView("Simple")
	@NoFrame
	private Valor referencia;
	
	@Column(length=30)
	private String numeroCheque;
		
	@Override
	public String descripcionTipoTransaccion() {
		return "Canje de Valores";
	}

	public Caja getCaja() {
		return caja;
	}

	public void setCaja(Caja caja) {
		this.caja = caja;
	}

	public CuentaBancaria getCuenta() {
		return cuenta;
	}

	public void setCuenta(CuentaBancaria cuenta) {
		this.cuenta = cuenta;
	}

	public TipoValorConfiguracion getTipoValor() {
		return tipoValor;
	}

	public void setTipoValor(TipoValorConfiguracion tipoValor) {
		this.tipoValor = tipoValor;
	}

	public BigDecimal getImporte() {
		return importe == null ? BigDecimal.ZERO : this.importe;
	}

	public void setImporte(BigDecimal importe) {
		if (importe == null){
			this.importe = BigDecimal.ZERO;
		}
		else{
			this.importe = importe;
		}
	}

	public Valor getReferencia() {
		return referencia;
	}

	public void setReferencia(Valor referencia) {
		this.referencia = referencia;
	}

	public String getNumeroCheque() {
		return numeroCheque;
	}

	public void setNumeroCheque(String numeroCheque) {
		this.numeroCheque = numeroCheque;
	}

	@Override
	public void movimientosValores(List<IItemMovimientoValores> lista) {
		IItemMovimientoValores egresoChequeCtaBancaria = this;
		lista.add(egresoChequeCtaBancaria);

		if (this.getTipoValor().getComportamiento().equals(TipoValor.ChequePropio)){
			// se agrega un movimiento para debitar el efectivo automáticamente. 
			ItemTransEfectivo itemMovEfectivo = new ItemTransEfectivo();
			itemMovEfectivo.setTipoValorEfectivo(this.getCuenta().getEfectivo());
			itemMovEfectivo.setImporteEfectivo(this.getImporte());
			itemMovEfectivo.setGeneradoPor(this);					
			// como el cheque ya genera un movimiento financiero, este se inhibe
			itemMovEfectivo.setNoGenerarDefalle(true);
			lista.add(itemMovEfectivo);
		}
		
		ItemTransEfectivo itemIngresoCaja = new ItemTransEfectivo();
		itemIngresoCaja.setTesoreria(this.getCaja());
		itemIngresoCaja.setImporteEfectivo(this.getImporte());
		itemIngresoCaja.setTipoValorEfectivo(this.getCuenta().getEfectivo());
		lista.add(itemIngresoCaja); 
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
	public OperadorComercial operadorFinanciero() {
		return null;
	}

	@Override
	protected void asignarSucursal(){
		this.setSucursal(this.getCuenta().getSucursal());		
	}
	
	@Override
	@Hidden
	public Sucursal getSucursalDestino() {
		return this.getCaja().getSucursal();
	}

	@Override
	public void generadorPasesContable(Collection<IGeneradorItemContable> items) {
		GeneradorItemContablePorTr debe = new GeneradorItemContablePorTr(this, TipoCuentaContable.Finanzas.CuentaContablePorTipo(this.getTipoValor()));
		debe.setDebe(this.getImporte());
		items.add(debe);
		
		GeneradorItemContablePorTr haber = new GeneradorItemContablePorTr(this, TipoCuentaContable.Finanzas.CuentaContablePorTipo(this.getCuenta()));
		haber.setHaber(this.getImporte());
		items.add(haber);
	}

	@Override
	public EmpresaExterna ContabilidadOriginante() {
		return null;
	}

	@Override
	@Hidden
	public Banco getBanco() {
		return this.getCuenta().getBanco();
	}

	@Override
	public void setBanco(Banco banco) {		
	}

	@Override
	@Hidden
	public String getDetalle() {
		return null;
	}

	@Override
	public void setDetalle(String detalle) {		
	}

	@Override
	@Hidden
	public Date getFechaEmision() {
		return this.getFecha();
	}

	@Override
	public void setFechaEmision(Date fechaEmision) {
	}

	@Override
	@Hidden
	public Date getFechaVencimiento() {
		return this.getFecha();
	}

	@Override
	public void setFechaVencimiento(Date fechaVencimiento) {		
	}

	@Override
	@Hidden
	public String getNumeroValor() {
		return this.getNumeroCheque();
	}

	@Override
	public void setNumeroValor(String numeroValor) {
		this.setNumeroCheque(numeroValor);
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
		return this.getImporte();
	}

	@Override
	public BigDecimal importeMonedaTrValores(Transaccion transaccion) {
		return this.getImporte();
	}

	@Override
	public TipoMovimientoValores tipoMovimientoValores(boolean reversion) {
		if (!reversion){
			return new TipoMovEgresoValores(true);
		}
		else{
			return new TipoMovAnulacionEgrValores(true);
		}
	}

	@Override
	public void asignarOperadorComercial(Valor valor, Transaccion transaccion) {		
	}

	@Override
	public OperadorComercial operadorComercialValores(Transaccion transaccion) {
		return null;
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
	
	@Override
	protected void validacionesPreGrabarTransaccion(Messages errores){
		super.validacionesPreGrabarTransaccion(errores);
		
		if (!this.getCaja().getSucursal().equals(this.getCuenta().getSucursal())){
			errores.add("La sucursal de la caja debe ser la misma que la cuenta");
		}
	}
	
	@Override
	protected void sincronizarCotizaciones() {
		this.setMoneda(this.getCuenta().getEfectivo().getMoneda());
		
		super.sincronizarCotizaciones();
	}
	
	@Override
	public IChequera chequera() {
		return null;
	}
	
	@Override
	public BigDecimal ContabilidadTotal() {
		return null;
	}

	@Override
	public void despuesPersistirMovimientoFinanciero(MovimientoValores item, boolean revierte) {
	}	
}

