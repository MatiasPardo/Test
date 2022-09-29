package org.openxava.mercadolibre.actions;

import java.util.*;

import org.apache.commons.logging.*;
import org.openxava.actions.*;
import org.openxava.jpa.*;
import org.openxava.mercadolibre.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

import com.tiendanube.base.*;
import com.tiendanube.model.*;

public class ImportarPublicacionesTiendaNubeAction extends ViewBaseAction{

	private static final int MOSTRARERRORES = 50;

	private static Log log = LogFactory.getLog(ImportarPublicacionesTiendaNubeAction.class);

	private TiendaNube miTienda;
	
	private int errores = 0;
 
	private int importados;
	
	private int yaCreados = 0;
	
	@Override
	public void execute() throws Exception {
		SesionMercadoLibre sesion = SesionesMercadoLibre.getSesion();
		List<ConfiguracionMercadoLibre> configuradores = new LinkedList<ConfiguracionMercadoLibre>();
		try{
			configuradores = ConfiguracionMercadoLibre.buscarConfiguradores(Ecommerce.TiendaNube);
		}catch(ValidationException e){
			throw new ValidationException(e.getMessage());
		}
		for(ConfiguracionMercadoLibre con: configuradores){
			miTienda = sesion.conectarTiendaNube(con);
			List<Product> productosTN = null;
			try{
				productosTN = Product.findProducts(miTienda);
			}catch (Exception e){
				addError("Configurador: " + con.getCodigo() + " - Hubo un problema al buscar los produtos en TN. "+e.toString());
				continue;
			}
			importados=0;
			yaCreados=0;
			for(Product productoTN: productosTN){
				this.crearPublicacionConVariante(productoTN.getVariants(), productoTN.getName().getEs(), con);
			}
			if(errores == 0){
				addMessage("Configurador: " + con.getCodigo() + " - La operacion se ah llevado a cabo sin errores");
			}else addError("Configurador: " + con.getCodigo() + " - En total se han encontrado " + errores + " errores.\n");
			
			if(importados > 0){
				addMessage("Configurador: " + con.getCodigo() + " - Productos importados: " + importados);
			}
			if(yaCreados > 0){
				addMessage("Configurador: " + con.getCodigo() + " - Cantidad de Productos que ya se encuentran creados: " + yaCreados);
			}
		}
	}   

	private void crearPublicacionConVariante(List<Variant> variantes,String nombreProducto, ConfiguracionMercadoLibre con) {
		for(Variant variante: variantes){
			Producto prod = null;
			if(variante.getSku() != null){
				prod = (Producto) Producto.buscarPorCodigo(variante.getSku(),Producto.class.getSimpleName());
				    
				if(prod != null){
					try{
						PublicacionML publicacionEncontrada  = PublicacionML.buscar(String.valueOf(variante.getId()), Ecommerce.TiendaNube);
						//producto ya sincronizado
						StringBuilder errorYaExiste = new StringBuilder("Configurador: " + con.getCodigo() + " - La publicacion: " + nombreProducto + " ya se encuentra creada "); 
						if(publicacionEncontrada != null){
							if(Is.equal(publicacionEncontrada.getEstado(),EstadoPublicacionML.Eliminada)){
								if(Is.equal(publicacionEncontrada.getProducto().getCodigo(),prod.getCodigo())){
									errorYaExiste.append("- Estado Eliminada, PorFavor, indiquela como Publicada.");
									procesarError(errorYaExiste);	
								}else{
									errorYaExiste.append("- Estado Eliminada y Producto de Cloud diferente, Solucione la diferencia y vuelva a importar.");
									procesarError(errorYaExiste);	
								}
							}
							else if(Is.equal(publicacionEncontrada.getEstado(),EstadoPublicacionML.Publicada)){
								if(!Is.equal(publicacionEncontrada.getIdProducto(),variante.getProduct_id())){
									//No deberia entrar nunca aca.
						 			log.error("La publicacion ya existe, con id de variante de tienda nube: " + variante.getId() + " Pero con Id de producto diferente");
								}
								if(!Is.equal(publicacionEncontrada.getProducto().getCodigo(),prod.getCodigo())){
									errorYaExiste.append("- Estado Publicada y Producto de Cloud diferente, Solucione la diferencia y vuelva a importar.");
									procesarError(errorYaExiste);	
								}
								else{
									yaCreados++;
								}
							}
						}else{
							crearPublicacion(con, variante, prod);
						}
			 			log.warn("Configurador: " + con.getCodigo() + " - La publicacion ya existe id de variante de tienda nube: " + variante.getId());
					}catch(ValidationException e){
						crearPublicacion(con, variante, prod);
			 			log.warn("Configurador: " + con.getCodigo() + " - La publicacion de TN se creo para el id de variante: " + variante.getId());
					}
				}else {
					if(errores < MOSTRARERRORES){
						addError("Configurador: " + con.getCodigo() + " - Problemas con el SKU en Tienda Nube, "
								+ "o no existe el producto en Cloud -> " + nombreProducto);
					}else if(errores == MOSTRARERRORES){
						addError("Configurador: " + con.getCodigo() + " - Aún hay más errores...");
					}
					this.rollback();
					log.error("Configurador: " + con.getCodigo() + " - El producto en CloudERP no se encuentra cargado/encontrado, para el producto de TN: " + nombreProducto );
					errores++;
				}
			}else {  
				if(errores < MOSTRARERRORES){
					addError("Configurador: " + con.getCodigo() + " - El Sku del producto \"" + nombreProducto + "\" en TiendaNube esta vacio");
				}else if(errores == MOSTRARERRORES){
					addError("Configurador: " + con.getCodigo() + " - Aún hay más errores...");
				}
				this.rollback();
				log.error("Configurador: " + con.getCodigo() + " - El sku del producto esta vacio, id de producto TN: "+variante.getProduct_id());
				errores++;
			}
		}			 
	}

	private void procesarError(StringBuilder errorYaExiste) {
		if(errores < MOSTRARERRORES){
			addError(errorYaExiste.toString());
		}else if(errores == MOSTRARERRORES){
			addError("y mas errores...");
		}
	}

	private void crearPublicacion(ConfiguracionMercadoLibre con, Variant v, Producto prod) {
		PublicacionML nuevoProducto = new PublicacionML();
		nuevoProducto.setIdMercadoLibre(String.valueOf(v.getId()));
		nuevoProducto.setProducto(prod);
		nuevoProducto.setTipoEcommerce(Ecommerce.TiendaNube);
		nuevoProducto.setIdProducto(v.getProduct_id());
		nuevoProducto.setConfiguracionEcommerce(con);
		XPersistence.getManager().persist(nuevoProducto);
		this.commit();
		importados++;//se podria usar errores.add(message), pero no lo seria. 
	}


}
