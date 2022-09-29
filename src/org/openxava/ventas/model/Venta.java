package org.openxava.ventas.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.calculators.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.ventas.actions.*;
import org.openxava.ventas.calculators.*;

import com.clouderp.maps.model.AddressCloud;
import com.clouderp.maps.model.IObjectMapCloud;
import com.clouderp.maps.model.MapCloud;


@MappedSuperclass

public abstract class Venta extends Transaccion implements IVenta, IObjectMapCloud{

	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate 
    @NoModify
    @DefaultValueCalculator(value=VendedorDefaultCalculator.class)
	private Vendedor vendedor;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate 
    @NoModify
    @ReferenceView("PedidoVenta")
	@SearchAction(value="ReferenciaClienteEnVenta.buscar")
	@OnChange(OnChangeClienteVentaConVendedorAction.class)
	private Cliente cliente;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre", 
					condition="${costo} = 'f'")
	private ListaPrecio listaPrecio;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView(value="Simple")
	@SearchAction(value="ReferenciaDomicilioVenta.buscar")
	private Domicilio domicilioEntrega;
		
	@Required @DefaultValueCalculator(CurrentDateCalculator.class) 
	private Date fechaVencimiento;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal total;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal total1;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal total2;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal iva;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal subtotal;

	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal subtotal1;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal subtotal2;
	
	@Min(value=0, message="No puede menor a 0")
	@Max(value=100, message="No puede ser mayor a 100")
	private BigDecimal porcentajeDescuento;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal descuento;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal subtotalSinDescuento;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	@DefaultValueCalculator(value=CondicionVentaPrincipalCalculator.class)		
	private CondicionVenta condicionVenta;
	
	abstract public Collection<EstadisticaItemVenta> ItemsVenta();
		
	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Vendedor getVendedor() {
		return vendedor;
	}

	public void setVendedor(Vendedor vendedor) {
		this.vendedor = vendedor;
	}

	public Date getFechaVencimiento() {
		return fechaVencimiento;
	}

	public void setFechaVencimiento(Date fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}

	public BigDecimal getTotal() {
		return total == null? BigDecimal.ZERO: this.total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public BigDecimal getIva() {
		return iva == null? BigDecimal.ZERO: this.iva;
	}

	public void setIva(BigDecimal iva) {
		this.iva = iva;
	}

	public BigDecimal getSubtotal() {
		return subtotal == null? BigDecimal.ZERO: this.subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}

	public BigDecimal getTotal2() {
		return total2 == null? BigDecimal.ZERO: this.total2;
	}

	public void setTotal2(BigDecimal total2) {
		this.total2 = total2;
	}

	public BigDecimal getSubtotal2() {
		return subtotal2 == null? BigDecimal.ZERO: this.subtotal2;
	}

	public void setSubtotal2(BigDecimal subtotal2) {
		this.subtotal2 = subtotal2;
	}

	public BigDecimal getTotal1() {
		return total1 == null? BigDecimal.ZERO: this.total1;
	}

	public void setTotal1(BigDecimal total1) {
		this.total1 = total1;
	}

	public BigDecimal getSubtotal1() {
		return subtotal1 == null? BigDecimal.ZERO: this.subtotal1;
	}

	public void setSubtotal1(BigDecimal subtotal1) {
		this.subtotal1 = subtotal1;
	}

	public BigDecimal getDescuento() {
		return descuento == null? BigDecimal.ZERO: this.descuento;
	}

	public void setDescuento(BigDecimal descuento) {
		this.descuento = descuento;
	}

	public BigDecimal getSubtotalSinDescuento() {
		return subtotalSinDescuento == null? BigDecimal.ZERO: this.subtotalSinDescuento;
	}

	public void setSubtotalSinDescuento(BigDecimal subtotalSinDescuento) {
		this.subtotalSinDescuento = subtotalSinDescuento;
	}
	
	public BigDecimal getPorcentajeDescuento() {
		return porcentajeDescuento == null? BigDecimal.ZERO: this.porcentajeDescuento;
	}

	public void setPorcentajeDescuento(BigDecimal porcentajeDescuento) {
		this.porcentajeDescuento = porcentajeDescuento;
	}
	
	@Transient
	boolean totalesInstanciados = false;
	
	private void instanciarTotales(){
		if (!totalesInstanciados && this.soloLectura()){
			totalesInstanciados = true;
			// se trae de la base de datos, porque sino no quedan inicializados
			Venta venta = (Venta)XPersistence.getManager().find(this.getClass(), this.getId());
			this.setSubtotalSinDescuento(venta.getSubtotalSinDescuento());
			this.setSubtotal(venta.getSubtotal());
			this.setIva(venta.getIva());
			this.setTotal(venta.getTotal());
			this.setDescuento(venta.getDescuento());
		}
	}
	
	@Hidden
	public BigDecimal getSubTotalSinDescuentoImporte(){
		if (this.ejecutarRecalculoTotales()){
			this.recalcularTotales();
		}
		else {
			this.instanciarTotales();
		}
		return this.getSubtotalSinDescuento();
	}
	
	@Hidden
	public BigDecimal getSubtotalImporte() {
		if (this.ejecutarRecalculoTotales()){
			this.recalcularTotales();
		}
		else {
			this.instanciarTotales();
		}
		return this.getSubtotal();
	}
	
	@Hidden
	public BigDecimal getIvaImporte() {
		if (this.ejecutarRecalculoTotales()){
			this.recalcularTotales();
		}
		else {
			this.instanciarTotales();
		}
		return this.getIva();
	}
	
	@Hidden
	public BigDecimal getTotalImporte() {
		if (this.ejecutarRecalculoTotales()){
			this.recalcularTotales();
		}
		else {
			this.instanciarTotales();
		}
		return this.getTotal();
	}
	
	@Depends("porcentajeDescuento")
	@Hidden
	public BigDecimal getDescuentoImporte(){
		// se tiene que recalcular siempre, porque es de cabecera
		if (this.ejecutarRecalculoTotales()){
			this.recalcularTotales();
		}
		else {
			this.instanciarTotales();
		}
		return this.getDescuento().negate();
	}
	
	@Override
	public void recalcularTotales(){
	    super.recalcularTotales();
		BigDecimal subTotalSinDescuento = BigDecimal.ZERO;
		BigDecimal descuentos = BigDecimal.ZERO;
		BigDecimal subtotal = BigDecimal.ZERO;
		BigDecimal iva = BigDecimal.ZERO;
		BigDecimal total = BigDecimal.ZERO;
		//BigDecimal percepciones = BigDecimal.ZERO;
				
		if (this.getListaPrecio() == null){
			this.setListaPrecio(this.getCliente().getListaPrecio());
		}
		
		// Se calculan los totales
		for (EstadisticaItemVenta item: this.ItemsVenta()){
			item.recalcular();
			BigDecimal subtotalItemSinDescuento = item.getImporteSinDescuento();
			BigDecimal subtotalConDescuento = item.getSubtotal();
			if(this.calcularImpuestos()){
				BigDecimal ivaItem = subtotalConDescuento.multiply(item.getTasaiva()).divide(new BigDecimal(100));
				iva = iva.add(ivaItem);
			}
			subTotalSinDescuento = subTotalSinDescuento.add(subtotalItemSinDescuento);
			subtotal = subtotal.add(subtotalConDescuento);			
		}

		descuentos = subTotalSinDescuento.subtract(subtotal);
		 
		subTotalSinDescuento = subTotalSinDescuento.setScale(2, RoundingMode.HALF_EVEN);
		descuentos = descuentos.setScale(2, RoundingMode.HALF_EVEN).negate();
		subtotal = subtotal.setScale(2, RoundingMode.HALF_EVEN);
		iva = iva.setScale(2, RoundingMode.HALF_EVEN);
		total = total.add(subtotal).add(iva);
		
		// se actualiza la entidad
		this.setSubtotalSinDescuento(subTotalSinDescuento);
		this.setDescuento(descuentos);
		this.setSubtotal(subtotal);
		this.setIva(iva);
		this.setTotal(total);
	}	
	
	@Override
	protected void atributosConversionesMoneda(List<String> atributos){
		super.atributosConversionesMoneda(atributos);
		atributos.add("Total");
		atributos.add("Subtotal");
	}
	
	public abstract boolean calcularImpuestos();
	
	public abstract boolean generaCuentaCorriente();
		
	public CondicionVenta getCondicionVenta() {
		return condicionVenta;
	}

	public void setCondicionVenta(CondicionVenta condicionVenta) {
		this.condicionVenta = condicionVenta;
	}

	public BigDecimal CtaCteImporte(){
		return this.getTotal();
	}
	
	public Transaccion CtaCteTransaccion(){
		return this;
	}
	
	public Domicilio getDomicilioEntrega() {
		return domicilioEntrega;
	}

	public void setDomicilioEntrega(Domicilio domicilioEntrega) {
		this.domicilioEntrega = domicilioEntrega;
	}

	public void agregarParametrosImpresion(Map<String, Object> parameters) {
		super.agregarParametrosImpresion(parameters);
		
		parameters.put("RAZONSOCIAL_CLIENTE", this.getCliente().getNombre());
		parameters.put("CODIGO_CLIENTE", this.getCliente().getCodigo());
		parameters.put("CUIT_CLIENTE", this.getCliente().getNumeroDocumento());
		parameters.put("TIPODOCUMENTO_CLIENTE", this.getCliente().getTipoDocumento().toString());
		parameters.put("POSICIONIVA_CLIENTE", this.getCliente().getPosicionIva().getDescripcion());
		parameters.put("DIRECCION_CLIENTE", this.getCliente().getDomicilio().getDireccion());
		parameters.put("CODIGOPOSTAL_CLIENTE", this.getCliente().getDomicilio().getCiudad().getCodigoPostal().toString());
		parameters.put("CIUDAD_CLIENTE", this.getCliente().getDomicilio().getCiudad().getCiudad());
		parameters.put("PROVINCIA_CLIENTE", this.getCliente().getDomicilio().getCiudad().getProvincia().getProvincia());
		
		// domicilio entrega
		parameters.put("DIRECCION_DOMICILIOENTREGA", this.getDomicilioEntrega().getDireccion());
		parameters.put("CODIGOPOSTAL_DOMICILIOENTREGA", this.getDomicilioEntrega().getCiudad().getCodigoPostal());
		parameters.put("CIUDAD_DOMICILIOENTREGA", this.getDomicilioEntrega().getCiudad().getCiudad());
		parameters.put("PROVINCIA_DOMICILIOENTREGA", this.getDomicilioEntrega().getProvincia().getProvincia());
		LugarEntregaMercaderia lugarEntrega = this.getCliente().lugarEntrega(this.getDomicilioEntrega());
		if (lugarEntrega != null){
			parameters.put("HORARIO_DOMICILIOENTREGA", lugarEntrega.getHorario());
		}
		else{
			parameters.put("HORARIO_DOMICILIOENTREGA", "");
		}
		
		if (this.getVendedor() != null){
			parameters.put("CODIGO_VENDEDOR", this.getVendedor().getCodigo());
			parameters.put("NOMBRE_VENDEDOR", this.getVendedor().getNombre());
		}
		else{
			parameters.put("CODIGO_VENDEDOR", "");
			parameters.put("NOMBRE_VENDEDOR", "");
		}
		
		parameters.put("PORCENTAJEDESCUENTO", this.getPorcentajeDescuento());
		parameters.put("TOTAL", this.getTotal());
		parameters.put("TOTAL1", this.getTotal1());
		parameters.put("TOTAL2", this.getTotal2());
		parameters.put("SUBTOTALSINDESCUENTO", this.getSubtotalSinDescuento());
		parameters.put("DESCUENTO", this.getDescuento());
		parameters.put("SUBTOTAL", this.getSubtotal());
		parameters.put("SUBTOTAL1", this.getSubtotal1());
		parameters.put("SUBTOTAL2", this.getSubtotal2());
		parameters.put("IVA", this.getIva());
		
		if (this.getCondicionVenta() != null){
			parameters.put("NOMBRE_CONDICIONVENTA", this.getCondicionVenta().getNombre());
		}
		else{
			parameters.put("NOMBRE_CONDICIONVENTA", "");
		}
	}
	
	@Override
	protected void validacionesPreGrabarTransaccion(Messages errores){
		super.validacionesPreGrabarTransaccion(errores);
		
		Vendedor vendedorUsuario = Vendedor.buscarVendedorUsuario(Users.getCurrent());
		if (vendedorUsuario != null){
			if (!vendedorUsuario.getGerencia()){
				if (this.getVendedor() == null){
					errores.add("Falta asignar Vendedor");
				}
				else if (!this.getVendedor().equals(vendedorUsuario)){
					errores.add("No esta habilitado para registrar un pedido de otro vendedor");
				}
				else if (this.getCliente() != null){
					if (!Is.equal(this.getCliente().getVendedor(), vendedorUsuario)){
						errores.add("No esta habilitado para registrar pedidos de clientes que no le fueron asignados");
					}
				}
			}
		}
	}

	public ListaPrecio getListaPrecio() {
		return listaPrecio;
	}

	public void setListaPrecio(ListaPrecio listaPrecio) {
		this.listaPrecio = listaPrecio;
	}
	
	@Override
	public boolean refrescarColecciones(){
		return true;
	}

	public AddressCloud addressMapCloud(MapCloud map){
		if (this.getDomicilioEntrega().tieneCoordenadasGPS()){
			AddressCloud address = map.addAddress(this.getDomicilioEntrega().getLatitud(), this.getDomicilioEntrega().getLongitud());
			address.setLabel("N° " + this.getNumero() + " - " + this.getCliente().getCodigo() + " " + this.getCliente().getNombre());
			address.setDescription(this.getDomicilioEntrega().getDireccion());
			return address;
		}
		else{
			return null;
		}
	}
	
	@Override
	public CondicionVenta condicionVentaCalculoPrecio() {
		return this.getCondicionVenta();
	}
	
	
}
