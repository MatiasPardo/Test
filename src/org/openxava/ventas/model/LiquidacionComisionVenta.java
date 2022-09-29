package org.openxava.ventas.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.calculators.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.compras.model.Proveedor;
import org.openxava.cuentacorriente.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.filter.*;

@Entity

@Views({
	@View(members=
			"Principal[#" +
					"descripcion, fechaCreacion, usuario;" +
					"empresa, moneda, cotizacion;" +
					"numero, fecha;" +
					"estado, subestado, ultimaTransicion;" +				
					"vendedor, desde, hasta;" +
					"total];" +
				"observaciones;" + 
				"cobranzas{cobranzasPendientes}facturas{facturasPendientes}" 
		),
	@View(name="Cerrado",
		members=
		"Principal{Principal[#" +
				"descripcion, fechaCreacion, usuario;" +
				"empresa, moneda, cotizacion;" +
				"fecha, numero;" +
				"estado, subestado, ultimaTransicion;" +				
				"vendedor, desde, hasta;" +
				"total];" +
				"observaciones;" + 
				"detalleLiquidacion;" + 
				
		"}" +
		"historico{historicoEstados}" + 
		"trazabilidad{trazabilidad}" 
	),
	@View(name="Simple",
		members="numero, fecha")
})

@Tab(
		filter=VentasFilter.class,
		properties="fecha, empresa.nombre, numero, estado, vendedor.nombre, desde, hasta, total, fechaCreacion, usuario",
		baseCondition="(true = ? or ${vendedor.id} = ?) and " + EmpresaFilter.BASECONDITION,
		defaultOrder="${fechaCreacion} desc")

public class LiquidacionComisionVenta extends Transaccion implements ITransaccionCtaCte{

	@SuppressWarnings("unchecked")
	public static void buscarVendedoresParaComisionar(List<Vendedor> vendedores){
		String sql = "from Vendedor where proveedorCtaCte is not null and activo = true";
		Query query = XPersistence.getManager().createQuery(sql);
		List<?> results = query.getResultList();
		vendedores.addAll((List<Vendedor>) results);
	}
	
	@DefaultValueCalculator(value=FechaInicioMesCalculator.class)
	@Required
	private Date desde;
	
	@DefaultValueCalculator(value=FechaFinMesCalculator.class)
	@Required
	private Date hasta;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	@NoFrame
	private Vendedor vendedor;
	
	@ReadOnly
	private BigDecimal total;
	
	@Condition("${fecha} >= ${this.desde} and ${fecha} <= ${this.hasta} and ${vendedor.id} = ${this.vendedor.id} and ${empresa.id} = ${this.empresa.id} and ${idLiquidacion} is null " +
			"and ${anulado} = false and ${tipoEntidad} not in ('ReciboCobranza')")
	@ListProperties("fecha, tipo, numero, cliente.codigo, cliente.nombre, comision, porcentajeComision, importe1, importe2, cotizacion")
	@OrderBy("fecha desc")
	@ReadOnly
	public Collection<CuentaCorrienteVenta> getFacturasPendientes() {
		return null;
	}
	
	@Condition("${fecha} >= ${this.desde} and ${fecha} <= ${this.hasta} and ${vendedor.id} = ${this.vendedor.id} and ${empresa.id} = ${this.empresa.id} and ${idLiquidacion} is null " +
			"and ${anulado} = false and ${tipoEntidad} in ('ReciboCobranza')")
	@ListProperties("fecha, tipo, numero, cliente.codigo, cliente.nombre, comision, porcentajeComision, importe1, importe2, cotizacion")
	@OrderBy("fecha desc")
	@ReadOnly
	public Collection<CuentaCorrienteVenta> getCobranzasPendientes() {
		return null;
	}
	 
	@Condition("${idLiquidacion} = ${this.id}")
	@ListProperties("fecha, tipo, numero, cliente.codigo, cliente.nombre, comision, porcentajeComision, importe1, importe2, cotizacion")
	@OrderBy("fecha desc")
	public Collection<CuentaCorrienteVenta> getDetalleLiquidacion() {
		return null;
	}
		
	public Date getDesde() {
		return desde;
	}

	public void setDesde(Date desde) {
		this.desde = desde;
	}

	public Date getHasta() {
		return hasta;
	}

	public void setHasta(Date hasta) {
		this.hasta = hasta;
	}

	public Vendedor getVendedor() {
		return vendedor;
	}

	public void setVendedor(Vendedor vendedor) {
		this.vendedor = vendedor;
	}

