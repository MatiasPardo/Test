package org.openxava.ventas.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.cuentacorriente.model.*;
import org.openxava.inventario.model.*;
import org.openxava.jpa.*;
import org.openxava.util.*;
import org.openxava.validators.*;

@Entity

@Views({
	@View(name="Simple",
			members="numero, estado"),
	@View(members=
	"Principal{ Principal[#" + 
			"descripcion, moneda, cotizacion;" +
			"fecha, fechaVencimiento, fechaServicio, fechaCreacion;" +
			"puntoVenta, tipo, estado;" + 
			"numero, cae, fechaVencimientoCAE;" +
			"Cliente[cliente, razonSocial;" + 
				"cuit, posicionIva, tipoDocumento, listaPrecio, condicionVenta];" +
			"domicilioEntrega;" + 
			"observaciones];" + 
	"Descuentos[#" +
			"porcentajeDescuento, porcentajeFinanciero];" +
	"items; " + 
	"subtotalSinDescuento;" +
	"descuento;" + 		
	"subtotal;" + 
	"iva, percepcion1, percepcion2, impuestosInternos;" + 
	"total;}" + 
	"CuentaCorriente{ctacte} Trazabilidad{trazabilidad}"
	),
	@View(name="Reversion", 
		members=
			"Principal{ Principal[#" + 
					"descripcion, fechaCreacion, moneda, cotizacion;" +
					"fecha, fechaVencimiento, fechaServicio, fechaCreacion;" +
					"puntoVenta, tipo, estado;" +
					"numero, cae, fechaVencimientoCAE;" +
					"Cliente[cliente, razonSocial;" + 
						"cuit, posicionIva, tipoDocumento, condicionVenta];" +
					"observaciones;" +
					"revierte];" + 					 
			"Descuentos[#" +
					"porcentajeDescuento, porcentajeFinanciero];" +
			"items; " + 
			"subtotal, iva, percepcion1, percepcion2, impuestosInternos;" + 
			"total;}" + 
			"CuentaCorriente{ctacte} Trazabilidad{trazabilidad}")
})

@Tab(filter=EmpresaFilter.class,
	baseCondition=EmpresaFilter.BASECONDITION,
	properties="fecha, numero, tipo.tipo, estado, tipoOperacion, cae, fechaVencimientoCAE, cliente.codigo, cliente.nombre, total, subtotal, iva, descuento, subtotalSinDescuento",
	defaultOrder="${fechaCreacion} desc")

public class CreditoVenta extends VentaElectronica implements ITransaccionInventario, IVentaInventario{
	
	@ReadOnly
	private Boolean diferenciaCambio = Boolean.FALSE;
	
	public Boolean getDiferenciaCambio() {
		return diferenciaCambio;
	}

	public void setDiferenciaCambio(Boolean diferenciaCambio) {
		this.diferenciaCambio = diferenciaCambio;
	}

	@Override
	public void onPreCreate(){
		super.onPreCreate();
		this.setTipoOperacion("Credito");
	}
	
	@Override
	public String CtaCteTipo(){
		return "CREDITO";
	}
		
	@Override
	public String descripcionTipoTransaccion() {
		return "Crédito de Venta";
	}
	
	@Override
	public BigDecimal CtaCteImporte() {
		return this.getTotal().negate();
	}
	
	@Override
	public BigDecimal CtaCteNeto() {
		return this.getSubtotal().negate();
	}
	
	@Override
	public boolean generadaPorDiferenciaCambio(){
		return this.getDiferenciaCambio();
	}
		
	@Override
	protected IEstrategiaCancelacionPendiente establecerEstrategiaCancelacionPendiente(){
		if (this.getDiferenciaCambio()){
			EstrategiaCancelacionPendientePorUso estrategia = new EstrategiaCancelacionPendientePorUso();
			for(ItemVentaElectronica item: this.getItems()){
				estrategia.getPendientes().add(item.generadoPorDiferenciaCambio());
			}
			return estrategia;
		}
		else if (Is.equalAsString(this.getTipoEntidadCreadaPor(), IngresoPorDevolucion.class.getSimpleName())){
			EstrategiaCancelacionPendientePorUso estrategia = new EstrategiaCancelacionPendientePorUso();			
			estrategia.getPendientes().add(this.buscarPendienteCreditoVenta());
			return estrategia;
		}
		else{
			return super.establecerEstrategiaCancelacionPendiente();
		}
	}
	
