package org.openxava.mercadolibre.actions;

import java.math.BigDecimal;
import java.util.*;

import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openxava.actions.*;
import org.openxava.base.model.Esquema;
import org.openxava.inventario.model.Inventario;
import org.openxava.jpa.XPersistence;
import org.openxava.mercadolibre.model.*;
import org.openxava.model.*;
import org.openxava.util.*;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.*;

import com.tiendanube.base.*;

public class ActualizarProductosEcommerceAction extends TabBaseAction{
	
	private static final int LIMITEERRORES = 15;

	private static Log log = LogFactory.getLog(ActualizarProductosEcommerceAction.class);
	
	private String aModificar;

	private boolean seleccionados = false;

	private int contador = 0;
	private int errores = 0;

	private SesionMercadoLibre sesion;
	
	private ConfiguracionMercadoLibre ecommerce;
	
	@Override
	public void execute() throws Exception {
		
		this.sesion = SesionesMercadoLibre.getSesion();

		if(!getTab().hasSelected()){
			if(seleccionados){
				addMessage("Por favor use el boton que no dice seleccionados");
			}else{
				if(!Is.empty(this.getView().getValue("ecommerce.id"))){
					this.setEcommerce((ConfiguracionMercadoLibre)XPersistence.getManager().find(ConfiguracionMercadoLibre.class, this.getView().getValue("ecommerce.id")));
				}else{
					throw new ValidationException("Debe seleccionar un configurador para poder continuar.");
				}
				this.setaModificar(this.getRequest().getSession().getAttribute("aModificar").toString());
				this.setSeleccionados ((boolean)this.getRequest().getSession().getAttribute("seleccionados"));
				this.getRequest().getSession().removeAttribute("aModificar");
				this.getRequest().getSession().removeAttribute("seleccionados");
				this.actualizarProductos(this.getEcommerce());
			}
		}else{
			if(!seleccionados){
				addMessage("Por favor use el boton que dice seleccionados");
			}else{
				for (Map<?, ?> key: getTab().getSelectedKeys()) { 
					PublicacionML publicacion = (PublicacionML)MapFacade.findEntity("PublicacionML", key); 
					ProductoEcommerce actualizacion = new ProductoEcommerce();
					actualizacion.setSesion(sesion);
					actualizacion.setIdMercadolibre(publicacion.getIdMercadoLibre());
					actualizacion.setIdProducto(publicacion.getIdProducto());
					actualizacion.setIdProductoCloud(publicacion.getProducto().getId());
					actualizacion.setIdUnidadMedida(publicacion.getProducto().getUnidadMedida().getId());
					actualizacion.setIdPublicacion(publicacion.getId());
					actualizacion.setConfiguracionEcommerce(publicacion.getConfiguracionEcommerce());
					String idListaPrecio = publicacion.getConfiguracionEcommerce().getListaPrecio().getId();
					String idDeposito = publicacion.getConfiguracionEcommerce().getStockMercadoLibre().getId();
					this.actualizarProducto(actualizacion,idListaPrecio,idDeposito);
				}
				getTab().deselectAll();
				addMessage("Se actualizaron " + contador + " productos ");
			}
		}
		this.closeDialog();
		
	}


	private void actualizarProductos(ConfiguracionMercadoLibre ecommerce){
		
		String ordenarPor = "fechaActualizacionStock";
		if (getaModificar().equals("Precio")){
			ordenarPor = "fechaActualizacionPrecio";	
		}
		
		Query query = this.queryPublicaciones(ecommerce, ordenarPor);
		
		List<?> publicaciones = query.getResultList();
		for(Object ob: publicaciones){
			ProductoEcommerce actualizacion = new ProductoEcommerce();
			actualizacion.setIdMercadolibre((String)((Object[])ob)[2]);
			actualizacion.setIdProducto((String)((Object[])ob)[1]);
			actualizacion.setIdProductoCloud((String)((Object[])ob)[3]);
			actualizacion.setIdPublicacion((String)((Object[])ob)[0]);
			actualizacion.setIdUnidadMedida((String)((Object[])ob)[4]);
			actualizacion.setSesion(sesion);
//			if(idConfigurador == null || idConfigurador.isEmpty()){
//				addError("No hay configuracion definido para la publicacion con id: "+actualizacion.getIdPublicacion());
//				errores++;
//				continue;
//			}
			actualizacion.setConfiguracionEcommerce(ecommerce);
			String idListaPrecio = actualizacion.getConfiguracionEcommerce().getListaPrecio().getId();
			String idDeposito = actualizacion.getConfiguracionEcommerce().getStockMercadoLibre().getId();
			this.actualizarProducto(actualizacion, idListaPrecio, idDeposito);
		}
		addError("Se encontraron "+ errores +" errores al actualizar");
		addMessage("Se actualizaron " + contador + " productos del configurador: " + ecommerce.getNombre());

	}
	
