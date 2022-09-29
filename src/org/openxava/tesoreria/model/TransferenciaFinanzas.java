package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.contabilidad.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.filter.*;
import org.openxava.negocio.model.*;
import org.openxava.tesoreria.actions.*;
import org.openxava.util.*;

@Entity

@Views({
	@View(members=
		"Principal[#" + 
				"descripcion;" +
				"empresa, moneda, cotizacion;" +
				"numero, fecha;" +
				"estado, fechaCreacion, usuario;" + 				
				"observaciones];" +
		"Transferencia[" +		
			"origen;destino;tipoValor;" +
			"referencia;" + 
			"importeOriginal;" +
		"]"
		 
	),
	@View(name="Simple", members="numero")
})

@Tabs({
	@Tab(
		properties="empresa.nombre, fecha, numero, estado, tipoValor.nombre, importeOriginal, tipoValor.moneda.nombre, origen.nombre, destino.nombre",
		filter=SucursalDestinoEmpresaFilter.class,		
		baseCondition=SucursalDestinoEmpresaFilter.BASECONDITION_EMPRESASUCURSALDESTINO_TRANSACCIONES,
		defaultOrder="${fechaCreacion} desc")
})


public class TransferenciaFinanzas extends Transaccion implements ITransaccionValores, IItemMovimientoValores, ITransaccionContable {
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify @ReadOnly	
	private Sucursal sucursalDestino;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView("Simple")
	@DescriptionsList(descriptionProperties=("codigo, nombre"),
		depends="empresa",
		condition="${empresa.id} = ?")
	private Tesoreria origen;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView("Simple")
	@DescriptionsList(descriptionProperties=("codigo, nombre"),
	depends="empresa",
	condition="${empresa.id} = ?")
	private Tesoreria destino;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify	
	@DescriptionsList(descriptionProperties=("codigo, nombre"), 					  
					  condition="${comportamiento} IN (0, 1, 5)" )
	private TipoValorConfiguracion tipoValor;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView("SimpleCheque")
	@SearchAction(value="ReferenciasTransferenciaValores.buscarValor")
	@OnChange(OnChangeReferenciaEnTransferenciaFinanzas.class)
	private Valor referencia;
	
	@Stereotype("MONEY")
	@Required
	private BigDecimal importeOriginal;
		
	@Override
	public String descripcionTipoTransaccion() {
		return "Transferencia";
	}

	public Tesoreria getOrigen() {
		return origen;
	}

	public void setOrigen(Tesoreria origen) {
		this.origen = origen;
	}

	public Tesoreria getDestino() {
		return destino;
	}

	public void setDestino(Tesoreria destino) {
		this.destino = destino;
	}

	public TipoValorConfiguracion getTipoValor() {
		return tipoValor;
	}

	public void setTipoValor(TipoValorConfiguracion tipoValor) {
		this.tipoValor = tipoValor;
	}

	public Valor getReferencia() {
		return referencia;
	}

	public void setReferencia(Valor referencia) {
		this.referencia = referencia;
	}

	public BigDecimal getImporteOriginal() {
		return importeOriginal == null ? BigDecimal.ZERO : this.importeOriginal;
	}

	public void setImporteOriginal(BigDecimal importeOriginal) {
		this.importeOriginal = importeOriginal;
	}

	@Override
	public void movimientosValores(List<IItemMovimientoValores> lista) {
		lista.add(this);
		if (this.getTipoValor().getComportamiento().equals(TipoValor.ChequeTercero)){
			// un cheque de tercero cuando se transfiere a una caja, se agrega un movimiento para que se genera el detalle financiero 
			// del ingreso del cheque a la caja
			ItemTransMovValoresCustom itemIngresoPorTransferencia = new ItemTransMovValoresCustom(this);
			itemIngresoPorTransferencia.setTesoreria(this.transfiere());
			itemIngresoPorTransferencia.setTipo(new TipoMovIngPorTransfValores());			
			itemIngresoPorTransferencia.setTipoReversion(new TipoMovAnulacionIngPorTransfValores());
			lista.add(itemIngresoPorTransferencia);
		}
		else if (this.getTipoValor().getComportamiento().equals(TipoValor.Efectivo) || 
				this.getTipoValor().getComportamiento().equals(TipoValor.TarjetaCreditoCobranza)){
			// Si transfiere, hace un ingreso en el destino de la transferencia
			ItemTransEfectivo itemMovEfectivo = new ItemTransEfectivo();
			itemMovEfectivo.setTipoValorEfectivo(this.getTipoValor());
			itemMovEfectivo.setImporteEfectivo(this.importeOriginalValores());
			itemMovEfectivo.setTesoreria(this.transfiere());
			itemMovEfectivo.setGeneradoPor(this);
			lista.add(itemMovEfectivo);
		}
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
		return null;
	}