	public BigDecimal getTotal() {
		return total == null ? BigDecimal.ZERO : total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	@Override
	public String descripcionTipoTransaccion() {
		return "Comisiones";
	}

	@Override
	public Date CtaCteFecha() {
		return this.getFecha();
	}

	@Override
	public BigDecimal CtaCteImporte() {
		return this.getTotal();
	}

	@Override
	public BigDecimal CtaCteNeto() {
		return this.getTotal();
	}

	@Override
	public String CtaCteTipo() {
		return "COMISIONES";
	}

	@Override
	public OperadorComercial CtaCteOperadorComercial() {
		Proveedor proveedor = this.getVendedor().buscarProveedorCtaCte();
		if (proveedor == null){
			throw new ValidationException("El vendedor no tiene un proveedor para cuenta corriente asociado");
		}
		return proveedor;
	}

	@Override
	public IResponsableCuentaCorriente CtaCteResponsable() {
		return null;
	}

	@Override
	public Date CtaCteFechaVencimiento() {
		return this.getFecha();
	}

	@Override
	public String CtaCteNumero() {
		return this.getNumero();
	}

	@Override
	public Transaccion CtaCteTransaccion() {
		return this;
	}

	@Override
	public CuentaCorriente CtaCteNuevaCuentaCorriente() {
		return new CuentaCorrienteCompra();
	}

	@Override
	public void CtaCteReferenciarCuentaCorriente(CuentaCorriente ctacte) {		
	}

	@Override
	public boolean generadaPorDiferenciaCambio() {
		return false;
	}

	@Override
	public void detalleDiferenciaCambio(Collection<DiferenciaCambioVenta> detalleDifCambio) {		
	}

	@Override
	public void imputacionesGeneradas(Collection<Imputacion> imputacionesGeneradas) {
	}
	
	public void recalcularTotales(){
		// este recalculo se usa para que el usuario puede ver el total de la comisión antes de confirmar
		// Si la transacción se confirma, no se recalula el total, ya que la confirmación hará el cálculo
		if (!this.getEstado().equals(Estado.Confirmada)){
			this.calcularComisiones(false);
		}
	}
	
	private void calcularComisiones(boolean registrarComprobantes){
		if ((registrarComprobantes) && (this.esNuevo())){
			throw new ValidationException("Error interno: no se puede registrar los comprobantes si la liquidación es nueva");
		}
		ConfiguracionComisionesVenta configuracion = this.getEmpresa().getComisionesVenta();
		if ((configuracion == null) || (!configuracion.getActivo())){
			throw new ValidationException(this.getEmpresa().toString() + " no esta configurada para el cálculo de comisiones");
		}
		
		if (configuracion.getTipoCalculo().equals(TipoLiquidacionComisionVenta.Facturacion)){
			this.setTotal(this.calcularComisionesPorCtaCte(registrarComprobantes, true));
		}
		else if (configuracion.getTipoCalculo().equals(TipoLiquidacionComisionVenta.Cobranzas)){
			this.setTotal(this.calcularComisionesPorCtaCte(registrarComprobantes, false));
		}
	}
	
	private BigDecimal calcularComisionesPorCtaCte(boolean registrarComprobantes, boolean facturacion){
		// calculo comisiones sobre las facturas, créditos y débitos de venta
		ConfiguracionComisionesVenta configuracion = this.getEmpresa().getComisionesVenta();
		Query query = null;
		if (facturacion){
			query = this.queryFacturacionPendientes();
		}
		else{
			query = this.queryCobranzasPendientes();
		}		
		List<?> results = query.getResultList();
		BigDecimal comisiones = BigDecimal.ZERO;
		for(Object res: results){
			CuentaCorrienteVenta cc = (CuentaCorrienteVenta)res;
			BigDecimal importe = BigDecimal.ZERO;
			if (cc.getMoneda1().equals(this.getMoneda())){				
				importe = cc.getNeto1();
			}
			else if (cc.getMoneda2().equals(this.getMoneda())){
				importe = cc.getNeto2();
			}
			else{
				throw new ValidationException("No se puede calcular comisiones en moneda " + this.getMoneda().getNombre());
			}
			if (!facturacion){
				// las cobranzas siempre están en negativo en la cuenta corriente
				importe = importe.negate();
			}
			comisiones = comisiones.add(calcularComision(cc, importe, configuracion, registrarComprobantes));
		}
		comisiones = comisiones.setScale(2, RoundingMode.HALF_EVEN);
		return comisiones;
	}
	
	private Query queryFacturacionPendientes(){
		String sql = "from CuentaCorrienteVenta where " + 
				"fecha >= :desde and fecha <= :hasta and vendedor = :vendedor and empresa = :empresa and idLiquidacion is null " + 
				"and anulado = false and tipoEntidad not in ('ReciboCobranza')";
		Query query = XPersistence.getManager().createQuery(sql);
		query.setParameter("desde", this.getDesde());
		query.setParameter("hasta", this.getHasta());
		query.setParameter("empresa", this.getEmpresa());
		query.setParameter("vendedor", this.getVendedor());
		return query;
	}
	
	private Query queryCobranzasPendientes(){
		String sql = "from CuentaCorrienteVenta where " + 
				"fecha >= :desde and fecha <= :hasta and vendedor = :vendedor and empresa = :empresa and idLiquidacion is null " + 
				"and anulado = false and tipoEntidad in ('ReciboCobranza')";
		Query query = XPersistence.getManager().createQuery(sql);
		query.setParameter("desde", this.getDesde());
		query.setParameter("hasta", this.getHasta());
		query.setParameter("empresa", this.getEmpresa());
		query.setParameter("vendedor", this.getVendedor());
		return query;
	}
	
	private BigDecimal calcularComision(CuentaCorrienteVenta comprobante, BigDecimal importe, ConfiguracionComisionesVenta config, boolean registrar){
		BigDecimal porcentaje = BigDecimal.ZERO;
		if (config.getTipoPorcentaje().equals(TipoPorcentajeComisionVenta.Vendedor)){
			porcentaje = this.getVendedor().getPorcentajeComisiones();
		}
		else if (config.getTipoPorcentaje().equals(TipoPorcentajeComisionVenta.Cliente)){
			porcentaje = comprobante.getCliente().getPorcentajeComisiones();
		}
		BigDecimal comision = importe.multiply(porcentaje).divide(new BigDecimal(100));
		
		comprobante.setPorcentajeComision(porcentaje);
		comprobante.setComision(comision);		
		if (registrar){
			comprobante.setIdLiquidacion(this.getId());			
		}
		return comision;
	}
	
	@Override
	public String viewName(){
		if (this.cerrado()){
			return "Cerrado";
		}
		else{
			return super.viewName();
		}	
	}
	
	protected void preConfirmarTransaccion(){
		super.preConfirmarTransaccion();
		
		// Bloquear vendedor
		this.bloquearVendedorParaActualizarComisiones();		
		this.calcularComisiones(true);
	}
	
	protected void posAnularTransaccion(){
		super.posAnularTransaccion();
		
		this.anularComisiones();
	}
	
	private void anularComisiones(){
		String sql = "from CuentaCorrienteVenta where idLiquidacion = :id";				
		Query query = XPersistence.getManager().createQuery(sql);
		query.setParameter("id", this.getId());
		
		List<?> results = query.getResultList();
		for(Object res: results){
			CuentaCorrienteVenta ctacte = (CuentaCorrienteVenta)res;
			ctacte.setIdLiquidacion(null);
			ctacte.setComision(null);
			ctacte.setPorcentajeComision(null);
		}
	}
	
	private void bloquearVendedorParaActualizarComisiones(){
		// se bloquea a nivel de base de datos, por vendedor
		String sql = "select v.id from " + Esquema.concatenarEsquema("Vendedor") + " v where"
				+ " v.id = :id for update";
		Query query = XPersistence.getManager().createNativeQuery(sql);
		query.setParameter("id", vendedor.getId());
		query.getResultList();
	}
	
	@Override
	protected void validacionesPreGrabarTransaccion(Messages errores){
		super.validacionesPreGrabarTransaccion(errores);
		
		if (this.getEmpresa() != null){
			if (!this.getEmpresa().calculaComisiones()){
				throw new ValidationException("Empresa no calcula comisiones");
			}
		}
		else{
			throw new ValidationException("Empresa no asignada");
		}
		
		if ((this.getDesde().compareTo(this.getHasta()) > 0)){
			throw new ValidationException("Mal definidas las fechas: Desde no puede ser superior Hasta");
		}
		
		if (this.getVendedor() != null){
			if (this.getVendedor().buscarProveedorCtaCte() == null){
				throw new ValidationException("El vendedor " + this.getVendedor().getNombre() + " no tiene asociado un proveedor para la cuenta corriente");
			}
		}
	}
	
	@Override
	public Integer CtaCteCoeficiente() {
		return 1;
	}
	
	@Override
	public boolean generaCtaCte(){
		return true;
	}
}
