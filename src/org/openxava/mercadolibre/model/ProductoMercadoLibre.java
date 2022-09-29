package org.openxava.mercadolibre.model;

import java.math.*;

import org.Mercadolibre.*;



public class ProductoMercadoLibre extends ProductoEcommerce{
	

	@Override
	public void actualizarPrecio(ProductoEcommerce producto,BigDecimal precio) throws Exception {
		MLItem mlItem = new MLItem(producto.getIdMercadolibre());
		mlItem.setPrecio(precio);
		mlItem.setVariacionSeleccionada(producto.getIdProducto());
		producto.getMiConexion().actualizar(mlItem);
	}
	
	@Override
	public void actualizarStock(ProductoEcommerce producto, BigDecimal stock) throws Exception {
		MLItem mlItem = new MLItem(producto.getIdMercadolibre());
		mlItem.setStock(stock);
		mlItem.setVariacionSeleccionada(producto.getIdProducto());
		producto.getMiConexion().actualizar(mlItem);
	}


}