	@Override
	public Integer AfipTipoComprobante(){
		return this.getTipo().codigoFiscal("CreditoVenta");
		
		/*if (this.getTipo().equals(TipoComprobanteAfip.A)){
			return 3;	
		}
		else if (this.getTipo().equals(TipoComprobanteAfip.B)){
			return 8;
		}
		else if (this.getTipo().equals(TipoComprobanteAfip.E)){
			return 21;
		}
		else if (this.getTipo().equals(TipoComprobanteAfip.C)){
			return 13;
		}
		else{
		    throw new ValidationException("Tipo de comprobante AFIP Incorrecto");
		}*/
	}
	
	@Override
	public boolean debeAutorizaAfip(){
		return true;
	}
	
	@Override
	public String viewName(){
		if (this.revierteTransaccion()){
			return "Reversion";			
		}
		else{
			return super.viewName();
		}
	}
	
	@Override
	public void asignarPrecioUnitario(ItemVentaElectronica item){
		boolean precioDefault = true;
		if (this.getCliente() != null){
			if (Is.equalAsString(this.getTipoEntidadCreadaPor(), IngresoPorDevolucion.class.getSimpleName())){
				BigDecimal precio = this.buscarPrecioHistorico(item);
				if (precio.compareTo(BigDecimal.ZERO) != 0){
					item.setPrecioUnitario(precio);
					precioDefault = false;
				}
			}
		}
		
		if (precioDefault){
			super.asignarPrecioUnitario(item);
		}				
	}
	
	@Override
	protected void posConfirmarTransaccion(){
		super.posConfirmarTransaccion();
		if (this.debeGenerarDescuentoFinanciero()){
			this.crearDescuentoCredito();
		}
		
		FacturaVentaContado facturaContado = revierteFacturaContado(); 
		if (facturaContado != null){
			if (this.devolucionFacturaContado()){
				facturaContado.asignarSubEstadoCreditoPorDevolucion();
			}
			else{
				facturaContado.anularReciboContado();
				facturaContado.asignarSubEstadoCredito();
			}
			Trazabilidad.crearTrazabilidad(facturaContado, FacturaVentaContado.class.getSimpleName(), this, this.getClass().getSimpleName());
		}		
	}
	
	private boolean devolucionFacturaContado(){
		if (Is.equalAsString(this.getTipoEntidadCreadaPor(), DevolucionFacturaContado.class.getSimpleName())){
			return true;
		}
		else{
			return false;
		}
	}
	
	public void crearDescuentoCredito(){
		try {
			CreditoInternoVenta creditoInterno = new CreditoInternoVenta();
			creditoInterno.copiarPropiedades(this);
			creditoInterno.setEmpresa(null);
			creditoInterno.setEstado(Estado.Borrador);
			creditoInterno.asignarNumeracion(this.getNumero(), this.getNumeroInterno());
			creditoInterno.setItems(new LinkedList<ItemVentaElectronica>());
			creditoInterno.generadaPorCredito(this);
			XPersistence.getManager().persist(creditoInterno);
			
			BigDecimal porcentajeDescuentoCredito = (new BigDecimal(100)).subtract(this.getPorcentajeFinanciero());
			creditoInterno.setPorcentajeFinanciero(porcentajeDescuentoCredito);	
			for (ItemVentaElectronica item: this.getItems()){
				ItemVentaElectronica itemCreditoInterno = new ItemVentaElectronica();
				itemCreditoInterno.copiarPropiedades(item);
				itemCreditoInterno.setVenta(creditoInterno);				
				creditoInterno.getItems().add(itemCreditoInterno);
				itemCreditoInterno.recalcular();
				XPersistence.getManager().persist(itemCreditoInterno);
			}
			creditoInterno.confirmarTransaccion();
			
			// Trazabilidad entre factura de venta y la factura manual
			Trazabilidad.crearTrazabilidad(this, this.getClass().getSimpleName(), creditoInterno, creditoInterno.getClass().getSimpleName());
		} catch (Exception e) {
			String msj = "Error al procesar descuento de venta: ";
			if (e.getMessage() != null){
				msj += e.getMessage();
			}
			throw new ValidationException(msj);
		}
	}
	
