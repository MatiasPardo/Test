package org.openxava.ventas.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.inventario.model.*;

@Entity

@Views({
	@View(members=
		"Principal[#" + 
		"fecha, fechaUltimaActualizacion, fechaCreacion;" +
		"cumplido, fechaCumplimiento, empresa];" + 
		"Remito{remito}Liquidacion{liquidacion};" 
	),
	@View(name="Remito", members=
		"Principal[#" + 
		"fecha, fechaUltimaActualizacion, fechaCreacion;" +
		"cumplido, fechaCumplimiento, empresa];" + 
		"Remito{remito};" 
	),
	@View(name="Liquidacion", members=
		"Principal[#" + 
		"fecha, fechaUltimaActualizacion, fechaCreacion;" +
		"cumplido, fechaCumplimiento, empresa];" + 
		"Liquidacion{liquidacion};" 
	),
	@View(name="Pedido", members=
		"Principal[#" + 
		"fecha, fechaUltimaActualizacion, fechaCreacion;" +
		"cumplido, fechaCumplimiento, empresa];" + 
		"Pedido{pedido};" 
)
})
@Tab(
		filter=EmpresaFilter.class,
		properties="fecha, ejecutado, remito.numero, liquidacion.numero, pedido.numero, cliente.codigo, cliente.nombre, fechaCumplimiento, fechaUltimaActualizacion, fechaCreacion",
		rowStyles={
			@RowStyle(style="pendiente-ejecutado", property="ejecutado", value="true")	
		},
		defaultOrder="${fechaCreacion} desc", 
		baseCondition=EmpresaFilter.BASECONDITION + " and " + Pendiente.BASECONDITION)

public class PendienteFacturaVenta extends Pendiente{

	public PendienteFacturaVenta(){
	
	}
		
	public PendienteFacturaVenta(Transaccion origen){
		super(origen);
		if (origen instanceof LiquidacionConsignacion){
			this.setLiquidacion((LiquidacionConsignacion)origen);
			this.setRemito(this.getLiquidacion().getRemito());			
			this.setCliente(this.getRemito().getCliente());			
			this.setPedido(this.getRemito().primerPedidoAsociado());
		}
		else if (origen instanceof PedidoVenta){
			this.setPedido((PedidoVenta)origen);
			this.setCliente(this.getPedido().getCliente());
		}
		else{
			this.setRemito((Remito)origen);			
			this.setCliente(this.getRemito().getCliente());
			this.setPedido(this.getRemito().primerPedidoAsociado());
		}		
	}
	
	@Override
	public void inicializar(Transaccion origen){
		super.inicializar(origen);
		
		if (this.getPedido() != null){
			// Cuando el pendiente es generado desde el pedido directo: facturación de servicios/conceptos
			if (this.getRemito() == null){
				for(EstadisticaPedidoVenta item: this.getPedido().getItems()){
					item.setPendientePreparacion(item.getCantidad());
				}
			}
		}
		
	}
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)	
	private Remito remito;
		
	public Remito getRemito() {
		return remito;
	}

	public void setRemito(Remito remito) {
		this.remito = remito;
	}

	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private LiquidacionConsignacion liquidacion;
	
	public LiquidacionConsignacion getLiquidacion() {
		return liquidacion;
	}

	public void setLiquidacion(LiquidacionConsignacion liquidacion) {
		this.liquidacion = liquidacion;
	}

	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)	
	private PedidoVenta pedido;
	
	public PedidoVenta getPedido() {
		return pedido;
	}

	public void setPedido(PedidoVenta pedido) {
		this.pedido = pedido;
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
		if (this.getLiquidacion() != null){
			return this.getLiquidacion();
		}
		else if (this.getRemito() != null){
			return this.getRemito();
		}
		else{
			return this.getPedido();
		}
	}

	@Override
	public String tipoEntidadDestino(Transaccion origen) {
		if (origen.getEmpresa().getInscriptoIva()){
			return FacturaVenta.class.getSimpleName();
		}
		else{
			return FacturaManual.class.getSimpleName();
		}
	}

	@Override
	public Transaccion crearTransaccionDestino() {
		if (this.getTipoTrDestino().equals(FacturaVenta.class.getSimpleName())){
			return new FacturaVenta();
		}
		else{
			return new FacturaManual();
		}
	}
	
	@Override
	public boolean permiteProcesarJunto(Pendiente pendiente) {
		return false;
	}
		
	@Override
	public void itemsPendientes(List<IItemPendiente> items){
		if (this.getLiquidacion() != null){
			for(ItemLiquidacionConsignacion item: this.getLiquidacion().getItems()){
				IItemPendiente itempendiente = item.itemPendienteFacturaVentaProxy();
				if (itempendiente != null){
					items.add(itempendiente);
				}
			}
		}
		else if (this.getRemito() != null){
			for(ItemRemito item: this.getRemito().getItems()){
				IItemPendiente itempendiente = item.itemPendienteFacturaVentaProxy();
				if (itempendiente != null){
					items.add(itempendiente);
				}
			}
		}
		else if (this.getPedido() != null){
			for(EstadisticaPedidoVenta item: this.getPedido().getItems()){
				IItemPendiente itempendiente = item.itemPendienteFacturaVentaProxy();
				if (itempendiente != null){
					items.add(itempendiente);
				}
			}
		}
	}
	
	public String viewName(){
		if (this.getLiquidacion() != null){
			return "Liquidacion";
		}
		else if (this.getRemito() != null){
			return "Remito";
		}
		else if (this.getPedido() != null){
			return "Pedido";
		}
		else{
			return super.viewName();
		}
	}
	
}
