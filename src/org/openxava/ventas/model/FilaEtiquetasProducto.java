package org.openxava.ventas.model;

import java.math.*;

import org.openxava.validators.*;

public class FilaEtiquetasProducto {

	public static final int MAXETIQUETASPORFILA = 5;
	
	private int cantidadEtiquetasPorFila = 0;
	
	private int cantidadEtiquetasUsadas = 0;
		
	private void setCantidadEtiquetasPorFila(int cantidadEtiquetasPorFila) {
		if (cantidadEtiquetasPorFila > MAXETIQUETASPORFILA){
			throw new ValidationException("La cantidad máxima de etiquetas por fila posibles son " + Integer.toString(MAXETIQUETASPORFILA));
		}
		this.cantidadEtiquetasPorFila = cantidadEtiquetasPorFila;
	}

	public FilaEtiquetasProducto(Integer cantidadEtiquetasPorFila) {
		this.setCantidadEtiquetasPorFila(cantidadEtiquetasPorFila);
	}

	public boolean estaCompleta() {
		return this.cantidadEtiquetasUsadas >= this.cantidadEtiquetasPorFila;
	}
	
	public void agregarEtiqueta(Producto producto, BigDecimal precio) {
		if (!this.estaCompleta()){
			this.cantidadEtiquetasUsadas++;
			
			int subindice = this.cantidadEtiquetasUsadas;
			
			try{
				this.getClass().getMethod("setCodigo" + Integer.toString(subindice), String.class).invoke(this, producto.getCodigo());
				this.getClass().getMethod("setDescripcion" + Integer.toString(subindice), String.class).invoke(this, producto.getNombre());
				this.getClass().getMethod("setIdProducto" + Integer.toString(subindice), String.class).invoke(this, producto.getId());
				this.getClass().getMethod("setPrecio" + Integer.toString(subindice), BigDecimal.class).invoke(this, precio);
				this.getClass().getMethod("setPrecioMasIva" + Integer.toString(subindice), BigDecimal.class).invoke(this, producto.agregarIva(precio));
				String nombreProveedor = "";
				String codigoProveedor = "";
				if (producto.getProveedor() != null){
					nombreProveedor = producto.getProveedor().getNombre();
					codigoProveedor = producto.getProveedor().getCodigo();
				}
				this.getClass().getMethod("setCodigoProveedor" + Integer.toString(subindice), String.class).invoke(this, codigoProveedor);
				this.getClass().getMethod("setNombreProveedor" + Integer.toString(subindice), String.class).invoke(this, nombreProveedor);
			}
			catch(Exception e){
				throw new ValidationException("No se pudo agregar etiqueta: " + e.toString());
			}			
		}
		else{
			throw new ValidationException("No se puede agregar etiquetas, ya están completas");
		}
	}
	
	
	// La fila tiene todos los campos de cada etiqueta con un subindice
	private String codigo1;
	private String descripcion1;
	private String idProducto1;
	private BigDecimal precio1;
	private String codigoProveedor1;
	private String nombreProveedor1;
	private BigDecimal precioMasIva1;
	
	private String codigo2;
	private String descripcion2;
	private String idProducto2;
	private BigDecimal precio2;
	private String codigoProveedor2;
	private String nombreProveedor2;
	private BigDecimal precioMasIva2;
	
	private String codigo3;
	private String descripcion3;
	private String idProducto3;
	private BigDecimal precio3;
	private String codigoProveedor3;
	private String nombreProveedor3;
	private BigDecimal precioMasIva3;
	
	private String codigo4;
	private String descripcion4;
	private String idProducto4;
	private BigDecimal precio4;
	private String codigoProveedor4;
	private String nombreProveedor4;
	private BigDecimal precioMasIva4;

	private String codigo5;
	private String descripcion5;
	private String idProducto5;
	private BigDecimal precio5;
	private String codigoProveedor5;
	private String nombreProveedor5;
	private BigDecimal precioMasIva5;

	public String getCodigo1() {
		return codigo1;
	}

	public void setCodigo1(String codigo1) {
		this.codigo1 = codigo1;
	}

	public BigDecimal getPrecio1() {
		return precio1;
	}

	public void setPrecio1(BigDecimal precio1) {
		this.precio1 = precio1;
	}

	public String getCodigo2() {
		return codigo2;
	}

	public void setCodigo2(String codigo2) {
		this.codigo2 = codigo2;
	}

	public BigDecimal getPrecio2() {
		return precio2;
	}

	public void setPrecio2(BigDecimal precio2) {
		this.precio2 = precio2;
	}

	public String getCodigo3() {
		return codigo3;
	}

	public void setCodigo3(String codigo3) {
		this.codigo3 = codigo3;
	}

	public BigDecimal getPrecio3() {
		return precio3;
	}

	public void setPrecio3(BigDecimal precio3) {
		this.precio3 = precio3;
	}

	public String getCodigo4() {
		return codigo4;
	}

	public void setCodigo4(String codigo4) {
		this.codigo4 = codigo4;
	}

	public BigDecimal getPrecio4() {
		return precio4;
	}

	public void setPrecio4(BigDecimal precio4) {
		this.precio4 = precio4;
	}