	private void actualizarProducto(ProductoEcommerce prodActualizar, String idListaPrecio, String idDeposito){
		if(getaModificar().equals("Precio")){
			try{
				soloPrecio(idListaPrecio, prodActualizar);
				if(prodActualizar.getConfiguracionEcommerce().getEcommerce().equals(Ecommerce.MercadoLibre)){
					prodActualizar.getMiConexion().desconectar();
				}			
			}catch(ValidationException e){
				this.procesarErrores(e, prodActualizar.getIdPublicacion());
			}
		}
		if(getaModificar().equals("Stock")){
			try{
				soloStock(idListaPrecio,idDeposito, prodActualizar);
				if(prodActualizar.getConfiguracionEcommerce().getEcommerce().equals(Ecommerce.MercadoLibre)){
					prodActualizar.getMiConexion().desconectar();
				}	
			}catch(ValidationException e){
				this.procesarErrores(e,prodActualizar.getIdPublicacion());
				
			}
		}
	}

	
	private void procesarErrores(ValidationException e, String idPublicacion) {
		if(getErrors().getStrings().size() == LIMITEERRORES){
			addError("y aún mas errores..");
		}else if(getErrors().getStrings().size() < LIMITEERRORES){
			addErrors(e.getErrors());					
			log.error(idPublicacion + " " + e.getErrors());
			errores++;
		}else if(getErrors().getStrings().size() > LIMITEERRORES){
			//addErrors(e.getErrors());					
			log.error(idPublicacion + " " + e.getErrors());
			errores++;
		}		
	}


	public void soloPrecio(String idListaPrecio,ProductoEcommerce actualizacion) throws ValidationException{
		ListaPrecio listaEcommerce = XPersistence.getManager().find(ListaPrecio.class, idListaPrecio);
		BigDecimal newPrice = listaEcommerce.buscarPrecio(actualizacion.getIdProductoCloud(), actualizacion.getIdUnidadMedida(),new BigDecimal(1));
		PublicacionML publicacionml = null;
		if(newPrice != null){
			try{
				ProductoEcommerce prodEcommerce = actualizacion.getConfiguracionEcommerce().getEcommerce().strategyEcommerce();	
				prodEcommerce.actualizarPrecio(actualizacion, newPrice);
				publicacionml = XPersistence.getManager().find(PublicacionML.class, actualizacion.getIdPublicacion(), LockModeType.PESSIMISTIC_WRITE);
				prodEcommerce.auditoriaPrecio(publicacionml, newPrice);
				this.commit();
				contador++;	
				
			}catch(ApiException e){
				this.rollback();
				throw new ValidationException("No fue posible encontrar la publicacion con id de mercadolibre: "+actualizacion.getIdMercadolibre() 
				+" debe marcar la publicacion como eliminada. "+ e.getMessage());	
			}catch(Exception e){
				this.rollback();
				throw new ValidationException("No fue posible actualizar el producto de "+ actualizacion.getConfiguracionEcommerce().getEcommerce() +
						 "con id mercado libre: "+actualizacion.getIdMercadolibre() + " - " + e.getMessage());	
			}
		}else {
				Producto productoConProblemas = XPersistence.getManager().find(Producto.class,actualizacion.getIdProductoCloud());
				throw new ValidationException("No esta definido el precio en la lista " + listaEcommerce.getCodigo() + " para el producto " +  productoConProblemas.getCodigo() );
		}
	}
	
	private void soloStock(String idListaPrecio,String idDeposito, ProductoEcommerce actualizacion) throws ValidationException {
		BigDecimal newStock = Inventario.buscarStockPorId(idDeposito, actualizacion.getIdProductoCloud() , new HashMap<String,String>());
		PublicacionML publicacionml = null;
		if(newStock == null){
			newStock = BigDecimal.ZERO;
		}		
		try{
			ProductoEcommerce prodEcommerce = actualizacion.getConfiguracionEcommerce().getEcommerce().strategyEcommerce();
			prodEcommerce.actualizarStock(actualizacion, newStock);
			publicacionml = XPersistence.getManager().find(PublicacionML.class, actualizacion.getIdPublicacion(), LockModeType.PESSIMISTIC_WRITE);		
			prodEcommerce.auditoriaStock(publicacionml,newStock);
			this.commit();
			contador++;
		}catch(Exception e){
			this.rollback();
			throw new ValidationException("No fue posible actualizar el producto de "+ actualizacion.getConfiguracionEcommerce().getEcommerce() +
					 " con id variante: "+actualizacion.getIdMercadolibre() + " - " + e.getMessage());			
		}		
	}

	private Query queryPublicaciones(ConfiguracionMercadoLibre ecommerce, String ordenarPor){		
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT i.id, i.idProducto, i.idMercadoLibre, i.producto_id, r.unidadmedida_id FROM ");
		sql.append(Esquema.concatenarEsquema("publicacionml i JOIN "));
		sql.append(Esquema.concatenarEsquema("producto r "));
		sql.append("ON r.id = i.producto_id ");
		sql.append("WHERE i.configuracionecommerce_id = :ecommerce AND i.estado =:estadoPublicacionML ");
		sql.append("ORDER BY i." + ordenarPor + " asc ");
		Query query = XPersistence.getManager().createNativeQuery(sql.toString());
		query.setParameter("estadoPublicacionML", EstadoPublicacionML.Publicada.ordinal());
		query.setParameter("ecommerce", ecommerce.getId());
		query.setFlushMode(FlushModeType.COMMIT);
		
		return query;		
	}
	
	public String getaModificar() {
		return aModificar;
	}

	public void setaModificar(String aModificar) {
		this.aModificar = aModificar;
	}

	public boolean isSeleccionados() {
		return seleccionados;
	}

	public void setSeleccionados(boolean seleccionados) {
		this.seleccionados = seleccionados;
	}


	public ConfiguracionMercadoLibre getEcommerce() {
		return ecommerce;
	}

	public void setEcommerce(ConfiguracionMercadoLibre ecommerce) {
		this.ecommerce = ecommerce;
	}

}
