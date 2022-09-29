package org.openxava.compras.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.inventario.model.*;
import org.openxava.negocio.model.Cantidad;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(members=						
	"Principal{" +
		"Principal[#" +
				"descripcion, fechaCreacion, usuario;" +
				"fecha, fechaReal, estado;" +
				"tipo, empresa, numero;" + 
				"cae, fechaVencimientoCAE, fechaVencimiento, fechaServicio;" +
				"proveedor;" + 
				"condicionCompra, moneda, cotizacion;" + 
				"observaciones];" +
		"impuestos;" +
		"items;" +
		"Totales[subtotal, iva, otrosImpuestos, total];" + 				
		"Descuentos[subtotalSinDescuento, descuento];" +
	"}" +
	"Recepcion{recepcion, importeNoConforme;}" +
	"Trazabilidad{trazabilidad} CuentaCorriente{ctacte}" 
	),
	@View(name="FacturaCompra", members=					
	"Principal{" +
		"Principal[#" +
				"descripcion, fechaCreacion, usuario;" +
				"fecha, fechaReal, estado;" +
				"tipo, empresa, numero;" + 
				"cae, fechaVencimientoCAE, fechaVencimiento, fechaServicio;" +
				"proveedor;" + 
				"condicionCompra, moneda, cotizacion;" + 
				"observaciones];" +
		"impuestos;" +
		"items;" +
		"Totales[subtotal, iva, otrosImpuestos, total];" + 				
		"Descuentos[subtotalSinDescuento, descuento];" +
	"}" +
	"Recepcion{recepcion, importeNoConforme;}" +
	"Trazabilidad{trazabilidad}" 
	),
	@View(name="Cerrado", members=
		"Principal{" + 
			"Principal[#" +
					"descripcion, fechaCreacion, usuario;" +
					"fecha, fechaReal, estado;" +
					"tipo, empresa, numero;" + 
					"cae, fechaVencimientoCAE, fechaVencimiento, fechaServicio;" +
					"proveedor;" + 
					"condicionCompra, moneda, cotizacion;" + 
					"observaciones];" + 	
			"items;" +
			"impuestos;" + 
			"Totales[subtotal, iva, otrosImpuestos, total];" +
			"Descuentos[subtotalSinDescuento, descuento];" +
		"}" +  
		"Recepcion{recepcion, importeNoConforme;}" +
		"Trazabilidad{trazabilidad}  CuentaCorriente{ctacte}"
	),
	@View(name="Simple", members="numero, estado;")
})

@Tab(
		filter=EmpresaFilter.class,
		properties="fecha, empresa.nombre, numero, cae, estado, proveedor.nombre, total, subtotal, iva, fechaCreacion, usuario, importeNoConforme",
		baseCondition=EmpresaFilter.BASECONDITION,
		defaultOrder="${fechaCreacion} desc")

public class FacturaCompra extends CompraElectronica implements IImportadorItemCSV{
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoFrame
	private RecepcionMercaderia recepcion;
	
	@ReadOnly
	@Stereotype("MONEY")
	private BigDecimal importeNoConforme = BigDecimal.ZERO;
	
	public BigDecimal getImporteNoConforme() {
		return importeNoConforme;
	}

	public void setImporteNoConforme(BigDecimal importeNoConforme) {
		this.importeNoConforme = importeNoConforme;
	}

	public RecepcionMercaderia getRecepcion() {
		return recepcion;
	}

	public void setRecepcion(RecepcionMercaderia recepcion) {
		this.recepcion = recepcion;
	}

