package org.openxava.inventario.model;


import java.math.*;
import java.util.*;
import javax.persistence.*;

import org.apache.commons.collections.*;
import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.tesoreria.model.*;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(members="Principal[#" + 
					"fecha, fechaUltimaActualizacion, fechaCreacion;" +
					"cumplido, fechaCumplimiento, empresa];"),
	@View(name="OrdenPreparacion", members=
		"Principal[#" + 
				"fecha, fechaUltimaActualizacion, fechaCreacion;" +
				"cumplido, fechaCumplimiento, empresa];" + 
		"ordenPreparacion;"),
	@View(name="ReciboCobranza", members=
	"Principal[#" + 
			"fecha, fechaUltimaActualizacion, fechaCreacion;" +
			"cumplido, fechaCumplimiento, empresa];" + 
	"Factura[numero; itemsFactura];"),
})

@Tab(
		filter=EmpresaFilter.class,
		properties="fecha, ejecutado, numero, cliente.codigo, cliente.nombre, fechaCumplimiento, fechaUltimaActualizacion, fechaCreacion",
		rowStyles={
			@RowStyle(style="pendiente-ejecutado", property="ejecutado", value="true")	
		},
		defaultOrder="${fechaCreacion} desc", 
		baseCondition=EmpresaFilter.BASECONDITION + " and " + Pendiente.BASECONDITION)

public class PendienteRemito extends Pendiente{
	
	public PendienteRemito(){		
	}
	
	public PendienteRemito(Transaccion origen){
		super(origen);
		if (origen instanceof OrdenPreparacion){
			this.setOrdenPreparacion((OrdenPreparacion)origen);
			this.setCliente(((OrdenPreparacion)origen).getCliente());
		}
		else if (origen instanceof ReciboCobranza){
			this.setCliente(((ReciboCobranza)origen).getCliente());
		}
	}
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private OrdenPreparacion ordenPreparacion;
	
	public OrdenPreparacion getOrdenPreparacion() {
		return ordenPreparacion;
	}

	public void setOrdenPreparacion(OrdenPreparacion ordenPreparacion) {
		this.ordenPreparacion = ordenPreparacion;
	}
		
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private Cliente cliente;
	
	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	@Override
	public Transaccion origen() {
		if (this.getOrdenPreparacion() != null){
			return this.getOrdenPreparacion();
		}
		else{
			return this.buscarOrigen();
		}
	}

	@Override
	public String tipoEntidadDestino(Transaccion origen) {
		return Remito.class.getSimpleName();
	}

	@Override
	public Transaccion crearTransaccionDestino() {
		return new Remito();
	}
	
	@Override
	public boolean permiteProcesarJunto(Pendiente pendiente) {
		return false;
	}
		
	@Override
	public void itemsPendientes(List<IItemPendiente> items){
		if (this.getOrdenPreparacion() != null){
			for(ItemOrdenPreparacion item: this.getOrdenPreparacion().getItems()){
				IItemPendiente itempendiente = item.itemPendienteRemitoProxy();
				if (itempendiente != null){
					items.add(itempendiente);
				}
			}
		}
		else{
			Transaccion origen = this.origen();
			if (origen instanceof ReciboCobranza){
				for(ItemVentaElectronica item: ((ReciboCobranza)origen).getFacturaContado().getItems()){
					IItemPendiente itempendiente = item.itemPendienteRemitoProxy();
					if (itempendiente != null){
						items.add(itempendiente);
					}
				}
			}
		}
	}
	
	@Override
	public void separarItemsPendientes(List<Pendiente> pendientes, List<List<IItemPendiente>> grupoItemsPendientes) {
		if (!pendientes.isEmpty()){
			
			List<Pendiente> generadosPorOrdenPreparacion = new LinkedList<Pendiente>();
			List<Pendiente> resto = new LinkedList<Pendiente>();
			for(Pendiente p: pendientes){
				if (p.getTipoTrOrigen().equals(OrdenPreparacion.class.getSimpleName())){
					generadosPorOrdenPreparacion.add(p);
				}
				else{
					resto.add(p);
				}
			}
			
			if (!generadosPorOrdenPreparacion.isEmpty()){
				// Los generados por la orden de preparación, se separan de acuerdo al pedido y su porcentaje de descuento
				Map<BigDecimal, List<IItemPendiente>> gruposPorDescuentoGlobal = new HashMap<BigDecimal, List<IItemPendiente>>();			
				for(Pendiente p: generadosPorOrdenPreparacion){
					List<IItemPendiente> itemsPendientes = new LinkedList<IItemPendiente>();
					p.itemsPendientesNoCumplidos(itemsPendientes);
					
					for (IItemPendiente itemPendiente: itemsPendientes){
						BigDecimal porcentajeDescuento = BigDecimal.ZERO;
						if (((ItemOrdenPreparacion)itemPendiente.getItem()).getPedido() != null){
							porcentajeDescuento = ((ItemOrdenPreparacion)itemPendiente.getItem()).getPedido().getPorcentajeDescuento();					
						}					
						
						// se debe hacer esta comparación, para que funcione el map, ya que el Equals no funciona si el cero tiene distinta presición (el compareto si)
						if (porcentajeDescuento.compareTo(BigDecimal.ZERO) == 0){
							porcentajeDescuento = BigDecimal.ZERO;
						}
						
						List<IItemPendiente> grupo = null;
						if (gruposPorDescuentoGlobal.containsKey(porcentajeDescuento)){
							grupo = gruposPorDescuentoGlobal.get(porcentajeDescuento);
						}
						else{
							grupo = new LinkedList<IItemPendiente>();
							gruposPorDescuentoGlobal.put(porcentajeDescuento, grupo);
						}
						grupo.add(itemPendiente);
					}				
				}
				grupoItemsPendientes.addAll(gruposPorDescuentoGlobal.values());
			}
			
			// el resto toma el comportamiento default 
			super.separarItemsPendientes(resto, grupoItemsPendientes);
		}
		else{
			super.separarItemsPendientes(pendientes, grupoItemsPendientes);
		}
	}
	
	@SuppressWarnings("unchecked")
	@ListProperties("producto.codigo, producto.nombre, cantidad, unidadMedida.nombre, despacho.codigo")
	@CollectionView(value="Remito")
	public Collection<ItemVentaElectronica> getItemsFactura(){
		Transaccion origen = this.origen();
		if (origen instanceof ReciboCobranza){
			return ((ReciboCobranza) origen).getFacturaContado().getItems();
		}
		else{
			return CollectionUtils.EMPTY_COLLECTION;
		}
	}
	
	@Override
	public String viewName(){
		if (this.getOrdenPreparacion() != null){
			return "OrdenPreparacion";
		}
		else if (this.getTipoTrOrigen().equals(ReciboCobranza.class.getSimpleName())){
			return "ReciboCobranza";
		}
		else{
			return super.viewName();
		}
	}
	
	
}
