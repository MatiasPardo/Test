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
import org.openxava.util.Is;
import org.openxava.validators.*;

@Entity

@Views({
	@View(members=
	"Principal{ " +
		"descripcion, moneda, cotizacion;" + 
		"Principal[#" + 
			"fecha, fechaVencimiento, fechaServicio, fechaCreacion;" +
			"puntoVenta, tipo, estado;" + 
			"empresa, numero;" +
			"Cliente[cliente, razonSocial;" + 
				"cuit, posicionIva, tipoDocumento, listaPrecio, condicionVenta];" +
			"domicilioEntrega;" + 
			"observaciones];" +
		"Descuentos[#" +
			"porcentajeDescuento, porcentajeFinanciero];" +	
		"items;" +
		"Totales[subtotalSinDescuento, descuento;" +
			"total;]" +
	"}" + 
	"CuentaCorriente{ctacte}"
	),
	@View(name="Simple",
			members="numero, estado")
})

@Tab(filter=EmpresaFilter.class,
	baseCondition=EmpresaFilter.BASECONDITION,
	properties="fecha, numero, tipo.tipo, estado, tipoOperacion, cae, fechaVencimientoCAE, cliente.codigo, cliente.nombre, total, subtotal, iva, descuento, subtotalSinDescuento",
	defaultOrder="${fechaCreacion} desc")


