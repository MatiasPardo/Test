package org.openxava.mercadolibre.model;

public enum Ecommerce {
	
	MercadoLibre{
		@Override
		public ProductoEcommerce strategyEcommerce(){
			ProductoMercadoLibre ml = new ProductoMercadoLibre();
			return ml;
		}
		
	}, TiendaNube{
		@Override
		public ProductoEcommerce strategyEcommerce(){
			ProductoTiendaNube tn = new ProductoTiendaNube();
			return tn;
		}
	};
	
	public abstract ProductoEcommerce strategyEcommerce();
	
}
