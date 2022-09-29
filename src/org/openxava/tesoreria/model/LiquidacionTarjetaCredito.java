package org.openxava.tesoreria.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.*;
import javax.validation.constraints.Min;

import org.openxava.annotations.*;
import org.openxava.base.model.EmpresaExterna;
import org.openxava.base.model.Esquema;
import org.openxava.base.model.Estado;
import org.openxava.base.model.ITransaccionValores;
import org.openxava.base.model.ObjetoNegocio;
import org.openxava.base.model.Transaccion;
import org.openxava.base.model.Trazabilidad;
import org.openxava.compras.model.DebitoCompra;
import org.openxava.compras.model.ImpuestoCompra;
import org.openxava.compras.model.ItemCompraElectronica;
import org.openxava.compras.model.Proveedor;
import org.openxava.contabilidad.model.CuentaContable;
import org.openxava.contabilidad.model.GeneradorItemContablePorTr;
import org.openxava.contabilidad.model.IGeneradorItemContable;
import org.openxava.contabilidad.model.ITransaccionContable;
import org.openxava.contabilidad.model.TipoCuentaContable;
import org.openxava.jpa.XPersistence;
import org.openxava.model.MapFacade;
import org.openxava.negocio.calculators.ObjetoPrincipalCalculator;
import org.openxava.negocio.filter.SucursalEmpresaFilter;
import org.openxava.negocio.model.OperadorComercial;
import org.openxava.negocio.model.Sucursal;
import org.openxava.util.Is;
import org.openxava.util.Messages;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.calculators.TasaIvaCalculator;

@Entity

@Views({
	@View(members=
		"Principal[#" +  
				"empresa, numero, fecha, estado;" +
				"numeroDebito, sucursalDebito, fechaRealDebito;" + 
				"sucursal, cuenta; " + 
				"observaciones];" +
		"proveedor;" + 		
		"Totales[importeCupones, importeGastos;" +
			"totalCalculado, totalLiquidacion];" +		
		"cupones;" + 		
		"items;" + 
		"impuestos;" +
		"trazabilidad;"
		),
	@View(name="Simple", members="numero, estado;")
})

@Tab(filter=SucursalEmpresaFilter.class,
	baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL,
	properties="fecha, numero, estado, proveedor.nombre, sucursal.nombre, cuenta.nombre",
	defaultOrder="${fechaCreacion} desc")


public class LiquidacionTarjetaCredito extends Transaccion implements ITransaccionValores, IItemMovimientoValores, ITransaccionContable{

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView("Simple")
	private Proveedor proveedor;
		
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private CuentaBancaria cuenta;
	
	@Required
	@Column(length=20)
	private String numeroDebito;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	@DefaultValueCalculator(value=ObjetoPrincipalCalculator.class, 
		properties={@PropertyValue(name="entidad", value="Sucursal")})
	private Sucursal sucursalDebito;
	
	@Required
	private Date fechaRealDebito;
	
	@ElementCollection
	@ListProperties("concepto.codigo, concepto.nombre, importe, tasaiva, iva")	
	private Collection<ItemLiquidacionTarjetaCredito> items;
	
	@ElementCollection
	@ListProperties("impuesto.codigo, impuesto.nombre, importe, alicuota")	
	private Collection<ImpuestoCompra> impuestos;
	
	@Condition(value="${liquidacionTarjeta.id} = ${this.id}")
	@ListAction("ColeccionCuponesTarjeta.Multiseleccion")
	@RemoveAction(value="ColeccionCuponesTarjeta.remove")
	@RemoveSelectedAction(value="ColeccionCuponesTarjeta.removeSelected")
	@NoCreate @NoModify
	@ListProperties(value="reciboCobranza.fecha, destino.nombre, tipoValor.nombre, importe, cupon, lote, detalle, fechaCreacion")
	@OrderBy("fechaCreacion asc")
	public Collection<ItemReciboCobranza> getCupones(){
		return null;
	}
	
