package org.openxava.inventario.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.negocio.filter.*;
import org.openxava.ventas.model.*;

@Entity

@View(members=
	"Principal[#" + 
			"fecha, fechaUltimaActualizacion, fechaCreacion;" +
			 
			"cumplido, fechaCumplimiento, empresa];" + 
	"remito;"
	 
)

@Tab(
		filter=SucursalEmpresaFilter.class,
		properties="fecha, ejecutado, remito.numero, cliente.codigo, cliente.nombre, fechaCumplimiento, fechaUltimaActualizacion, fechaCreacion",
		rowStyles={
				@RowStyle(style="pendiente-ejecutado", property="ejecutado", value="true")	
		},		
		defaultOrder="${fechaCreacion} desc", 
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL + " and " + Pendiente.BASECONDITION)


public class PendienteLiquidacionConsignacion extends Pendiente{

	public PendienteLiquidacionConsignacion(){
	}
	
	public PendienteLiquidacionConsignacion(Transaccion origen){
		super(origen);
		this.setRemito((Remito)origen);
		this.setCliente(((Remito)origen).getCliente());
	}
	
	@Override
	public void inicializar(Transaccion origen){
		super.inicializar(origen);
		for(ItemRemito item: this.getRemito().getItems()){
			item.setPendienteLiquidacion(item.getCantidad());
		}
	}
	
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Liquidacion")
	private Remito remito;
		
	@ReadOnly
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	private Cliente cliente;
	
	public Remito getRemito() {
		return remito;
	}

	public void setRemito(Remito remito) {
		this.remito = remito;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	
	@Override
	public Transaccion origen() {
		return this.getRemito();
	}

	@Override
	public String tipoEntidadDestino(Transaccion origen) {		
		return LiquidacionConsignacion.class.getSimpleName();
	}

	@Override
	public Transaccion crearTransaccionDestino() {
		return new LiquidacionConsignacion();
	}
	
	@Override
	public boolean permiteProcesarJunto(Pendiente pendiente) {
		return false;
	}
	
	@Override
	public void itemsPendientes(List<IItemPendiente> items){
		for(ItemRemito item: this.getRemito().getItems()){
			items.add(item.itemPendienteLiquidacionProxy());
		}
	}
}