	public String getCodigo5() {
		return codigo5;
	}

	public void setCodigo5(String codigo5) {
		this.codigo5 = codigo5;
	}

	public BigDecimal getPrecio5() {
		return precio5;
	}

	public void setPrecio5(BigDecimal precio5) {
		this.precio5 = precio5;
	}

	public String getDescripcion1() {
		return descripcion1;
	}

	public void setDescripcion1(String descripcion1) {
		this.descripcion1 = descripcion1;
	}

	public String getIdProducto1() {
		return idProducto1;
	}

	public void setIdProducto1(String idProducto1) {
		this.idProducto1 = idProducto1;
	}

	public String getDescripcion2() {
		return descripcion2;
	}

	public void setDescripcion2(String descripcion2) {
		this.descripcion2 = descripcion2;
	}

	public String getIdProducto2() {
		return idProducto2;
	}

	public void setIdProducto2(String idProducto2) {
		this.idProducto2 = idProducto2;
	}

	public String getDescripcion3() {
		return descripcion3;
	}

	public void setDescripcion3(String descripcion3) {
		this.descripcion3 = descripcion3;
	}

	public String getIdProducto3() {
		return idProducto3;
	}

	public void setIdProducto3(String idProducto3) {
		this.idProducto3 = idProducto3;
	}

	public String getDescripcion4() {
		return descripcion4;
	}

	public void setDescripcion4(String descripcion4) {
		this.descripcion4 = descripcion4;
	}

	public String getIdProducto4() {
		return idProducto4;
	}

	public void setIdProducto4(String idProducto4) {
		this.idProducto4 = idProducto4;
	}

	public String getDescripcion5() {
		return descripcion5;
	}

	public void setDescripcion5(String descripcion5) {
		this.descripcion5 = descripcion5;
	}

	public String getIdProducto5() {
		return idProducto5;
	}

	public void setIdProducto5(String idProducto5) {
		this.idProducto5 = idProducto5;
	}

	public String getCodigoProveedor1() {
		return codigoProveedor1;
	}

	public void setCodigoProveedor1(String codigoProveedor1) {
		this.codigoProveedor1 = codigoProveedor1;
	}

	public String getNombreProveedor1() {
		return nombreProveedor1;
	}

	public void setNombreProveedor1(String nombreProveedor1) {
		this.nombreProveedor1 = nombreProveedor1;
	}

	public String getCodigoProveedor2() {
		return codigoProveedor2;
	}

	public void setCodigoProveedor2(String codigoProveedor2) {
		this.codigoProveedor2 = codigoProveedor2;
	}

	public String getNombreProveedor2() {
		return nombreProveedor2;
	}

	public void setNombreProveedor2(String nombreProveedor2) {
		this.nombreProveedor2 = nombreProveedor2;
	}

	public String getCodigoProveedor3() {
		return codigoProveedor3;
	}

	public void setCodigoProveedor3(String codigoProveedor3) {
		this.codigoProveedor3 = codigoProveedor3;
	}

	public String getNombreProveedor3() {
		return nombreProveedor3;
	}

	public void setNombreProveedor3(String nombreProveedor3) {
		this.nombreProveedor3 = nombreProveedor3;
	}

	public String getCodigoProveedor4() {
		return codigoProveedor4;
	}

	public void setCodigoProveedor4(String codigoProveedor4) {
		this.codigoProveedor4 = codigoProveedor4;
	}

	public String getNombreProveedor4() {
		return nombreProveedor4;
	}

	public void setNombreProveedor4(String nombreProveedor4) {
		this.nombreProveedor4 = nombreProveedor4;
	}

	public String getCodigoProveedor5() {
		return codigoProveedor5;
	}

	public void setCodigoProveedor5(String codigoProveedor5) {
		this.codigoProveedor5 = codigoProveedor5;
	}

	public String getNombreProveedor5() {
		return nombreProveedor5;
	}

	public void setNombreProveedor5(String nombreProveedor5) {
		this.nombreProveedor5 = nombreProveedor5;
	}

	public BigDecimal getPrecioMasIva1() {
		return precioMasIva1;
	}

	public void setPrecioMasIva1(BigDecimal precioMasIva1) {
		this.precioMasIva1 = precioMasIva1;
	}

	public BigDecimal getPrecioMasIva2() {
		return precioMasIva2;
	}

	public void setPrecioMasIva2(BigDecimal precioMasIva2) {
		this.precioMasIva2 = precioMasIva2;
	}

	public BigDecimal getPrecioMasIva3() {
		return precioMasIva3;
	}

	public void setPrecioMasIva3(BigDecimal precioMasIva3) {
		this.precioMasIva3 = precioMasIva3;
	}

	public BigDecimal getPrecioMasIva4() {
		return precioMasIva4;
	}

	public void setPrecioMasIva4(BigDecimal precioMasIva4) {
		this.precioMasIva4 = precioMasIva4;
	}

	public BigDecimal getPrecioMasIva5() {
		return precioMasIva5;
	}

	public void setPrecioMasIva5(BigDecimal precioMasIva5) {
		this.precioMasIva5 = precioMasIva5;
	}
}