	@Override
	@Hidden
	public Banco getBanco() {
		if (this.getReferencia() != null){
			return this.getReferencia().getBanco();
		}
		else{
			return null;
		}
	}

	@Override
	public void setBanco(Banco banco) {	
	}

	@Override
	@Hidden
	public String getDetalle() {
		if (this.getReferencia() != null){
			return this.getReferencia().getDetalle();
		}
		else{
			return null;
		}
	}

	@Override
	public void setDetalle(String detalle) {		
	}

	@Override
	@Hidden
	public Date getFechaEmision() {
		if (this.getReferencia() != null){
			return this.getReferencia().getFechaEmision();
		}
		else{
			return null;
		}
	}

	@Override
	public void setFechaEmision(Date fechaEmision) {	
	}

	@Override
	@Hidden
	public Date getFechaVencimiento() {
		if (this.getReferencia() != null){
			return this.getReferencia().getFechaVencimiento();
		}
		else{
			return null;
		}
	}

	@Override
	public void setFechaVencimiento(Date fechaVencimiento) {
	}

	@Override
	public String getFirmante() {
		if (this.getReferencia() != null){
			return this.getReferencia().getFirmante();
		}
		else{
			return null;
		}
	}

	@Override
	public String getCuitFirmante() {
		if (this.getReferencia() != null){
			return this.getReferencia().getCuitFirmante();
		}
		else{
			return null;
		}
	}

	@Override
	public String getNroCuentaFirmante() {
		if (this.getReferencia() != null){
			return this.getReferencia().getNroCuentaFirmante();
		}
		else{
			return null;
		}
	}

	@Override
	public Tesoreria tesoreriaAfectada() {
		return this.getOrigen();
	}

	@Override
	public Tesoreria transfiere() {
		return this.getDestino();
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
		return this.getImporteOriginal();
	}

	@Override
	public BigDecimal importeMonedaTrValores(Transaccion transaccion) {		
		BigDecimal cotizacion = this.buscarCotizacionTrConRespectoA(this.getTipoValor().getMoneda());		
		return this.getImporteOriginal().divide(cotizacion, 2, RoundingMode.HALF_EVEN);				
	}

