package org.openxava.compras.actions;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedList;

import org.openxava.base.actions.ProcesarCSVGenericoAction;
import org.openxava.base.model.Empresa;
import org.openxava.compras.model.FacturaCompra;
import org.openxava.compras.model.ImpuestoCompra;
import org.openxava.compras.model.ItemCompraElectronica;
import org.openxava.compras.model.Proveedor;
import org.openxava.contabilidad.model.CentroCostos;
import org.openxava.impuestos.model.Impuesto;
import org.openxava.impuestos.model.TasaImpuesto;
import org.openxava.jpa.XPersistence;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.Producto;

import com.csvreader.CsvReader;

public class ProcesarCSVFacturaCompraAction extends ProcesarCSVGenericoAction{

	private FacturaCompra ultimaFactura = null;
	
	@Override
	protected void preProcesarCSV() throws Exception {
		this.ultimaFactura = null;
	}

	@Override
	protected void posProcesarCSV() throws Exception {
		if (this.ultimaFactura != null){
			Collection<ItemCompraElectronica> items = this.ultimaFactura.getItems();
			
			// se inicializan la lista de items para que al grabar la factura no tenga items
			this.ultimaFactura.setItems(new LinkedList<ItemCompraElectronica>());
			XPersistence.getManager().persist(this.ultimaFactura);
			
			for(ItemCompraElectronica item: items){
				XPersistence.getManager().persist(item);
			}
			this.ultimaFactura.setItems(items);
			this.ultimaFactura.grabarTransaccion();
			this.commit();
		}
	}

	@Override
	protected void procesarLineaCSV(CsvReader csvReader) throws IOException {
		String numeroFactura = csvReader.get(2);
		if(Is.emptyString(numeroFactura)){
			throw new ValidationException("Falta asignar número de factura");
		}
		
		if (this.nuevaFactura(numeroFactura)){
			this.crearFactura(numeroFactura, csvReader);
		}
		this.cargarItems(csvReader);		
	}
	
	private void crearFactura(String numero, CsvReader csvReader) throws IOException {
		this.ultimaFactura = new FacturaCompra();
		this.ultimaFactura.setNumero(numero);
		this.ultimaFactura.setItems(new LinkedList<ItemCompraElectronica>());
		this.ultimaFactura.setImpuestos(new LinkedList<ImpuestoCompra>());
		this.ultimaFactura.setFecha(this.convertirStrFecha(csvReader.get(0)));
		this.ultimaFactura.setFechaReal(this.convertirStrFecha(csvReader.get(1)));
		this.ultimaFactura.setNumero(csvReader.get(2));		
		this.ultimaFactura.setEmpresa((Empresa)Empresa.buscarPorCodigoError(csvReader.get(3), Empresa.class.getSimpleName()));
		this.ultimaFactura.setProveedor((Proveedor)Proveedor.buscarPorCodigoError(csvReader.get(4), Proveedor.class.getSimpleName()));				
		this.ultimaFactura.setFechaVencimiento(this.convertirStrFecha(csvReader.get(5)));					
	}
	
	private boolean nuevaFactura(String numero){
		if (this.ultimaFactura == null){
			return true;
		}
		else{
			return !Is.equalAsString(numero, this.ultimaFactura.getNumero());
		}
	}
	
	private void cargarItems(CsvReader csvReader) throws IOException {
		if (!Is.emptyString(csvReader.get(6))){
			ImpuestoCompra itemImpuesto = new ImpuestoCompra();
			// se crea un impuesto
			itemImpuesto.setImpuesto((Impuesto)Impuesto.buscarPorCodigoError(csvReader.get(6), Impuesto.class.getSimpleName())); 
			if(!Is.emptyString(csvReader.get(7))){
				itemImpuesto.setImporte(this.convertirStrDecimal(csvReader.get(7)));
				if (!Is.emptyString(csvReader.get(8))){
					itemImpuesto.setAlicuota(this.convertirStrDecimal(csvReader.get(8)));
				}
			}
			else{
				throw new ValidationException("Falta asignar el importe para el impuesto " + csvReader.get(6));
			}
			this.ultimaFactura.getImpuestos().add(itemImpuesto);
		}
		
		if (!Is.emptyString(csvReader.get(9))){
			ItemCompraElectronica itemFactura = new ItemCompraElectronica();
			// se crea un item de factura
			int indice = 9;
			if(!Is.emptyString(csvReader.get(indice))){
				Producto producto = (Producto)Producto.buscarPorCodigoError(csvReader.get(indice), Producto.class.getSimpleName());
				itemFactura.setProducto(producto);
				
			}else throw new ValidationException("Producto no asignado en factura nro: " + csvReader.get(indice));
			
			indice++;		
			itemFactura.setCantidad(this.convertirStrDecimal(csvReader.get(indice)));
			indice++;
			if (!Is.emptyString(csvReader.get(indice))){
				BigDecimal precioUnit = this.convertirStrDecimal(csvReader.get(indice));
				if (precioUnit.compareTo(BigDecimal.ZERO) < 0){
					throw new ValidationException("Precio en negativo " + csvReader.get(indice));
				}
				itemFactura.setPrecioUnitario(precioUnit);;
			}
			else{
				throw new ValidationException("Precio unitario no asignado");
			}
			indice++;
			if(!Is.emptyString(csvReader.get(indice))){
				TasaImpuesto tasa = (TasaImpuesto) TasaImpuesto.buscarTasaPorPorcentaje(new BigDecimal(csvReader.get(indice)));
				itemFactura.setAlicuotaIva(tasa);			
			}
			indice++;
			if(!Is.emptyString(csvReader.get(indice))){
				CentroCostos ccostos = (CentroCostos) CentroCostos.buscarPorCodigoError(csvReader.get(indice), CentroCostos.class.getSimpleName());
				itemFactura.setCentroCostos(ccostos);			
			}
			itemFactura.setCompra(this.ultimaFactura);
			itemFactura.recalcular();			
			this.ultimaFactura.getItems().add(itemFactura);
		}
		
	}
	
	@Override
	protected Boolean commitParcial(){
		return Boolean.FALSE;
	}


}
