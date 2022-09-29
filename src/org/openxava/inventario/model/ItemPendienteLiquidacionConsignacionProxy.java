package org.openxava.inventario.model;

import java.math.*;

import org.openxava.base.model.*;
import org.openxava.negocio.model.*;

public class ItemPendienteLiquidacionConsignacionProxy extends ItemPendientePorCantidadProxy{
	
	private ItemRemito itemRemito;
	
	public ItemRemito getItemRemito() {
		return itemRemito;
	}

	public void setItemRemito(ItemRemito itemRemito) {
		this.itemRemito = itemRemito;
	}

	@Override
	public UnidadMedida getUnidadMedida() {
		return this.getItemRemito().getUnidadMedida();
	}

	@Override
	public ObjetoNegocio getItem() {
		return this.getItemRemito();
	}

	@Override
	public BigDecimal getCantidadPendiente() {
		return this.getItemRemito().getPendienteLiquidacion();
	}

	@Override
	public void setCantidadPendiente(BigDecimal cantidadPendiente) {
		this.getItemRemito().setPendienteLiquidacion(cantidadPendiente);		
	}

	@Override
	public BigDecimal getCantidadOriginal() {
		return this.getItemRemito().getCantidad();
	}

	private Pendiente pendiente = null;
	
	@Override
	public Pendiente getPendiente() {
		if (pendiente == null){
			this.pendiente = getItemRemito().getRemito().buscarPendiente(LiquidacionConsignacion.class);
		}
		return pendiente;
	}
	
	private boolean cantidadesParaLiquidacion = false;
	
	private BigDecimal cantidadLiquidacionFacturar = null;
	
	private BigDecimal cantidadLiquidacionDevolucion = null;
	
	public void asignarCantidadesParaLiquidacion(BigDecimal cantidadFacturar, BigDecimal cantidadDevolver) {
		if ((cantidadFacturar != null) && (cantidadDevolver != null)){
			this.cantidadLiquidacionFacturar = cantidadFacturar;
			this.cantidadLiquidacionDevolucion = cantidadDevolver;
			this.cantidadesParaLiquidacion = true;
		}
	}
	
	public void modificarCantidadesParaLiquidacion(ItemLiquidacionConsignacion item){
		if (this.cantidadesParaLiquidacion){
			item.setFacturar(this.cantidadLiquidacionFacturar);
			item.setDevolucion(this.cantidadLiquidacionDevolucion);
		}
		else{
			item.setFacturar(this.getCantidadPendiente());
		}
	}

}
