package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.compras.model.*;
import org.openxava.contabilidad.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(members=
		"Principal{" + 
				"descripcion, fechaCreacion, usuario;" +				
				"fecha, numero, estado;" +
				"empresa;" +
				"cheque;" + 				
				"observaciones;}" + 
		"Trazabilidad{trazabilidad}"
	),
	@View(name="Simple", members="numero")
})

@Tabs({
	@Tab(
		properties="empresa.nombre, fecha, numero, estado, cheque.numero, cheque.importe, observaciones, cliente.nombre, proveedor.nombre, cuentaBancaria.nombre",
		filter=EmpresaFilter.class,		
		baseCondition=EmpresaFilter.BASECONDITION,
		defaultOrder="${fechaCreacion} desc")
})

public class RechazoChequeTercero extends Transaccion implements ITransaccionValores, ITransaccionContable, IItemMovimientoValores{

	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("RechazoChequeTercero")
	@NoCreate @NoModify	
	@SearchAction("ReferenciasRechazoValoresTercero.buscarCheque")	
	private Valor cheque;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private Cliente cliente;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="codigo, nombre")
	private CuentaBancaria cuentaBancaria;
	
	@ReadOnly
	@ReferenceView("Simple")
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private Proveedor proveedor;
	
	public Valor getCheque() {
		return cheque;
	}

	public void setCheque(Valor cheque) {
		this.cheque = cheque;
	}
	
	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public CuentaBancaria getCuentaBancaria() {
		return cuentaBancaria;
	}

	public void setCuentaBancaria(CuentaBancaria cuentaBancaria) {
		this.cuentaBancaria = cuentaBancaria;
	}

	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}

	@Override
	public String descripcionTipoTransaccion() {
		return "Rechazo de Cheque";
	}

	@Override
	public void generadorPasesContable(Collection<IGeneradorItemContable> items) {		
		Producto conceptoChRechazado = this.conceptoChequeRechazado();
		CuentaContable cuenta = TipoCuentaContable.Ventas.CuentaContablePorTipo(conceptoChRechazado);
		GeneradorItemContablePorTr paseDebe = new GeneradorItemContablePorTr(this, cuenta);
		paseDebe.setDebe(this.getCheque().getImporte());
		items.add(paseDebe);
		
		if (this.rechazadoPorBanco()){
			cuenta = TipoCuentaContable.Finanzas.CuentaContablePorTipo(this.getCuentaBancaria());
			GeneradorItemContablePorTr paseHaber = new GeneradorItemContablePorTr(this, cuenta);
			paseHaber.setHaber(this.getCheque().getImporte());
			items.add(paseHaber);
		}
		else{
			// rechazado por Proveedor
			cuenta = TipoCuentaContable.Compras.CuentaContablePorTipo(conceptoChRechazado);
			GeneradorItemContablePorTr paseHaber = new GeneradorItemContablePorTr(this, cuenta);
			paseHaber.setHaber(this.getCheque().getImporte());
			items.add(paseHaber);
		}
	}

	@Override
	public void movimientosValores(List<IItemMovimientoValores> lista) {
		lista.add(this);
		if (this.rechazadoPorBanco()){
			ItemTransEfectivo itemMovEfectivo = new ItemTransEfectivo();
			itemMovEfectivo.setTipoValorEfectivo(this.getTipoValor().consolidaCon(this.getCuentaBancaria()));
			// en negativo, se debita el cheque de la cuenta.
			itemMovEfectivo.setImporteEfectivo(this.importeOriginalValores().negate());
			itemMovEfectivo.setTesoreria(this.getCuentaBancaria());
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
		return this.getCliente();
	}
	
	private boolean rechazadoPorBanco(){
		boolean rechazado = false;
		if (this.getCheque() != null){
			if (this.getCheque().getProveedor() == null){
				rechazado = true;
			}
			return rechazado;
		}
		else{
			throw new ValidationException("Cheque no asignado");
		}		
	}
		
	public Producto conceptoChequeRechazado(){
		if (this.getCheque() != null){
			Producto concepto = this.getCheque().getTipoValor().getConceptoChequeRechazado();
			if (concepto != null){
				return concepto;
			}
			else{
				throw new ValidationException("Debe definir un concepto de cheque rechazado en el tipo de valor " + this.getCheque().getTipoValor());
			}
		}
		else{
			return null;
		}
	}

	@Override
	public TipoValorConfiguracion getTipoValor() {
		return this.getCheque().getTipoValor();
	}

	@Override
	public Banco getBanco() {
		return this.getCheque().getBanco();
	}

	@Override
	public void setBanco(Banco banco) {
	}

	@Override
	public String getDetalle() {
		return this.getObservaciones();
	}

	@Override
	public void setDetalle(String detalle) {	
	}

	@Override
	public Date getFechaEmision() {
		return this.getCheque().getFechaEmision();
	}

	@Override
	public void setFechaEmision(Date fechaEmision) {	
	}

	@Override
	@Hidden
	public Date getFechaVencimiento() {
		return this.getCheque().getFechaVencimiento();
	}

	@Override
	public void setFechaVencimiento(Date fechaVencimiento) {	
	}

	@Override
	@Hidden
	public String getFirmante() {
		return this.getCheque().getFirmante();
	}

	@Override
	@Hidden
	public String getCuitFirmante() {
		return this.getCheque().getCuitFirmante();
	}

	@Override
	@Hidden
	public String getNroCuentaFirmante() {
		return this.getCheque().getCuitFirmante();
	}

	@Override
	public Tesoreria tesoreriaAfectada() {
		return this.getCheque().getTesoreria();
	}

	@Override
	public Tesoreria transfiere() {
		return null;
	}

	@Override
	public Valor referenciaValor() {
		return this.getCheque();
	}

	@Override
	public void asignarReferenciaValor(Valor valor) {	
	}

	@Override
	public BigDecimal importeOriginalValores() {
		if (this.rechazadoPorBanco()){
			return this.getCheque().getImporte();
		}
		else{		
			return BigDecimal.ZERO;
		}
	}

	@Override
	public BigDecimal importeMonedaTrValores(Transaccion transaccion) {
		if (this.rechazadoPorBanco()){
			if (this.getMoneda().equals(this.getCheque().getMoneda())){
				return this.getCheque().getImporte();
			}
			else{
				BigDecimal cotizacion = this.buscarCotizacionTrConRespectoA(this.getCheque().getMoneda());
				return this.getCheque().getImporte().divide(cotizacion, 2, RoundingMode.HALF_EVEN);				
			}
		}
		else{
			return BigDecimal.ZERO;
		}
	}

	@Override
	public TipoMovimientoValores tipoMovimientoValores(boolean reversion) {
		if (!reversion){
			return new TipoMovRechazoChTercero();
		}
		else{
			return new TipoMovAnulacionRechazaChTercero(); 
		}
	}

	@Override
	public void asignarOperadorComercial(Valor valor, Transaccion transaccion) {
	}

	@Override
	public OperadorComercial operadorComercialValores(Transaccion transaccion) {
		return this.getCliente();
	}

	@Override
	public ConceptoTesoreria conceptoTesoreria() {
		return null;
	}

	@Override
	public boolean noGenerarDetalle() {
		if (this.rechazadoPorBanco()){
			return true;
		}
		else{
			// cuando es rechazado porque se entregó a proveedor, se genera un detalle con importe en $0 para el seguimiento de valores
			return false;
		}
	}

	@Override
	public ObjetoNegocio itemTrValores() {
		return this;
	}
	
	@Override
	public void recalcularTotales(){
		super.recalcularTotales();
		
		// se sincroniza los datos del cheque
		if (this.getCheque() != null){
			this.setCliente(this.getCheque().getCliente());
			this.setProveedor(this.getCheque().getProveedor());
			if (this.getCheque().getProveedor() == null){
				CuentaBancaria cta = XPersistence.getManager().find(CuentaBancaria.class, this.getCheque().getTesoreria().getId());
				this.setCuentaBancaria(cta);
			}			
		}
	}
	
	@Override
	protected void validacionesPreConfirmarTransaccion(Messages errores){
		if (this.getCheque().getCliente() == null){
			errores.add("El cheque no tiene como origen un Cliente");
		}
		if (!this.getCheque().getEstado().equals(EstadoValor.Historico)){
			errores.add("El cheque esta en estado " + this.getCheque().getEstado().toString());
		}
	}
	
	// WORKFLOW DESTINO
	// 1) Debito venta para cliente
	// 2) Debito compra interno a proveedor
	@Override
	public void tipoTrsDestino(Collection<Class<?>> tipoTrsDestino){
		tipoTrsDestino.add(DebitoVenta.class);
		tipoTrsDestino.add(DebitoInternoCompra.class);
	}
	
	@Override
	protected boolean cumpleCondicionGeneracionPendiente(Class<?> tipoTrDestino){	
		if (DebitoInternoCompra.class.equals(tipoTrDestino)){
			if (this.getProveedor() == null){
				return false;
			}
			else{
				return super.cumpleCondicionGeneracionPendiente(tipoTrDestino);
			}
		}
		else{
			return super.cumpleCondicionGeneracionPendiente(tipoTrDestino);
		}
	}
	
	@Override
	public void getTransaccionesGeneradas(Collection<Transaccion> trs){
		if (this.cerrado()){ 
			Query query = XPersistence.getManager().createQuery("from PendienteDebitoVenta where rechazoCheque.id = :id");
			query.setParameter("id", this.getId());
			query.setMaxResults(1);
			List<?> result = query.getResultList();
			if (!result.isEmpty()){
				PendienteDebitoVenta pendiente = (PendienteDebitoVenta)result.get(0);
				if (!Is.emptyString(pendiente.getIdDebitoVenta())){
					try{
						Transaccion debito = (Transaccion)XPersistence.getManager().find(DebitoVenta.class, pendiente.getIdDebitoVenta());
						if (debito != null){
							trs.add(debito);
						}
					}
					catch(Exception e){
						// puede no existir, porque el usuario proceso el pendiente pero después borró la transacción.
					}
				}
			}
			
			query = XPersistence.getManager().createQuery("from PendienteDebitoInternoCompra where rechazoCheque.id = :id");
			query.setParameter("id", this.getId());
			query.setMaxResults(1);
			result = query.getResultList();
			if (!result.isEmpty()){
				PendienteDebitoInternoCompra pendiente = (PendienteDebitoInternoCompra)result.get(0);
				if (!Is.emptyString(pendiente.getIdDebitoCompra())){
					try{
						Transaccion debito = (Transaccion)XPersistence.getManager().find(DebitoInternoCompra.class, pendiente.getIdDebitoCompra());
						if (debito != null){
							trs.add(debito);
						}
					}
					catch(Exception e){
						// puede no existir, porque el usuario proceso el pendiente pero después borró la transacción.
					}
				}
			}
		}
	}
	
	protected void pasajeAtributosWorkFlowSinItemsPrePersist(Transaccion destino, List<Pendiente> pendientes){
		if (destino.getClass().equals(DebitoVenta.class)){						
			RechazoChequeTercero rechazo = ((PendienteDebitoVenta)pendientes.get(0)).getRechazoCheque();
			DebitoVenta debito = (DebitoVenta) destino;
			debito.setDomicilioEntrega(rechazo.getCliente().getDomicilio());
		}
	}
	
	protected void pasajeAtributosWorkFlowSinItemsPosPersist(Transaccion destino, List<Pendiente> pendientes){
		if (destino.getClass().equals(DebitoVenta.class)){
			DebitoVenta debito = (DebitoVenta)destino;
			debito.setItems(new LinkedList<ItemVentaElectronica>());
			
			for(Pendiente pendiente: pendientes){
				// se asocia el pendiente al debito generado para tener trazabilidad
				((PendienteDebitoVenta)pendiente).setIdDebitoVenta(debito.getId());	
				RechazoChequeTercero rechazo = ((PendienteDebitoVenta)pendiente).getRechazoCheque();
				ItemVentaElectronica item = new ItemVentaElectronica();
				item.setVenta(debito);
				debito.getItems().add(item);
				
				item.setProducto(rechazo.conceptoChequeRechazado());
				item.setUnidadMedida(item.getProducto().getUnidadMedida());
				item.setCantidad(new BigDecimal(1));
				
				item.setPrecioUnitario(this.convertirImporteChequeAMonedaTr(debito, rechazo.getCheque()));
				
				item.recalcular();
				XPersistence.getManager().persist(item);
			}
		}
		else if (destino.getClass().equals(DebitoInternoCompra.class)){
			DebitoInternoCompra debito = (DebitoInternoCompra)destino;
			debito.setItems(new LinkedList<ItemCompraElectronica>());
			
			for (Pendiente pendiente: pendientes){
				// se asocia el pendiente al debito generado para tener trazabilidad
				((PendienteDebitoInternoCompra)pendiente).setIdDebitoCompra(debito.getId());	
				RechazoChequeTercero rechazo = ((PendienteDebitoInternoCompra)pendiente).getRechazoCheque();
				
				ItemCompraElectronica item = new ItemCompraElectronica();
				item.setCompra(debito);
				debito.getItems().add(item);
				
				item.setProducto(rechazo.conceptoChequeRechazado());
				item.setUnidadMedida(item.getProducto().getUnidadMedida());
				item.setCantidad(new BigDecimal(1));
				item.setPrecioUnitario(this.convertirImporteChequeAMonedaTr(debito, rechazo.getCheque()));
				
				item.recalcular();
				XPersistence.getManager().persist(item);
			}
		}
	}
	
	private BigDecimal convertirImporteChequeAMonedaTr(Transaccion tr, Valor valor){		
		BigDecimal cotizacion = tr.buscarCotizacionTrConRespectoA(valor.getMoneda());
		return valor.getImporte().divide(cotizacion, 2, RoundingMode.HALF_EVEN);
	}
		
	@Override
	@Hidden
	public String getNumeroValor(){
		if (this.getCheque() != null){
			return this.getCheque().getNumero();
		}
		else{
			return null;
		}
	}
	
	@Override
	public void setNumeroValor(String numeroValor){		
	}
	
	@Override
	@Hidden
	public Sucursal getSucursalDestino() {
		return null;
	}
	
	@Override
	public EmpresaExterna ContabilidadOriginante() {
		EmpresaExterna empresaExterna = null;
		if (this.getCliente() != null){
			empresaExterna = XPersistence.getManager().find(EmpresaExterna.class, this.getCliente().getId());
		}
		return empresaExterna;		
	}
	
	@Override
	public IChequera chequera(){
		return null;
	}
	
	@Override
	public BigDecimal ContabilidadTotal() {
		// TODO Auto-generated method stub
		return null;
	}
}