	@Min(value=0, message="No puede ser negativo")	
	private BigDecimal totalLiquidacion = BigDecimal.ZERO;
	
	@ReadOnly 
	private BigDecimal importeCupones;
	
	@ReadOnly
	private BigDecimal importeGastos;
	
	@ReadOnly
	private BigDecimal totalCalculado;
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Liquidación Tarjeta";
	}
	
	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}

	public String getNumeroDebito() {
		return numeroDebito;
	}

	public void setNumeroDebito(String numeroDebito) {
		this.numeroDebito = numeroDebito;
	}

	@SuppressWarnings("unchecked")
	public Collection<ItemLiquidacionTarjetaCredito> getItems() {
		return items == null ? Collections.EMPTY_LIST : this.items;
	}

	public void setItems(Collection<ItemLiquidacionTarjetaCredito> items) {
		this.items = items;
	}

	public Collection<ImpuestoCompra> getImpuestos() {
		return impuestos;
	}

	public void setImpuestos(Collection<ImpuestoCompra> impuestos) {
		this.impuestos = impuestos;
	}
	
	public CuentaBancaria getCuenta() {
		return cuenta;
	}

	public void setCuenta(CuentaBancaria cuenta) {
		this.cuenta = cuenta;
	}
	
	public BigDecimal getTotalLiquidacion() {
		return totalLiquidacion == null ? BigDecimal.ZERO : totalLiquidacion;
	}

	public void setTotalLiquidacion(BigDecimal totalLiquidacion) {
		if (totalLiquidacion != null){
			this.totalLiquidacion = totalLiquidacion;
		}
	}
	
	public BigDecimal getImporteCupones() {
		return importeCupones == null ? BigDecimal.ZERO : importeCupones;
	}

	public void setImporteCupones(BigDecimal importeCupones) {
		if (importeCupones != null){
			this.importeCupones = importeCupones;
		}
	}

	public BigDecimal getImporteGastos() {
		return importeGastos == null ? BigDecimal.ZERO : importeGastos;
	}

	public void setImporteGastos(BigDecimal importeGastos) {
		if (importeGastos != null){
			this.importeGastos = importeGastos;
		}
	}

	public BigDecimal getTotalCalculado() {
		return totalCalculado == null ? BigDecimal.ZERO : totalCalculado;
	}

	public void setTotalCalculado(BigDecimal totalCalculado) {
		if (totalCalculado != null){
			this.totalCalculado = totalCalculado;
		}
	}
	
	@Override
	protected boolean agregarItemDesdeMultiseleccion(Map<?, ?> key, Map<String, Object> itemsMultiseleccion){
		try {
			ICuponTarjeta cupon = (ICuponTarjeta)MapFacade.findEntity("ItemReciboCobranza", key);
			if (cupon.getLiquidacionTarjeta() == null){
				cupon.setLiquidacionTarjeta(this);			
				return true;
			}
			else{
				throw new ValidationException("El cupon ya esta liquidado");
			}
		} catch (Exception e) {
			String error = e.getMessage();
			if (Is.emptyString(error)) error = e.toString();
			throw new ValidationException("Error al agregar cheque: " + error);
		}		
	}
	
	@Override
	public void onPreDelete(){
		super.onPreDelete();
		
		Query query = this.queryCuponesAgrupados();
		if (!query.getResultList().isEmpty()){
			throw new ValidationException("Primero debe quitar los cupones de la liquidación");
		}		
	}

	@Override
	protected void posAnularTransaccion(){
		super.posAnularTransaccion();
		
		@SuppressWarnings("unchecked")
		List<ICuponTarjeta> results = this.queryCupones().getResultList();
		for(ICuponTarjeta cupon: results){
			cupon.setLiquidacionTarjeta(null);
		}	
	}
	
	@Override
	protected void validacionesPreConfirmarTransaccion(Messages errores){
		super.validacionesPreConfirmarTransaccion(errores);
		
		if (this.queryCuponesAgrupados().getResultList().isEmpty()){
			errores.add("Sin cupones");
		}
		
		if (this.getItems().isEmpty()){
			errores.add("sin_items");
		}
	}
		
	@Override
	public void recalcularTotales(){
		super.recalcularTotales();
				
		BigDecimal importeCupones = BigDecimal.ZERO;
		
		Map<String, Map<String, BigDecimal>> importePorTarjeta = this.agruparCuponesPorTipoValor();
		for(Entry<String, Map<String, BigDecimal>> tarjeta: importePorTarjeta.entrySet()){
			for(Entry<String, BigDecimal> caja: tarjeta.getValue().entrySet()){
				importeCupones = importeCupones.add(caja.getValue());
			}
		}
		BigDecimal importeGastos = BigDecimal.ZERO;
		for(ItemLiquidacionTarjetaCredito item: this.getItems()){
			TasaIvaCalculator calculator = new TasaIvaCalculator();
			calculator.setProductoID(item.getConcepto().getId());
			try{
				item.setTasaiva((BigDecimal)calculator.calculate());
				item.setIva(item.getImporte().multiply(item.getTasaiva()).divide(new BigDecimal(100)).setScale(2, RoundingMode.HALF_EVEN));
			}
			catch(Exception e){
				throw new ValidationException("Error al buscar tasa de iva " + e.toString());
			}			
			importeGastos = importeGastos.add(item.getImporte()).add(item.getIva());
		}
		for(ImpuestoCompra impuesto: this.getImpuestos()){
			importeGastos = importeGastos.add(impuesto.getImporte());
		}
		
		this.setImporteCupones(importeCupones);
		this.setImporteGastos(importeGastos);
		this.setTotalCalculado(this.getImporteCupones().subtract(this.getImporteGastos()));
		
		if (this.getEstado().equals(Estado.Confirmada)){
			if (this.getTotalCalculado().compareTo(this.getTotalLiquidacion()) != 0){
				throw new ValidationException("No coincide el total calculado " + this.getTotalCalculado().toString() + " con el total ingresado " + this.getTotalLiquidacion().toString());
			}
		}
	}
	
	@Override
	protected void posConfirmarTransaccion(){
		super.posConfirmarTransaccion();
		
		this.generarDebitoCompra();
	}		
	
	private void generarDebitoCompra(){
		
		DebitoCompra debito = new DebitoCompra();
		debito.copiarPropiedades(this);		
		debito.setSucursal(this.getSucursalDebito());
		debito.setNumero(this.getNumeroDebito());
		debito.setFecha(this.getFecha());
		debito.setFechaReal(this.getFechaRealDebito());
		XPersistence.getManager().persist(debito);
		
		Collection<ItemCompraElectronica> itemsDebito = new LinkedList<ItemCompraElectronica>();
		for(ItemLiquidacionTarjetaCredito itemLiquidacion: this.getItems()){
			ItemCompraElectronica itemDebito = new ItemCompraElectronica();
			itemDebito.setCompra(debito);
			itemDebito.setProducto(itemLiquidacion.getConcepto());
			itemDebito.setCantidad(new BigDecimal(1));
			itemDebito.setPrecioUnitario(itemLiquidacion.getImporte());
			itemDebito.recalcular();
			itemsDebito.add(itemDebito);
		}
		debito.setItems(itemsDebito);
		
		Collection<ImpuestoCompra> impuestosDebito = new LinkedList<ImpuestoCompra>();
		for(ImpuestoCompra impuesto: this.getImpuestos()){
			ImpuestoCompra copia = new ImpuestoCompra();
			copia.setImpuesto(impuesto.getImpuesto());
			copia.setImporte(impuesto.getImporte());
			copia.setAlicuota(impuesto.getAlicuota());
			impuestosDebito.add(copia);
		}
		debito.setImpuestos(impuestosDebito);
		
		debito.notificarLiquidacionTarjeta(this);
		debito.confirmarTransaccion();
		
		BigDecimal totalDebito = debito.getTotal().setScale(2, RoundingMode.HALF_EVEN);
		BigDecimal totalDebitoLiq = this.getImporteGastos().setScale(2, RoundingMode.HALF_EVEN);
		if (totalDebito.subtract(totalDebitoLiq).abs().compareTo(new BigDecimal("0.01")) > 0){
			throw new ValidationException("El debito debe generarse por " + totalDebitoLiq.toString() + " pero se esta calculando por " + totalDebito.toString());
		}
		
		Trazabilidad.crearTrazabilidad(this, LiquidacionTarjetaCredito.class.getSimpleName(), debito, debito.getClass().getSimpleName());
	}

	@Override
	public void movimientosValores(List<IItemMovimientoValores> lista) {
		// se ingresa en el banco el total liquidado
		lista.add(this);
		
		// se egresa de la caja el total de cupones
		Map<String, Map<String, BigDecimal>> importePorTarjeta = this.agruparCuponesPorTipoValor();
		for(Entry<String, Map<String, BigDecimal>> tarjeta: importePorTarjeta.entrySet()){
			TipoValorConfiguracion tipoValorTarjeta = XPersistence.getManager().find(TipoValorConfiguracion.class, tarjeta.getKey());
			for(Entry<String, BigDecimal> caja: tarjeta.getValue().entrySet()){
				ItemTransMovValoresCustom itemMovTarjeta = new ItemTransMovValoresCustom(this);
				Caja cajaTarjeta = XPersistence.getManager().find(Caja.class, caja.getKey());
				itemMovTarjeta.setTesoreria(cajaTarjeta);
				itemMovTarjeta.setTipoValorCustom(tipoValorTarjeta);
				itemMovTarjeta.setTipo(new TipoMovEgresoValores());			
				itemMovTarjeta.setTipoReversion(new TipoMovAnulacionEgrValores());
				itemMovTarjeta.setImporteOriginal(caja.getValue());
				lista.add(itemMovTarjeta);
			}			
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
	public OperadorComercial operadorFinanciero() {
		return this.getProveedor();
	}

	@Override
	@Hidden
	public Sucursal getSucursalDestino() {
		return this.getCuenta().getSucursal();
	}

	@Override
	@Hidden
	public TipoValorConfiguracion getTipoValor() {
		return this.getCuenta().getEfectivo();		
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
		return null;
	}

	@Override
	public void asignarReferenciaValor(Valor valor) {		
	}

	@Override
	public BigDecimal importeOriginalValores() {
		return this.getTotalCalculado();
	}

	@Override
	public BigDecimal importeMonedaTrValores(Transaccion transaccion) {
		BigDecimal cotizacion = this.buscarCotizacionTrConRespectoA(this.getTipoValor().getMoneda());		
		return this.importeOriginalValores().divide(cotizacion, 2, RoundingMode.HALF_EVEN);				
	}

	@Override
	public TipoMovimientoValores tipoMovimientoValores(boolean reversion) {
		if(!reversion){
			return new TipoMovIngresoValores();
		}
		else{
			return new TipoMovEgresoValores();
		}
	}

	@Override
	public void asignarOperadorComercial(Valor valor, Transaccion transaccion) {
	}

	@Override
	public OperadorComercial operadorComercialValores(Transaccion transaccion) {
		return this.getProveedor();
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
	public void generadorPasesContable(Collection<IGeneradorItemContable> items) {
		// Debe: ingreso en cuenta bancaria
		CuentaContable cuenta = TipoCuentaContable.Finanzas.CuentaContablePorTipo(this.getCuenta());
		GeneradorItemContablePorTr paseDebe = new GeneradorItemContablePorTr(this, cuenta);
		paseDebe.setDebe(this.getImporteCupones());
		items.add(paseDebe);
		
		// Haber: salida de la caja de cupones
		Map<String, Map<String, BigDecimal>> importePorTarjeta = this.agruparCuponesPorTipoValor();
		for(Entry<String, Map<String, BigDecimal>> tarjeta: importePorTarjeta.entrySet()){
			TipoValorConfiguracion tipoTarjeta = XPersistence.getManager().find(TipoValorConfiguracion.class, tarjeta.getKey());
			cuenta = TipoCuentaContable.Finanzas.CuentaContablePorTipo(tipoTarjeta);
			GeneradorItemContablePorTr paseHaber = new GeneradorItemContablePorTr(this, cuenta);
			
			BigDecimal importe = BigDecimal.ZERO;
			for(Entry<String, BigDecimal> importesPorCaja: tarjeta.getValue().entrySet()){
				importe = importe.add(importesPorCaja.getValue());
			}			
			paseHaber.setHaber(importe);
			items.add(paseHaber);
		}
		
	}
	
	@Transient
	// El mapa es por Tipo Valor -> Caja -> Importe
	private Map<String, Map<String, BigDecimal>> cuponesPorTipoValor = null;
	
	private Map<String, Map<String, BigDecimal>> agruparCuponesPorTipoValor(){
		if (cuponesPorTipoValor == null){
			cuponesPorTipoValor = new HashMap<String, Map<String, BigDecimal>>();
			Query query = queryCuponesAgrupados();
			List<?> results = query.getResultList();
			for(Object r: results){
				Object[]result = (Object[])r;
				String idTipoValor = (String)result[0];
				Map<String, BigDecimal> cajas = null;
				if (this.cuponesPorTipoValor.containsKey(idTipoValor)){
					cajas = this.cuponesPorTipoValor.get(idTipoValor);
				}
				else{
					cajas = new HashMap<String, BigDecimal>();
					this.cuponesPorTipoValor.put(idTipoValor, cajas);
				}
				
				String idCaja = (String)result[1];
				BigDecimal importe = (BigDecimal)result[2];
				if (cajas.containsKey(idCaja)){
					cajas.replace(idCaja, cajas.get(idCaja).add(importe)); 
				}
				else{
					cajas.put(idCaja, importe);
				}				
			}
		}
		return cuponesPorTipoValor;
	}
		
	private Query queryCuponesAgrupados() {
		StringBuilder sql = new StringBuilder();
		sql.append("select tipoValor_id, destino_id, sum(importeOriginal) from ");
		sql.append(Esquema.concatenarEsquema("ItemReciboCobranza where liquidacionTarjeta_id = :liquidacion "));
		sql.append("group by tipoValor_id, destino_id");
		
		Query query = XPersistence.getManager().createNativeQuery(sql.toString());
		query.setParameter("liquidacion", this.getId());
		return query;
	}

	private Query queryCupones(){
		Query query = XPersistence.getManager().createQuery("from ItemReciboCobranza where liquidacionTarjeta = :liquidacion");
		query.setParameter("liquidacion", this);
		return query;			
	}
	
	@Override
	protected void validacionesPreAnularTransaccion(Messages errores){
		super.validacionesPreAnularTransaccion(errores);
		
		Collection<Trazabilidad> trazabilidad = new LinkedList<Trazabilidad>();
		Trazabilidad.buscarTrazabilidadPorTipo(trazabilidad, this, true, DebitoCompra.class.getSimpleName());
		for(Trazabilidad traz: trazabilidad){
			Transaccion destino = traz.trDestino();
			if (!destino.getEstado().equals(Estado.Anulada)){
				errores.add(destino.toString() + " debe estar anulada");
			}
		}
	}

	public Sucursal getSucursalDebito() {
		return sucursalDebito;
	}

	public void setSucursalDebito(Sucursal sucursalDebito) {
		this.sucursalDebito = sucursalDebito;
	}

	public Date getFechaRealDebito() {
		return fechaRealDebito;
	}

	public void setFechaRealDebito(Date fechaRealDebito) {
		this.fechaRealDebito = fechaRealDebito;
	}

	@Override
	public EmpresaExterna ContabilidadOriginante() {
		return null;
	}

	@Override
	@Hidden
	public String getNumeroValor() {
		return null;
	}

	@Override
	public void setNumeroValor(String numeroValor) {
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
