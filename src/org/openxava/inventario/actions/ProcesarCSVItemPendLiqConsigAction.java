package org.openxava.inventario.actions;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.openxava.actions.IChainAction;
import org.openxava.base.actions.ProcesarCSVGenericoAction;
import org.openxava.base.model.IItemPendiente;
import org.openxava.inventario.model.ItemPendienteLiquidacionConsignacionProxy;
import org.openxava.inventario.model.ItemRemito;
import org.openxava.util.Is;
import org.openxava.util.Messages;
import org.openxava.validators.ValidationException;

import com.csvreader.CsvReader;

public class ProcesarCSVItemPendLiqConsigAction extends ProcesarCSVGenericoAction implements IChainAction{
	
	private static final int CANTIDADCOLUMNAS = 9;
	
	@Override
	protected int getCantidadColumnasObligatorias(){
		return CANTIDADCOLUMNAS;
	}
			
	
	private Map<String, Object> clientesProductosProcesados;

	@Override
	protected void preProcesarCSV() throws Exception {	
		this.clientesProductosProcesados = new HashMap<String, Object>();
		this.itemsPendientes = new LinkedList<IItemPendiente>();
	}
	
	private List<IItemPendiente> itemsPendientes = null;
		
	public List<IItemPendiente> getItemsPendientes() {
		return itemsPendientes;
	}

	@Override
	protected void procesarLineaCSV(CsvReader csvReader) throws IOException {
		String codigoCliente = csvReader.get(0);
		String codigoProducto = csvReader.get(2);
		String cantidadFacturarStr = csvReader.get(5);
		String cantidadDevolucionStr = csvReader.get(6);
		String codigoEmpresa = csvReader.get(7);
		String codigoSucursal = csvReader.get(8);
		
		Messages errores = new Messages();
		if (Is.emptyString(codigoCliente)){
			errores.add("Falta asignar código de cliente");
		}
		if (Is.emptyString(codigoProducto)){
			errores.add("Falta asignar código de producto");
		}
		if (Is.emptyString(codigoEmpresa)){
			errores.add("Falta asignar código de empresa");
		}
		if (Is.emptyString(codigoSucursal)){
			errores.add("Falta asignar código de sucursal");
		}
		BigDecimal cantidadFacturar = BigDecimal.ZERO;
		if (!Is.emptyString(cantidadFacturarStr)){
			cantidadFacturar = this.convertirStrDecimal(cantidadFacturarStr);
			if (cantidadFacturar.compareTo(BigDecimal.ZERO) < 0){
				errores.add("Las cantidades no pueden ser negativas");
			}
		}
		BigDecimal cantidadDevolver = BigDecimal.ZERO;
		if (!Is.emptyString(cantidadDevolucionStr)){
			cantidadDevolver = this.convertirStrDecimal(cantidadDevolucionStr);
			if (cantidadDevolver.compareTo(BigDecimal.ZERO) < 0){
				errores.add("Las cantidades no pueden ser negativas");
			}
		}
		
		BigDecimal cantidadTotal = cantidadFacturar.add(cantidadDevolver);
		if (cantidadTotal.compareTo(BigDecimal.ZERO) == 0){
			errores.add("Debe asignar una cantidad a facturar y/o a devolver");
		}
		if (errores.isEmpty()){
			if (!this.clientesProductosProcesados.containsKey(codigoCliente + codigoProducto + codigoEmpresa + codigoSucursal)){
				this.clientesProductosProcesados.put(codigoCliente + codigoProducto + codigoEmpresa + codigoSucursal, null);
				
				List<ItemRemito> itemsRemitos = ItemRemito.buscarItemRemitoPendienteLiquicacion(codigoCliente, codigoProducto, codigoEmpresa, codigoSucursal);
				if (!itemsRemitos.isEmpty()){					
					for(ItemRemito itemRemito: itemsRemitos){
						if (cantidadTotal.compareTo(BigDecimal.ZERO) > 0){
							ItemPendienteLiquidacionConsignacionProxy itemPendiente = itemRemito.itemPendienteLiquidacionProxy();
							BigDecimal pendienteLiquidacion = itemPendiente.getCantidadPendiente();
							BigDecimal cantidadFacturarItem = BigDecimal.ZERO;
							BigDecimal cantidadDevolverItem = BigDecimal.ZERO;
							
							if (cantidadFacturar.compareTo(BigDecimal.ZERO) > 0){
								if (cantidadFacturar.compareTo(pendienteLiquidacion) < 0){
									cantidadFacturarItem = cantidadFacturar;
									pendienteLiquidacion = pendienteLiquidacion.subtract(cantidadFacturarItem);
								}
								else{
									cantidadFacturarItem = pendienteLiquidacion;
									pendienteLiquidacion = BigDecimal.ZERO;
								}
							}
							if ((cantidadDevolver.compareTo(BigDecimal.ZERO) > 0) && (pendienteLiquidacion.compareTo(BigDecimal.ZERO) > 0)){
								if (cantidadDevolver.compareTo(pendienteLiquidacion) < 0){
									cantidadDevolverItem = cantidadDevolver;
									pendienteLiquidacion = pendienteLiquidacion.subtract(cantidadDevolverItem);
								}
								else{
									cantidadDevolverItem = pendienteLiquidacion;
									pendienteLiquidacion = BigDecimal.ZERO;
								}
							}
							
							itemPendiente.asignarCantidadesParaLiquidacion(cantidadFacturarItem, cantidadDevolverItem);
							cantidadFacturar = cantidadFacturar.subtract(cantidadFacturarItem);
							cantidadDevolver = cantidadDevolver.subtract(cantidadDevolverItem);
							cantidadTotal = cantidadFacturar.add(cantidadDevolver);
							this.getItemsPendientes().add(itemPendiente);
						}
						else{
							break;
						}												
					}
					
					if (cantidadTotal.compareTo(BigDecimal.ZERO) != 0){
						throw new ValidationException("El cantidad a facturar y/o devolver supera el pendiente de liquidación en " + cantidadTotal.toString());
					}
				}
				else{
					throw new ValidationException("No se encontró un item pendiente de liquidación por consignación para el cliente " + codigoCliente + " y el producto " + codigoProducto);
				}	
			}
			else{
				throw new ValidationException("Ya se procesó el cliente " + codigoCliente + " y producto " + codigoProducto + " en empresa " + codigoEmpresa);
			}
		}	
		else{
			throw new ValidationException(errores);
		}
	}

	@Override
	public String getNextAction() throws Exception {
		if (!this.getItemsPendientes().isEmpty() && this.getErrors().isEmpty()){
			this.getRequest().setAttribute("ItemsPendientes", this.getItemsPendientes());
			return "ProcesarItemsPendientes.CumplirItemsPendientes";
		}
		else{
			return null;
		}
	}

	@Override
	protected void posProcesarCSV() throws Exception {		
	}
	
	@Override
	protected Boolean commitParcial(){
		return Boolean.FALSE;
	}
}
