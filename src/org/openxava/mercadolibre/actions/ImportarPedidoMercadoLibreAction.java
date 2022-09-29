package org.openxava.mercadolibre.actions;

import java.util.*;
import org.Mercadolibre.*;
import org.openxava.actions.*;
import org.openxava.base.model.Estado;
import org.openxava.jpa.XPersistence;
import org.openxava.mercadolibre.model.*;
import org.openxava.validators.ValidationException;

public class ImportarPedidoMercadoLibreAction extends ViewBaseAction implements IPedidosEcommerce, IHideActionAction{

	private HashMap<String, Object> notificacionesError;
	
	private NotificacionML notificacion;
	
	private ConexionMeli conexion;
	
	private ConfiguracionMercadoLibre con;
	
	private int notificacionesProcesada = 0;
	
	@Override
	public void execute() throws Exception {
		SesionMercadoLibre sesion = SesionesMercadoLibre.getSesion();
		try{
			this.buscarInstanciarPedidosEcommerce(sesion);
			addMessage("Se realizo la operacion busqueda de notificaciones, con " + notificacionesProcesada + " notificaciones procesadas corretamente" );
		}catch (ValidationException e){
			if(notificacionesProcesada > 0){
				addMessage("A pesar de los errores se pudieron procesar " + notificacionesProcesada + " notificaciones de forma correta" );
			}
			addError(e.getMessage());
		}
	}
	
	@Override
	public void buscarInstanciarPedidosEcommerce(SesionMercadoLibre sesion) {
		List<ConfiguracionMercadoLibre> configuradores = ConfiguracionMercadoLibre.buscarConfiguradores(Ecommerce.MercadoLibre);	
		if(configuradores.size() == 0){
			throw new ValidationException("No hay configuradores de MercadoLibre activos");
		}
		List<?> results = PedidoML.notificacionesPendientes();		
		addMessage("Notificaciones Pendientes: "+ results.size());
		if (!results.isEmpty()){	
			notificacionesError = new HashMap<String, Object>();			
			for(Object res: results){
				try{
					con = null;
					try{
						con = this.buscarConfigurador(configuradores, String.valueOf(((Object[])res)[1]));
					}catch (ValidationException e){
						addError(e.getMessage());
						continue;
					}
					notificacion = null;
					conexion = null;
					try{
						try{
							notificacion = sesion.bloquearNotificacion((String)((Object[])res)[0]);
							conexion = sesion.conectar(con);
						}
						catch(Exception e){
						}	
						
						if (notificacion != null){
							String idMercadoLibre = notificacion.idResource();
							// si la notificacion ya se procesó con un error, no se vuelve a intentar procesar.
							// ya que de un pedido puede tener varias notificaciones. así es más rápido
							if (!notificacionesError.containsKey(idMercadoLibre)){
								validarPedidoEcommerce(idMercadoLibre);
							}
						}
					}
					catch(ValidationException v){
						addError(v.getMessage());
					}
					catch(Exception e){
						addError(e.toString());
					}
				}finally{
					if(conexion != null){
						conexion.desconectar();
					}
				}
			}			
			
			if (!getErrors().isEmpty()){
				throw new ValidationException(getErrors());
			}
		}
	}
	
	public void validarPedidoEcommerce(String idMercadoLibre) {
		
		PedidoML pedido = PedidoML.existePedido(Long.parseLong(idMercadoLibre));
		if (pedido == null){
			try{
				MLPedido mlPedido = conexion.buscarPedido(idMercadoLibre);
				
				pedido = PedidoML.crearPedidoDesdeMercadoLibre(mlPedido, con);
			}
			catch(ValidationException v){
				notificacionesError.put(idMercadoLibre, null);
				XPersistence.rollback();
				throw new ValidationException("Configurador: " + con.getCodigo() + " - Error en Pedido " + idMercadoLibre + ": " + v.getErrors());
			}
			catch(Exception e){
				notificacionesError.put(idMercadoLibre, null);
				XPersistence.rollback();
				throw new ValidationException("Configurador: " + con.getCodigo() + " - Error en Pedido " + idMercadoLibre + ": " + e.toString());
			}
		}
		notificacion.setProcessed(true);
		
		Estado estadoPedido = pedido.getEstado();
		String pedidoProcesado = pedido.getNumero();
		this.commit();
		notificacionesProcesada = notificacionesProcesada + 1;
		addMessage("Configurador: " + con.getCodigo() + " pedido: " + pedidoProcesado + " creado");
		if (estadoPedido.equals(Estado.Abierta)){
			try{
				// se trata de confirmar para que reserva inventario
				// se tiene que volver a instanciar
				pedido = PedidoML.existePedido(Long.parseLong(idMercadoLibre));
				pedido.confirmarTransaccion();
				this.commit();
				
			}
			catch(Exception e){
				this.rollback();
			}
		}
		
	}
	
	public ConfiguracionMercadoLibre buscarConfigurador(List<ConfiguracionMercadoLibre> configuradores, String appid) {
		ConfiguracionMercadoLibre configurador = null;
		for(ConfiguracionMercadoLibre con: configuradores){
			if(con.getAppId().equals(appid)){
				configurador = XPersistence.getManager().merge(con); 
				break;
			}
		}
		if(configurador == null)
			throw new ValidationException("No hay configurador definido para AppId: " + appid);
		else return configurador;
		
	}

	@Override
	public String getActionToHide() {
		return "PedidoEcommerce.NovedadesML";
	}
}