public class CreditoInternoVenta extends VentaElectronica implements ITransaccionInventario, IVentaInventario{

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
		return "CREDITO INTERNO";
	}
			
	@Override
	public String descripcionTipoTransaccion() {
		return "Crédito de Venta Interno";
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
	public Boolean numeraSistema(){
		return true;
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
		else{
			return super.establecerEstrategiaCancelacionPendiente();
		}
	}
	
	@Override
	public boolean generadaPorDiferenciaCambio(){
		return this.getDiferenciaCambio();
	}
	
	public Boolean calculaImpuestos(){
		return Boolean.FALSE;
	}
	
	@Transient
	private CreditoVenta creditoOrigen = null;
	
	public void generadaPorCredito(CreditoVenta credito){
		this.creditoOrigen = credito;
		this.setIdObjetoAsociado(credito.getId());
	}
	
	@Override
	public IResponsableCuentaCorriente CtaCteResponsable() {
		if (this.creditoOrigen != null){
			return this.creditoOrigen.CtaCteResponsable();
		}
		else{
			return super.CtaCteResponsable();
		}
	}
	
	@Override
	public Integer CtaCteCoeficiente() {
		return -1;
	}
		
	@Override
	public void generarTransaccionIntercompany(BigDecimal porcentaje){
		if ((porcentaje.compareTo(BigDecimal.ZERO) > 0) &&
				(porcentaje.compareTo(new BigDecimal(100)) <= 0)){
			BigDecimal descuentoFinanciero = (new BigDecimal(100)).subtract(porcentaje);
			VentaElectronica debitoInterno = this.existeDebitoGeneradoPorIntercompany();
			if (debitoInterno == null){
				crearDebitoPorIntercompany(descuentoFinanciero);
			}
			else{
				if (debitoInterno.getPorcentajeFinanciero().compareTo(descuentoFinanciero) != 0){
					throw new ValidationException("Existe un débito generado");
				}
			}
			if (!existeCreditoIntercompany()){
				crearCreditoIntercompany(descuentoFinanciero);
			}
			else{
				throw new ValidationException("Ya se generó una factura intercompany");
			}
		}
		else{
			throw new ValidationException("Porcentaje debe ser entre 0 y 100");
		}
	}
	
	private VentaElectronica existeDebitoGeneradoPorIntercompany(){
		Query query = XPersistence.getManager().createQuery("from DebitoInternoVenta where idObjetoAsociado = :id and estado not in (2, 4)");
		query.setParameter("id", this.getId());
		query.setMaxResults(1);
		List<?> results = query.getResultList();
		if (!results.isEmpty()){
			return (VentaElectronica)results.get(0);
		}
		else{
			return null;
		}
	}
	
	private void crearDebitoPorIntercompany(BigDecimal porcentajeDescuentoVenta) {
		try {
			DebitoInternoVenta trAnula = (DebitoInternoVenta)new DebitoInternoVenta();
			trAnula.copiarPropiedades(this);
			trAnula.setEstado(Estado.Borrador);
			trAnula.setItems(new LinkedList<ItemVentaElectronica>());
			trAnula.setIdObjetoAsociado(this.getId());
			trAnula.setIntercompany(Boolean.TRUE);
			XPersistence.getManager().persist(trAnula);
			
			trAnula.setPorcentajeFinanciero(porcentajeDescuentoVenta);	
			for (ItemVentaElectronica item: this.getItems()){
				ItemVentaElectronica itemTrAnula = new ItemVentaElectronica();
				itemTrAnula.copiarPropiedades(item);
				itemTrAnula.setVenta(trAnula);				
				trAnula.getItems().add(itemTrAnula);
				itemTrAnula.recalcular();
				XPersistence.getManager().persist(itemTrAnula);
			}
			trAnula.confirmarTransaccion();
			
			List<CuentaCorriente> comprobantesCuentaCorriente = new LinkedList<CuentaCorriente>();
			List<Imputacion> imputaciones = new LinkedList<Imputacion>();
			comprobantesCuentaCorriente.add(this.comprobanteCuentaCorriente());
			comprobantesCuentaCorriente.add(trAnula.comprobanteCuentaCorriente());
			Imputacion.imputarComprobantes(comprobantesCuentaCorriente, imputaciones);
						
			for(Imputacion imputacion: imputaciones){
				imputacion.asignarGeneradaPor(this);
			}
		} catch (Exception e) {
			String msj = "Error al crear débito interno venta: ";
			if (e.getMessage() != null){
				msj += e.getMessage();
			}
			throw new ValidationException(msj);
		}
	}
	
	private boolean existeCreditoIntercompany(){
		Query query = XPersistence.getManager().createQuery("from CreditoVenta where idObjetoAsociado = :id and estado not in (2, 4)");
		query.setParameter("id", this.getId());
		query.setMaxResults(1);
		return !query.getResultList().isEmpty();
	}
	
	private void crearCreditoIntercompany(BigDecimal porcentajeDescuentoVenta){
		try {
			CreditoVenta credito = (CreditoVenta)new CreditoVenta();
			this.inicializarTrCreadaPorWorkFlow(credito);
			credito.setEmpresa(null);
			
			credito.setItems(new LinkedList<ItemVentaElectronica>());		
			XPersistence.getManager().persist(credito);
			// para sincronizar las cotizaciones
			credito.grabarTransaccion(); 
			
			credito.setPorcentajeFinanciero(porcentajeDescuentoVenta);
			credito.setIdObjetoAsociado(this.getId());
			credito.setIntercompany(Boolean.TRUE);
			for (ItemVentaElectronica item: this.getItems()){
				ItemVentaElectronica itemCredito = new ItemVentaElectronica();
				itemCredito.copiarPropiedades(item);
				itemCredito.setVenta(credito);
				itemCredito.setPrecioUnitario(Transaccion.convertirMoneda(this, credito, item.getPrecioUnitario()));
				credito.getItems().add(itemCredito);
				itemCredito.recalcular();
				
				if (itemCredito.getTasaiva().compareTo(BigDecimal.ZERO) > 0){
					// se actualiza el precio unitario, de tal forma que con el cálculo de iva, de el mismo importe que antes
					BigDecimal precioUnitario = itemCredito.descontarIva(itemCredito.getPrecioUnitario());
					itemCredito.setPrecioUnitario(precioUnitario);
					itemCredito.recalcular();
				}
				
				XPersistence.getManager().persist(itemCredito);
			}
			credito.grabarTransaccion();
			
		} catch (Exception e) {
			String msj = "Error al crear crédito de venta intercompany: ";
			if (e.getMessage() != null){
				msj += e.getMessage();
			}
			throw new ValidationException(msj);
		}
	}

	@Override
	protected Class<?> tipoTransaccionRevierte() {
		return DebitoInternoVenta.class;
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

	@Override
	public ArrayList<IItemMovimientoInventario> movimientosInventario() {
		FacturaVentaContado facturaContado = revierteFacturaContado();
		if (facturaContado != null){
			ArrayList<IItemMovimientoInventario> items = new ArrayList<IItemMovimientoInventario>();
			items.addAll(this.getItems());
			return items;
		}
		else{
			return null;
		}
	}

	@Override
	public boolean revierteInventarioAlAnular() {
		return true;
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
	protected void posConfirmarTransaccion(){
		super.posConfirmarTransaccion();
		
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
}
