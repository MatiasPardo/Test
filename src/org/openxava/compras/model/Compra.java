package org.openxava.compras.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.compras.actions.*;
import org.openxava.negocio.model.*;
import org.openxava.ventas.calculators.CondicionVentaPrincipalCalculator;
import org.openxava.ventas.model.*;

import com.allin.interfacesafip.util.*;

@MappedSuperclass

public abstract class Compra extends Transaccion{

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate 
    @NoModify
    @ReferenceView("Transaccion")
	@OnChange(OnChangeProveedorCompraAction.class)
	private Proveedor proveedor;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre", condition="${compras} = 't'")
	@NoCreate @NoModify
	@DefaultValueCalculator(value=CondicionVentaPrincipalCalculator.class, 
			properties={@PropertyValue(name="ventas", value="false")})
	private CondicionVenta condicionCompra;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre", 
					condition="(${costo} = 't' or ${precioBaseCosto} = 't')")	
	private ListaPrecio listaPrecio;
	
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
	private BigDecimal descuento = BigDecimal.ZERO;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal subtotalSinDescuento = BigDecimal.ZERO;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal subtotal;

	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal subtotal1;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal subtotal2;
	
	abstract public Collection<ItemCompra> ItemsCompra();
	
	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}
	
	public CondicionVenta getCondicionCompra() {
		return condicionCompra;
	}

	public void setCondicionCompra(CondicionVenta condicionCompra) {
		this.condicionCompra = condicionCompra;
	}

	public BigDecimal getTotal() {
		return total == null ? BigDecimal.ZERO : total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public BigDecimal getTotal1() {
		return total1 == null ? BigDecimal.ZERO : total1;
	}

	public void setTotal1(BigDecimal total1) {
		this.total1 = total1;
	}

	public BigDecimal getTotal2() {
		return total2 == null ? BigDecimal.ZERO : total2;
	}

	public void setTotal2(BigDecimal total2) {
		this.total2 = total2;
	}

	public BigDecimal getIva() {
		return iva == null ? BigDecimal.ZERO : iva;
	}

	public void setIva(BigDecimal iva) {
		this.iva = iva;
	}

	public BigDecimal getSubtotal() {
		return subtotal == null ? BigDecimal.ZERO : subtotal;
	}

	public void setSubtotal(BigDecimal subtotal) {
		this.subtotal = subtotal;
	}

	public BigDecimal getSubtotal1() {
		return subtotal1 == null ? BigDecimal.ZERO : subtotal1;
	}

	public void setSubtotal1(BigDecimal subtotal1) {
		this.subtotal1 = subtotal1;
	}

	public BigDecimal getSubtotal2() {
		return subtotal2 == null ? BigDecimal.ZERO : subtotal2;
	}

	public void setSubtotal2(BigDecimal subtotal2) {
		this.subtotal2 = subtotal2;
	}

	@Override
	public Moneda buscarMonedaDefault(){
		Moneda monedaDefault = null;
		if (this.getProveedor() != null){
			monedaDefault = this.getProveedor().getMoneda();
		}
		
		if (monedaDefault == null){
			monedaDefault = super.buscarMonedaDefault();
		}
		return monedaDefault;
	}

	@Override
	protected void atributosConversionesMoneda(List<String> atributos){
		super.atributosConversionesMoneda(atributos);
		atributos.add("Total");
		atributos.add("Subtotal");
	}
	
	@Override
	public void grabarTransaccion(){
		super.grabarTransaccion();
		
		List<String> atributosItems =new LinkedList<String>();
		atributosConversionMonedaItemCompra(atributosItems);
		if (!atributosItems.isEmpty()){
			for(ItemCompra item: this.ItemsCompra()){
				this.sincronizarMonedas(item, atributosItems);
			}
		}
	}
	
	protected void atributosConversionMonedaItemCompra(List<String> atributos){
		atributos.add("Suma");
	}
	
	@Override
	public void recalcularTotales(){
	    super.recalcularTotales();
	    BigDecimal subtotalSinDescuentos = BigDecimal.ZERO;
	    BigDecimal descuentos = BigDecimal.ZERO;
		BigDecimal subtotal = BigDecimal.ZERO;
		BigDecimal iva = BigDecimal.ZERO;
		BigDecimal total = BigDecimal.ZERO;
				
		// Se calculan los totales
		for (ItemCompra item: this.ItemsCompra()){
			item.recalcular();
			BigDecimal subtotalItem = item.getSuma();
			BigDecimal subtotalItemSinDescuento = subtotalItem.subtract(item.getDescuento());			
			if(this.calcularImpuestos()){
				BigDecimal ivaItem = subtotalItem.multiply(item.getTasaiva()).divide(new BigDecimal(100));
				iva = iva.add(ivaItem);
			}			
			subtotal = subtotal.add(subtotalItem);			
			subtotalSinDescuentos = subtotalSinDescuentos.add(subtotalItemSinDescuento);
			descuentos = descuentos.add(item.getDescuento());
		}
		
		BigDecimal impuestosCabecera = BigDecimal.ZERO;
		if (this.calcularImpuestos()){
			impuestosCabecera = this.totalImpuestosCabecera();
		}
		
		 
		subtotal = subtotal.setScale(2, RoundingMode.HALF_EVEN);
		iva = iva.setScale(2, RoundingMode.HALF_EVEN);
		impuestosCabecera.setScale(2, RoundingMode.HALF_EVEN);
		total = total.add(subtotal).add(iva).add(impuestosCabecera);
		subtotalSinDescuentos = subtotalSinDescuentos.setScale(2, RoundingMode.HALF_EVEN);
		descuentos = descuentos.setScale(2, RoundingMode.HALF_EVEN);		
		
		// se actualiza la entidad
		this.setSubtotalSinDescuento(subtotalSinDescuentos);
		this.setDescuento(descuentos);
		this.setSubtotal(subtotal);
		this.setIva(iva);
		this.setTotal(total);
	}	
	
	protected BigDecimal totalImpuestosCabecera(){
		return BigDecimal.ZERO;
	}
	
	protected boolean calcularImpuestos(){
		boolean calcula = true;
		if (this.getEmpresa() != null){
			if (!this.getEmpresa().getInscriptoIva()){
				calcula = false;
			}
		}
		if ((calcula) && (this.getProveedor() != null)){
			calcula = this.getProveedor().getPosicionIva().calculaIvaCompras();			
		}
		return calcula;
	}
	
	public BigDecimal getDescuento() {
		return descuento == null ? BigDecimal.ZERO : this.descuento;
	}

	public void setDescuento(BigDecimal descuento) {
		this.descuento = descuento;
	}

	public BigDecimal getSubtotalSinDescuento() {
		return subtotalSinDescuento == null ? BigDecimal.ZERO : this.subtotalSinDescuento;
	}

	public void setSubtotalSinDescuento(BigDecimal subtotalSinDescuento) {
		this.subtotalSinDescuento = subtotalSinDescuento;
	}

	public ListaPrecio getListaPrecio() {
		return listaPrecio;
	}

	public void setListaPrecio(ListaPrecio listaPrecio) {
		this.listaPrecio = listaPrecio;
	}
	
	@Override
	public void agregarParametrosImpresion(Map<String, Object> parameters) {
		super.agregarParametrosImpresion(parameters);
		
		parameters.put("RAZONSOCIAL_PROVEEDOR", this.getProveedor().getNombre());
		parameters.put("CODIGO_PROVEEDOR", this.getProveedor().getCodigo());
		parameters.put("CUIT_PROVEEDOR", this.getProveedor().getNumeroDocumento());
		parameters.put("TIPODOCUMENTO_PROVEEDOR", this.getProveedor().getTipoDocumento().toString());
		parameters.put("POSICIONIVA_PROVEEDOR", this.getProveedor().getPosicionIva().getDescripcion());
		parameters.put("DIRECCION_PROVEEDOR", this.getProveedor().getDomicilio().getDireccion());
		parameters.put("CODIGOPOSTAL_PROVEEDOR", this.getProveedor().getDomicilio().getCiudad().getCodigoPostal().toString());
		parameters.put("CIUDAD_PROVEEDOR", this.getProveedor().getDomicilio().getCiudad().getCiudad());
		parameters.put("PROVINCIA_PROVEEDOR", this.getProveedor().getDomicilio().getCiudad().getProvincia().getProvincia());
		
		if (this.getCondicionCompra() != null){
			parameters.put("NOMBRE_CONDICIONCOMPRA", this.getCondicionCompra().getNombre());
		}
		else{
			parameters.put("NOMBRE_CONDICIONCOMPRA", "");
		}
		
		parameters.put("TOTAL", this.getTotal());
		parameters.put("TOTAL1", this.getTotal1());
		parameters.put("TOTAL2", this.getTotal2());
		
		String totalLetras = "";
		try{
			totalLetras = NumberToLetterConverter.convertNumberToLetter(this.getTotal());
		}
		catch(Exception e){
		}
		parameters.put("TOTALLETRAS", totalLetras);
		
		String totalLetras1 = "";
		try{
			totalLetras1 = NumberToLetterConverter.convertNumberToLetter(this.getTotal1());
		}
		catch(Exception e){
		}
		parameters.put("TOTALLETRAS1", totalLetras1);
		
		String totalLetras2 = "";
		try{
			totalLetras2 = NumberToLetterConverter.convertNumberToLetter(this.getTotal2());
		}
		catch(Exception e){
		}
		parameters.put("TOTALLETRAS2", totalLetras2);
	}
}