	@Override
	public void onPreCreate(){
		super.onPreCreate();
		this.setTipoOperacion("Factura");
	}
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Factura de Compra";		
	}
	
	@Override
	public String viewName(){
		if (this.cerrado()){
			return "Cerrado";
		}
		else{
			return "FacturaCompra";
		}
	}

	/*@Override
	protected IEstrategiaCancelacionPendiente establecerEstrategiaCancelacionPendiente(){
		if (this.getRecepcion() != null){
			EstrategiaCancelacionPendientePorUso estrategia = new EstrategiaCancelacionPendientePorUso();
			Pendiente pendiente = this.getRecepcion().buscarPendiente(this.getClass());
			estrategia.getPendientes().add(pendiente);
			return estrategia;
		}
		else{
			return super.establecerEstrategiaCancelacionPendiente();
		}
	}*/
	@Override
	protected IEstrategiaCancelacionPendiente establecerEstrategiaCancelacionPendiente(){
		if (this.getRecepcion() != null){
			EstrategiaCancelacionPendientePorItem estrategia = new EstrategiaCancelacionPendientePorItem();
			for (ItemCompraElectronica item: this.getItems()){
				BigDecimal cantidadTotalACancelar = item.cantidadTotalACancelar();
				if (!item.getRecepciones().isEmpty() && (cantidadTotalACancelar.compareTo(BigDecimal.ZERO) > 0)){					
					
					List<ItemRecepcionFacturaCompra> itemsEliminados = new LinkedList<ItemRecepcionFacturaCompra>();
					for(ItemRecepcionFacturaCompra itemRecepcionFacturaCompra: item.getRecepciones()){				
						IItemPendientePorCantidad pendientePorCantidad = itemRecepcionFacturaCompra.getItemRecepcion().itemPendienteFacturaCompraProxy();
						if (pendientePorCantidad != null){
							if (itemRecepcionFacturaCompra.getCancelar().compareTo(BigDecimal.ZERO) > 0){
								Cantidad cantidadPendiente = pendientePorCantidad.getCantidadACancelar();
								cantidadPendiente.setCantidad(itemRecepcionFacturaCompra.getCancelar());
								cantidadPendiente.setUnidadMedida(item.getUnidadMedida());
								estrategia.getItemsPendientes().add(pendientePorCantidad);
								
							}
							else{
								item.quitarRecepcion(itemRecepcionFacturaCompra);
								itemsEliminados.add(itemRecepcionFacturaCompra);
							}
						}							
					}
					item.getRecepciones().removeAll(itemsEliminados);
				}					
			}			
			if (!estrategia.getItemsPendientes().isEmpty()){
				return estrategia;
			}
			else{
				return super.establecerEstrategiaCancelacionPendiente();
			}
			
		}
		else{
			return super.establecerEstrategiaCancelacionPendiente();
		}
	}
	
	public void fusionarItem(Collection<ItemCompraElectronica> items, ItemCompraElectronica item){
		boolean fusion = false;
		for(ItemCompraElectronica itemCompra: items){
			fusion = item.fusionarA(itemCompra);
			if (fusion){
				break;
			}
		}
		if (!fusion){
			items.add(item);
		}
	}

	@Override
	protected void inicializar(){
		super.inicializar();
		
		if (this.getRecepcion() != null){
			BigDecimal importe = BigDecimal.ZERO;
			for(ItemRecepcionMercaderia itemRecepcion: this.getRecepcion().getItems()){
				importe = importe.add(itemRecepcion.getImporteNoConformidad());
			}
			this.setImporteNoConforme(importe.setScale(2, RoundingMode.HALF_EVEN));
		}
	}
		
	@Override
	public String CtaCteTipo(){
		return "FACTURA COMPRA";
	}
	
	
	protected boolean numeroRepetido(){
		return this.numeroRepetidoCompra();
	}

	@Override
	public void iniciarImportacionCSV() {	
	}

	@Transient
	private Map<String, ItemCompraElectronica> itemsPorProductoCSV = null; 
	
	private Map<String, ItemCompraElectronica> getItemsPorProducto(){
		if (this.itemsPorProductoCSV == null){
			this.itemsPorProductoCSV = new HashMap<String, ItemCompraElectronica>();
			for(ItemCompraElectronica item: this.getItems()){
				if (!itemsPorProductoCSV.containsKey(item.getProducto().getCodigo())){
					itemsPorProductoCSV.put(item.getProducto().getCodigo(), item);
				}
			}
		}
		return this.itemsPorProductoCSV;
	}
	
	@Override
	public ItemTransaccion crearItemDesdeCSV(String[] values) {
		Integer cantidadCampos = 3;
		ItemCompraElectronica item = null;
		
		if (values.length >= cantidadCampos){
			String codigoProducto = values[0];
			Producto producto = (Producto)ObjetoEstatico.buscarPorCodigo(codigoProducto, Producto.class.getSimpleName());
			if (producto == null){
				throw new ValidationException("No existe el producto de código " + codigoProducto);
			}			
			BigDecimal cantidad = ProcesadorCSV.convertirTextoANumero(values[2]);
			BigDecimal precio = null;
			if ((values.length > 3) && (!Is.emptyString(values[3]))){
				precio = ProcesadorCSV.convertirTextoANumero(values[3]);
			}
			else{
				precio = ListaPrecio.costoDefault(this, producto, producto.getUnidadMedida(), cantidad);
			}
			if (this.getItemsPorProducto().containsKey(codigoProducto)){
				item = this.getItemsPorProducto().get(codigoProducto);
			}
			else{
				item = new ItemCompraElectronica();
				item.setCompra(this);
				item.setProducto(producto);
				this.getItemsPorProducto().put(codigoProducto, item);
				this.getItems().add(item);
			}			
			item.setCantidad(cantidad);
			if (precio != null){
				item.setPrecioUnitario(precio);
			}			
			return item;
		}
		else{
			throw new ValidationException("Faltan campos: deben ser " + cantidadCampos.toString());
		}			
	}

	@Override
	public void finalizarImportacionCSV() {
		this.getItemsPorProducto().clear();
		this.itemsPorProductoCSV = null;
	}	
}