	@Override
	public TipoMovimientoValores tipoMovimientoValores(boolean reversion) {
		if(!reversion){
			return new TipoMovEgresoValores();
		}
		else{
			return new TipoMovAnulacionEgrValores();
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
	
	protected void validacionesPreGrabarTransaccion(Messages errores){
		super.validacionesPreGrabarTransaccion(errores);
		
		if ((this.getOrigen() != null) && (this.getDestino() != null)){
			if (!this.getOrigen().esCuentaBancaria()){
				if (this.getDestino().esCuentaBancaria()){
					errores.add("Si el origen es una caja, el destino debe ser una caja (Utilizar Depósito Bancario)");
				}
			}
			
			if (this.getOrigen().equals(this.destino)){
				errores.add("El origen debe ser distino al destino");
			}			
		}
		
		if (this.getTipoValor() != null){
			if (this.getTipoValor().getComportamiento().equals(TipoValor.Efectivo)){
				if (this.getImporteOriginal().compareTo(BigDecimal.ZERO) == 0){
					errores.add("El importe no puede ser 0");
				}
			}
			else if (this.getTipoValor().getComportamiento().equals(TipoValor.ChequeTercero)){
				if (this.getReferencia() == null){
					errores.add("Referencia no asignada");
				}
				else{
					if (this.getOrigen().esCuentaBancaria()){
						errores.add("No se puede transferir un cheque de tercero desde una cuenta bancaria");
					}
					if (this.getDestino().esCuentaBancaria()){
						errores.add("No se puede transferir un cheque de tercero a una cuenta bancaria. Utilizar depósito bancario");
					}
				}
			}
			else if (this.getTipoValor().getComportamiento().equals(TipoValor.TarjetaCreditoCobranza)){
				if (this.getImporteOriginal().compareTo(BigDecimal.ZERO) == 0){
					errores.add("El importe no puede ser 0");
				}
				if (this.getOrigen().esCuentaBancaria()){
					errores.add("No se puede transferir una tarjeta desde una cuenta bancaria");
				}
				if (this.getDestino().esCuentaBancaria()){
					errores.add("No se puede transferir una tarjeta a una cuenta bancaria");
				}
			}
			else{
				errores.add("Tipo Valor no permitido para transferencias");
			}
		}
	}
	
	@Override
	public void recalcularTotales(){
		super.recalcularTotales();
		
		if (this.getReferencia() != null){
			if (!this.getReferencia().getTipoValor().getComportamiento().equals(TipoValor.Efectivo)){
				this.setImporteOriginal(this.getReferencia().getImporte());
			}
		}
	}
	
	@Override
	@Hidden
	public String getNumeroValor(){
		if (this.getReferencia() != null){
			return this.getReferencia().getNumero();
		}
		else{
			return null;
		}
	}
	
	@Override
	public void setNumeroValor(String numeroValor){		
	}

	@Override
	public void generadorPasesContable(Collection<IGeneradorItemContable> items) {
		// Haber: tipo de valor: origen
		Tesoreria tesoreria = this.getOrigen();
		Query query = XPersistence.getManager().createQuery("from CuentaBancaria where id = :id");
		query.setParameter("id", tesoreria.getId());
		query.setMaxResults(1);
		List<?> result = query.getResultList();
		CuentaContable cuenta = null;
		if (!result.isEmpty()){
			CuentaBancaria cuentaBancaria = (CuentaBancaria)result.get(0);
			cuenta = TipoCuentaContable.Finanzas.CuentaContablePorTipo(cuentaBancaria);
		}
		else{
			cuenta = TipoCuentaContable.Finanzas.CuentaContablePorTipo(this.getTipoValor());
		}
		GeneradorItemContablePorTr paseHaber = new GeneradorItemContablePorTr(this, cuenta);
		paseHaber.setHaber(this.importeMonedaTrValores(this));
		
		
		// Debe: tipo de valor: destino
		tesoreria = this.getDestino();
		query = XPersistence.getManager().createQuery("from CuentaBancaria where id = :id");
		query.setParameter("id", tesoreria.getId());
		query.setMaxResults(1);
		result = query.getResultList();
		cuenta = null;
		if (!result.isEmpty()){
			CuentaBancaria cuentaBancaria = (CuentaBancaria)result.get(0);
			cuenta = TipoCuentaContable.Finanzas.CuentaContablePorTipo(cuentaBancaria);
		}
		else{
			cuenta = TipoCuentaContable.Finanzas.CuentaContablePorTipo(this.getTipoValor());
		}
		GeneradorItemContablePorTr paseDebe = new GeneradorItemContablePorTr(this, cuenta);
		paseDebe.setDebe(this.importeMonedaTrValores(this));
		
		if (!Is.equal(paseDebe.igcCuentaContable(), paseHaber.igcCuentaContable())){
			items.add(paseDebe);
			items.add(paseHaber);
		}
	}
	
	@Override
	public boolean generaContabilidad(){
		boolean generar = super.generaContabilidad();
		if (generar){
			Collection<IGeneradorItemContable> items = new LinkedList<IGeneradorItemContable>();
			this.generadorPasesContable(items);
			if (items.isEmpty()){
				generar = false;
			}
		}
		return generar;
		
	}

	public Sucursal getSucursalDestino() {
		return sucursalDestino;
	}

	public void setSucursalDestino(Sucursal sucursalDestino) {
		this.sucursalDestino = sucursalDestino;
	}
	
	@Override
	protected void asignarSucursal(){
		if (this.getOrigen() != null){
			this.setSucursal(this.getOrigen().getSucursal());
		}
		if (this.getDestino() != null){
			this.setSucursalDestino(this.getDestino().getSucursal());
		}
	}
	
	@Override
	public EmpresaExterna ContabilidadOriginante() {
		return null;		
	}
	
	@Override
	public IChequera chequera(){
		return null;
	}
	
	@Override
	public BigDecimal ContabilidadTotal() {
		return null;
	}
}
