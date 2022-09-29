package org.openxava.mercadolibre.actions;

import java.util.*;
import org.Mercadolibre.*;
import org.openxava.actions.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.mercadolibre.model.*;
import org.openxava.util.*;

public class BuscarPedidosMercadoLibreAction extends TabBaseAction implements IPedidosEcommerce{

	private Messages menssages = new Messages();
	private Messages errores = new Messages();

	@SuppressWarnings("unchecked")
	@Override
	public void execute() throws Exception {
		
		SesionMercadoLibre sesion = SesionesMercadoLibre.getSesion();
		this.buscarInstanciarPedidosEcommerce(sesion);
		this.closeDialog();
		
		Iterator<String> iterr = errores.getStrings().iterator();
		Iterator<String> it = menssages.getStrings().iterator();
		
		while(it.hasNext()){
			this.addMessage(it.next());
		}
		
		while(iterr.hasNext()){
			this.addError(iterr.next());
		}
		
	}

	@Override
	public void buscarInstanciarPedidosEcommerce(SesionMercadoLibre sesion) {
		
		Date desde = (Date) this.getView().getValue("desde");
		Date hasta = (Date) this.getView().getValue("hasta");
		List<ConfiguracionMercadoLibre> configuradores = ConfiguracionMercadoLibre.buscarConfiguradores(Ecommerce.MercadoLibre);
		if(configuradores != null && configuradores.size() == 0){
			addError("No hay configuradores creados o que esten activos.");
		}
		for(ConfiguracionMercadoLibre con: configuradores){
		
			ConexionMeli miConexion = sesion.conectar(con);
			List<MLPedido> pedidos = new LinkedList<MLPedido>();
			try{
				try {
					miConexion.buscarPedidos(pedidos, desde, hasta);
				} catch (Exception e) {
					errores.add("Configurador: " + con.getCodigo() + " - Error al buscar pedidos para el configurador: " + con.getCodigo());
					e.printStackTrace();
				}
				
				recorrerPedidoYCrearlo(con.getId(), miConexion, pedidos);

				this.commit();
			}finally{
				if(miConexion != null){
					miConexion.desconectar();
				}
				if(pedidos.size() == 0){
					this.addError("Configurador: " + con.getCodigo() + " - No hay pedidos para el configurador con codigo: " + con.getCodigo());
				}else{
					this.addMessage("Configurador: " + con.getCodigo() + " - Se encontraron " + pedidos.size() + " pedidos para el configurador: " + con.getCodigo() 
								+ " Importados: " + menssages.getStrings().size() );
				}
			}
			
		}
		
	}

	private void recorrerPedidoYCrearlo(String idConfiguracion, ConexionMeli miConexion, List<MLPedido> pedidos) {
		for(MLPedido pedidoML: pedidos){
			Long numeroPedidoABuscar;
			PedidoML pedido = null;
			numeroPedidoABuscar = pedidoML.getNumeroDePedido();
			pedido = PedidoML.existePedido(numeroPedidoABuscar);

			if(pedido == null){
				ConfiguracionMercadoLibre con = XPersistence.getManager().find(ConfiguracionMercadoLibre.class, idConfiguracion);
				Long idMercadoLibre = null;
				Estado estadoPedido = null;
				
				if (pedido == null){
					try{
						buscarDatosCliente(miConexion, pedidoML);
						pedido = PedidoML.crearPedidoDesdeMercadoLibre(pedidoML, con);
						String idPedidoFacturacion = pedido.getNumero();
						if(pedidoML.getPackId() != null){
							idPedidoFacturacion = String.valueOf(pedidoML.getItems().iterator().next().getPackOrder());
						}
						pedido.setResponsableInscripto(miConexion.esResponsableInscripto(idPedidoFacturacion));
						estadoPedido = pedido.getEstado();
						idMercadoLibre = Long.valueOf(pedido.getNumero());
						this.commit();
						menssages.add("Configurador: " + con.getCodigo() + " - Se creo el pedido: " + pedidoML.getNumeroDePedido());
					}
					catch(Exception v){
						XPersistence.rollback();
						errores.add("Configurador: " + con.getCodigo() + " - Error en Pedido " + pedidoML.getNumeroDePedido() + ": " + v.getMessage());
						continue;
					}
				}

				if (estadoPedido != null && estadoPedido.equals(Estado.Abierta)){
					try{
						// se trata de confirmar para que reserva inventario
						pedido = PedidoML.existePedido(idMercadoLibre);
						pedido.confirmarTransaccion();
						this.commit();
						this.addMessage("Configurador: " + con.getCodigo() + " - Se realizo la reserva de stock para el pedido: " + idMercadoLibre);
					}
					catch(Exception e){
						this.rollback();
						this.addError("Configurador: " + con.getCodigo() + " - Error al confirmar pedido: " + idMercadoLibre + " - " + e.getMessage());
						
					}
				}
			}
			
		}
	}

	private void buscarDatosCliente(ConexionMeli miConexion, MLPedido pedidoML) throws Exception {
		MLItemPedido unItem = pedidoML.getItems().stream().findFirst().get();
		Long numeroPedidoABuscar = pedidoML.getNumeroDePedido();
		if(pedidoML.getPackId() != null){
			numeroPedidoABuscar = unItem.getPackOrder();
		}
		if(numeroPedidoABuscar != null){
			MLPedido pedidoCliente = miConexion.buscarPedido(String.valueOf(numeroPedidoABuscar));
			pedidoML.setNombreComprador(pedidoCliente.getNombreComprador());
			pedidoML.setApellidoComprador(pedidoCliente.getApellidoComprador());
			pedidoML.setTipoDocumentoComprador(pedidoCliente.getTipoDocumentoComprador());
			pedidoML.setNumeroDocumentoComprador(pedidoCliente.getNumeroDocumentoComprador());
		}
	}


}
