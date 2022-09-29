package org.openxava.mercadolibre.model;

import java.math.*;

import com.tiendanube.model.*;

public class ProductoTiendaNube extends ProductoEcommerce {

	
	@Override
	public void actualizarPrecio(ProductoEcommerce producto, BigDecimal precio) throws Exception {
		Product productApi = new Product();
		productApi.setPrice(precio.toString());
		productApi.updatePrice(Integer.valueOf(producto.getIdProducto()), Integer.valueOf(producto.getIdMercadolibre()), producto.getMiTienda());
		
	}
	
	@Override
	public void actualizarStock(ProductoEcommerce producto, BigDecimal stock) throws Exception {
		Product productApi = new Product();
		productApi.setStock(stock.intValue());
		productApi.updateStock(Integer.valueOf(producto.getIdProducto()), Integer.valueOf(producto.getIdMercadolibre()), producto.getMiTienda());

	}
	
}