	@Override
	public void ejecutarAccionesPosCommitAutorizaciones(){
		super.ejecutarAccionesPosCommitAutorizaciones();
		
		if ((this.getRevierte() != null) && (this.debeGenerarDescuentoFinanciero())){
						
			// se imputan el creditos y facturas generadas por descuentos financieros
			try{
				VentaElectronica creditoInterno = this.buscarPrimerObjetoAsociado("CreditoInternoVenta", this.getId());
				VentaElectronica facturaInterna = this.buscarPrimerObjetoAsociado("VentaElectronica", this.getRevierte().getId());
						
				List<CuentaCorriente> comprobantesCuentaCorriente = new LinkedList<CuentaCorriente>();
				comprobantesCuentaCorriente.add(facturaInterna.comprobanteCuentaCorriente());
				comprobantesCuentaCorriente.add(creditoInterno.comprobanteCuentaCorriente());
				List<Imputacion> imputaciones = new LinkedList<Imputacion>();		
				Imputacion.imputarComprobantes(comprobantesCuentaCorriente, imputaciones);
							
				for(Imputacion imputacion: imputaciones){
					imputacion.asignarGeneradaPor(creditoInterno);
				}
			}
			catch(Exception e){
				throw new ValidationException("No se pudieron imputar los descuentos financieros: " + e.toString() );
			}			
		}
	}
	
	@Override
	public Integer CtaCteCoeficiente() {
		return -1;
	}
	
	private PendienteCreditoVenta buscarPendienteCreditoVenta(){
		Query query = XPersistence.getManager().createQuery("from PendienteCreditoVenta p where p.idTrOrigen = :id");
		query.setParameter("id", this.getIdCreadaPor());
		query.setMaxResults(1);
		List<?> result = query.getResultList();
		if (!result.isEmpty()){
			return (PendienteCreditoVenta)result.get(0);
		}
		else{
			return null;
		}
	}

	@Override
	protected Class<?> tipoTransaccionRevierte() {
		return DebitoVenta.class;
	}
	
	@Override
	public ArrayList<IItemMovimientoInventario> movimientosInventario() {
		FacturaVentaContado facturaContado = revierteFacturaContado();
		if (facturaContado != null){
			try{
				facturaContado.tipoMovimientoInventario(true);
				ArrayList<IItemMovimientoInventario> items = new ArrayList<IItemMovimientoInventario>();
				items.addAll(this.getItems());
				return items;
			}
			catch(Exception e){
				return null;
			}
		}
		else{
			return null;
		}
	}
	
	private FacturaVentaContado revierteFacturaContado(){
		if (this.revierteTransaccion()){
			return XPersistence.getManager().find(FacturaVentaContado.class, this.getRevierte().getId());
		}
		else{
			return null;
		}
	}
	
	@Override
	public boolean revierteInventarioAlAnular() {
		return true;
	}
	
	@Override
	public ITipoMovimientoInventario tipoMovimientoInventario(boolean reversion) {
		FacturaVentaContado facturaContado = revierteFacturaContado();
		if (facturaContado != null){
			// Hace lo contrario a la factura de venta contado
			return facturaContado.tipoMovimientoInventario(!reversion);
		}
		else{
			throw new ValidationException("Este comprobante no afecta stock");
		}
	}
	
	@Override
	@Hidden
	public Deposito getDeposito() {
		FacturaVentaContado factura = this.revierteFacturaContado();
		if (factura != null){
			if (this.getSucursal() != null){
				Deposito dep = this.getSucursal().depositoPrincipal();
				if (dep == null){
					throw new ValidationException("No se encontró depósito principal para la sucursal " + this.getSucursal().toString());
				}
				return dep;
			}
			throw new ValidationException("El crédito no tiene sucursal asignada");
		}
		else{
			return null;
		}		
	}

	@Override
	public boolean validarStockDisponible() {
		return false;		
	}
}
