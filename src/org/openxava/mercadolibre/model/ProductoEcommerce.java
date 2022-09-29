package org.openxava.mercadolibre.model;

import java.math.*;
import java.util.*;

import org.Mercadolibre.*;
import org.openxava.util.*;
import org.openxava.validators.*;

import com.tiendanube.base.*;

public class ProductoEcommerce {
	
/*
 * 	i.id, i.idProducto, i.idMercadoLibre, i.producto_id, r.unidadmedida_id 
 */
	
	private String idProducto;
	
	private String idPublicacion;
	
	private String idMercadolibre;
	
	private String idProductoCloud;
	
	private String idUnidadMedida;
		
	private SesionMercadoLibre sesion;
	
	private ConfiguracionMercadoLibre configuracionEcommerce;

	public void actualizarPrecio(ProductoEcommerce producto, BigDecimal precio) throws Exception {
	}

	public void actualizarStock(ProductoEcommerce producto, BigDecimal stock) throws Exception {
	}

	public String getIdProducto() {
		return idProducto;
	}

	public void setIdProducto(String idProducto) {
		this.idProducto = idProducto;
	}

	public String getIdPublicacion() {
		return idPublicacion;
	}

	public void setIdPublicacion(String idPublicacion) {
		this.idPublicacion = idPublicacion;
	}

	public String getIdMercadolibre() {
		return idMercadolibre;
	}

	public void setIdMercadolibre(String idMercadolibre) {
		this.idMercadolibre = idMercadolibre;
	}

	public String getIdProductoCloud() {
		return idProductoCloud;
	}

	public void setIdProductoCloud(String idProductoCloud) {
		this.idProductoCloud = idProductoCloud;
	}

	public String getIdUnidadMedida() {
		return idUnidadMedida;
	}

	public void setIdUnidadMedida(String idUnidadMedida) {
		this.idUnidadMedida = idUnidadMedida;
	}

	public ConexionMeli getMiConexion() {
		if(this.sesion == null){
			throw new ValidationException("Problemas con la sesion");
		}
		return this.getSesion().conectar(this.getConfiguracionEcommerce());
	}

	public TiendaNube getMiTienda() {
		if(this.sesion == null){
			throw new ValidationException("Problemas con la sesion");
		}
		return this.sesion.conectarTiendaNube(this.getConfiguracionEcommerce());
	}

	public SesionMercadoLibre getSesion() {
		return sesion;
	}

	public void setSesion(SesionMercadoLibre sesion) {
		this.sesion = sesion;
	}

	public void auditoriaStock(PublicacionML publicacionml, BigDecimal stock) {	
		publicacionml.setFechaActualizacionStock(new Date());
		publicacionml.setUsuarioActualizacionStock(Users.getCurrent());
		publicacionml.setUltimoStock(stock);

	}
	
	
	public void auditoriaPrecio(PublicacionML publicacionml, BigDecimal precio) {
		publicacionml.setFechaActualizacionPrecio(new Date());
		publicacionml.setUsuarioActualizacionPrecio(Users.getCurrent());
		publicacionml.setUltimoPrecio(precio);
		
	}

	public ConfiguracionMercadoLibre getConfiguracionEcommerce() {
		return configuracionEcommerce;
	}

	public void setConfiguracionEcommerce(ConfiguracionMercadoLibre configuracionEcommerce) {
		this.configuracionEcommerce = configuracionEcommerce;
	}
	
}
