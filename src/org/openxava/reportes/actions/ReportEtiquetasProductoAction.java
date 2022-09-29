package org.openxava.reportes.actions;

import java.math.*;
import java.util.*;

import org.openxava.base.model.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.*;

public class ReportEtiquetasProductoAction extends ReportBaseConcatAction{

	private Collection<FilaEtiquetasProducto> etiquetas;
	
	private int cantidad = 1;
	
	private Integer numero = 1;
	
	private void setCantidad(Integer cant){
		this.cantidad = 1;
		if (cant != null){
			if (cant != 0){
				this.cantidad = cant;
			}
		}
	}
	
	private void setNumero(Integer num){
		this.numero = 1;
		if (num != null){
			if (num != 0){
				this.numero = num;
			}
		}
	}
	
	@Override
	public void execute() throws Exception {
		this.setCantidad((Integer)this.getRequest().getAttribute("cantidad"));
		this.setNumero((Integer)this.getRequest().getAttribute("numero"));
		ListaPrecio listaPrecio = (ListaPrecio)this.getRequest().getAttribute("listaPrecio");
		@SuppressWarnings("unchecked")
		Collection<Producto> productos = (Collection<Producto>)this.getRequest().getAttribute("productos");
		
		this.getRequest().removeAttribute("cantidad");
		this.getRequest().removeAttribute("numero");
		this.getRequest().removeAttribute("listaPrecio");
		this.getRequest().removeAttribute("productos");
		
		this.etiquetas = new LinkedList<FilaEtiquetasProducto>();
		this.generarEtiquetas(this.etiquetas, productos, listaPrecio);
		
		if (this.etiquetas.isEmpty()){
			throw new ValidationException("No se pudieron generar las etiquetas");
		}
		
		super.execute();
		
		this.deseleccionarElementos(true);	
	}
	
	@Override
	protected String[] getNombresReportes() {
		String nombreReporte = "EtiquetasProducto.jrxml";
		if (this.numero > 1) nombreReporte = "EtiquetasProducto" + this.numero.toString() + ".jrxml";
		
		String[] reportes = new String[this.cantidad];
		for(int i = 0; i < this.cantidad; i++){
			reportes[i] = nombreReporte;
		}
		return reportes;
	}
	

	@Override
	protected JRDataSource[] getDataSources() throws Exception {
		JRDataSource[] data = new JRDataSource[this.cantidad];
		for(int i = 0; i < this.cantidad; i++){
			data[i] = new JRBeanCollectionDataSource(this.etiquetas);
		}
		return data;		
	}
	
	private void generarEtiquetas(Collection<FilaEtiquetasProducto> etiquetas, Collection<Producto> productos, ListaPrecio lista){
		Integer cantidadEtiquetasPorFila = Esquemas.getEsquemaApp().getEtiquetasPorFila();
		FilaEtiquetasProducto fila = null;
		for(Producto producto: productos){
			BigDecimal precio = BigDecimal.ZERO;
			if (lista != null){
				Precio pre = lista.buscarObjetoPrecioSinCantidad(producto.getId(), producto.getUnidadMedida().getId());
				if (pre != null){
					precio = pre.getImporte();
				}
			}
			for(int i = 1; i <= cantidadEtiquetasPorFila; i++){
				if ((fila == null) || fila.estaCompleta()){
					fila = new FilaEtiquetasProducto(cantidadEtiquetasPorFila);
					
					etiquetas.add(fila);
				}				
				fila.agregarEtiqueta(producto, precio);
			}
		}
	}
}
