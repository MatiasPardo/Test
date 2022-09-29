package org.openxava.mercadolibre.actions;

import java.math.BigDecimal;
import java.util.*;

import org.openxava.actions.ViewBaseAction;
import org.openxava.base.model.*;
import org.openxava.jpa.XPersistence;
import org.openxava.mercadolibre.model.ConfiguracionMercadoLibre;
import org.openxava.mercadolibre.model.Ecommerce;
import org.openxava.mercadolibre.model.ItemPedidoML;
import org.openxava.mercadolibre.model.PedidoML;
import org.openxava.mercadolibre.model.PublicacionML;
import org.openxava.mercadolibre.model.SesionMercadoLibre;
import org.openxava.mercadolibre.model.SesionesMercadoLibre;
import org.openxava.negocio.model.TipoDocumento;
import org.openxava.util.*;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.Producto;

import com.tiendanube.base.*;
import com.tiendanube.model.*;

public class BuscarPedidosTiendaNubeAction extends ViewBaseAction implements IPedidosEcommerce{

	private int pedidosInstanciados = 0;
	
	private TipoCupon tipoCuponOrden = null;
	
	@Override
	public void execute() throws Exception {
		SesionMercadoLibre sesion = SesionesMercadoLibre.getSesion();
		this.buscarInstanciarPedidosEcommerce(sesion);
		if(pedidosInstanciados == 0){
			addWarning("No se creo ningun pedido");
		}else {
			addMessage("Se crearon " + pedidosInstanciados +" pedidos en total" );
		}
	}
	
	@Override
	public void buscarInstanciarPedidosEcommerce(SesionMercadoLibre sesion) {
		List<ConfiguracionMercadoLibre> configuradores = new LinkedList<ConfiguracionMercadoLibre>();
		try{
			configuradores = ConfiguracionMercadoLibre.buscarConfiguradores(Ecommerce.TiendaNube);
		}catch(ValidationException e){
			throw new ValidationException(e.getMessage());
		}
		
		int pedidos = 0;
		for(ConfiguracionMercadoLibre con: configuradores){
			String codigoCon = con.getCodigo();
			TiendaNube miTienda = sesion.conectarTiendaNube(con);
			List<PedidoML> pedidosERP = new LinkedList<PedidoML>();
			List<Order> pedidosPendientes = new LinkedList<Order>();
			Date fechaNuevaBusqueda = new Date();
			List<Order> pedidosTiendaNube = new LinkedList<Order>(); 
			
			try{
				pedidosTiendaNube = Order.findOrdersAfter(miTienda, con.getFechaBusqueda());
			}catch (Exception e){
				throw new ValidationException(e.getMessage());
			}

			for(Order order: pedidosTiendaNube){
				try{
					con = XPersistence.getManager().find(ConfiguracionMercadoLibre.class, con.getId());
				}catch(Exception e){
					this.addError("No fue posible instanciar el configurador");
				}
				PedidoML pedido = PedidoML.existePedido(Long.valueOf(order.getId()));
				if(order.getPayment_status().equals("paid") && pedido == null){
					try{
						pedido = this.crearPedidoDesdeTiendaNube(order, con);	
						Estado estadoPedido = pedido.getEstado();
						pedidosERP.add(pedido);
						this.commit(); 
						if (estadoPedido.equals(Estado.Abierta)){			
							try{ 
								// se trata de confirmar para que reserva inventario
								// se tiene que volver a instanciar		
								pedido = XPersistence.getManager().find(PedidoML.class, pedido.getId());
								if(!order.isTotalDiferente()){
										pedido.confirmarTransaccion();
								}else{
									if(tipoCuponOrden != null && Is.equal(tipoCuponOrden, TipoCupon.absolute)){
										pedido.cancelarTransaccion();;
									}
									addError("Configurador: " + codigoCon + " El pedido numero: " + order.getNumber() + " tiene descuentos que no estan soportados.");
								}
								this.commit();
							}
							catch(Exception e){
								//agregarPedidoPendiente(pedidosPendientes, order, pedido);
								this.rollback();
								addError("Configurador: " + codigoCon +" - Error en pedido numero: "+ order.getNumber() +" - Detalle: " + e.getMessage());
							}
						}
					}catch(Exception e){
						agregarPedidoPendiente(pedidosPendientes, order, pedido);						
						this.rollback();
						getErrors().add("Configurador: " + codigoCon + " - Error en pedido numero: "+ order.getNumber()+ " Detalle: ");
						ErroresERP.agregarErrores(this.getErrors(),e);
					}
				}else if(!order.getPayment_status().equals("paid")){
					pedidosPendientes.add(order);
				} 
			}
			
			try{
				con = XPersistence.getManager().find(ConfiguracionMercadoLibre.class, con.getId());
			}catch(Exception e){
				this.addError("No fue posible instanciar el configurador");
			}

			if(!pedidosPendientes.isEmpty()){
				Date fechaMinima = null; 
				for(Order o: pedidosPendientes){ 
					if(fechaMinima == null){
						fechaMinima = o.getCreated_at();
					}else if(o.getCreated_at().before(fechaMinima)){ 	
						fechaMinima = o.getCreated_at();
						}
				}
				fechaNuevaBusqueda = fechaMinima;
			}else {
				/*
				 * Actualizar la fecha de busqueda con la fecha mas nueva de los pedidos importador
				 * sino hay pedido nuevo no actualizo la fecha.
				 */
				Calendar cal = Calendar.getInstance();
				cal.setTime(fechaNuevaBusqueda);
				cal.add(Calendar.DATE, -1);
				fechaNuevaBusqueda = cal.getTime();
			}
			con.setFechaBusqueda(UtilERP.trucarDateTime(fechaNuevaBusqueda));
			this.commit();
			pedidos += pedidosERP.size();
			if(pedidosERP.size() >= 1){
				addMessage("Para el Condifugrador con codigo: " + con.getCodigo() + " se crearon: " + pedidosERP.size() + " pedido/s.");
			}
			else if(pedidosERP.size() == 0){
				addError("El condigurador con codigo: " + codigoCon + " - No creo ningun pedido. ");
			}			
			if(!Is.empty(pedidosPendientes)){
				StringBuilder datosPedidos = new StringBuilder();
				datosPedidos.append("Numero/s: ");
				for(Order or: pedidosPendientes){
					datosPedidos.append(or.getNumber());
					datosPedidos.append(";");
				}
				addInfo("El condigurador con codigo: " + codigoCon + " - Pedidos pendientes: " + pedidosPendientes.size() +"\n Detalles: "+datosPedidos.toString());
			}
			
		}
		pedidosInstanciados = pedidosInstanciados + pedidos;
	}

	private void agregarPedidoPendiente(List<Order> pedidosPendientes, Order order, PedidoML pedido) {
		if(pedido != null && pedido.esNuevo()){
			pedidosPendientes.add(order);
		}
	}	


	private PedidoML crearPedidoDesdeTiendaNube(Order order,ConfiguracionMercadoLibre conTN){
		String codigoCon = conTN.getCodigo();
		Customer customer = order.getCustomer();
		PedidoML pedido = new PedidoML();
		this.tipoCuponOrden = null;
		if(customer != null){
			pedido.setIdComprador(String.valueOf(customer.getId()));
			pedido.setNombre(traerNombre(customer.getName()));
			pedido.setApellido(traerApellido(customer.getName()));
			pedido.setEmail(customer.getEmail());
			pedido.setNumeroDocumento(customer.getIdentification());
		}
		pedido.setTipoEcommerce(Ecommerce.TiendaNube);
		pedido.setSucursal(conTN.getSucursal());
		pedido.setNumeroInterno(Long.valueOf(order.getId()));
		pedido.setNumero(String.valueOf(order.getNumber()));
		pedido.setFecha(order.getCreated_at());
		pedido.setFechaHora(order.getClosed_at());		
		pedido.setNombreComprador(order.getBilling_name());
		pedido.setTipoDocumento(TipoDocumento.DNI.toString());
		pedido.setDeposito(conTN.getStockMercadoLibre());
		pedido.setFormaPago(order.getGateway());
		pedido.setConfiguracionEcommerce(conTN);
		pedido.setEmpresa(conTN.getCuentaBancaria().getEmpresa());
		BigDecimal costoEnvio = new BigDecimal(order.getShipping_cost_customer());
		if(costoEnvio.compareTo(BigDecimal.ZERO) > 0 && conTN.getCostoEnvio() != null){
			pedido.setCostoEnvio(costoEnvio);
		}
		
		XPersistence.getManager().persist(pedido);
		if(order.getCoupon() != null && !order.getCoupon().isEmpty()){
			for(Coupon cupon: order.getCoupon()){
				this.tipoCuponOrden = cupon.getTipoCupon();
				if(Is.equal(TipoCupon.absolute,cupon.getTipoCupon())){
					getErrors().add("Codigo de Configurador: " + codigoCon + " - El tipo de descuento: " + cupon.getTipoCupon().toString().toUpperCase() + " NO esta soportado");
				}
				
				if(Is.equal(TipoCupon.percentage,cupon.getTipoCupon())){
					if(order.getPorcentageDiscount() != null && new BigDecimal(order.getPorcentageDiscount()).compareTo(BigDecimal.ZERO) > 0){
						pedido.setPorcentajeDescuento(new BigDecimal(order.getPorcentageDiscount()));
						pedido.recalcularTotales();
					}
				}

				if(Is.equal(TipoCupon.shipping, cupon.getTipoCupon())){
					pedido.setCostoEnvio(BigDecimal.ZERO);
				}
				
				if(order.getCoupon().size() > 2){
					getErrors().add("Codigo de Configurador: " + codigoCon + " - Existen mas de 1 cupon de descuento, actualmente no esta soportado");
					break;
				}
			}
		}
		
		List<ItemPedidoML> items = new LinkedList<ItemPedidoML>();
		for(ItemOrder itemTiendaNube: order.getProducts()){
			ItemPedidoML itemPedido = new ItemPedidoML();
			itemPedido.setPedido(pedido);
			PublicacionML publicacion = null;
			try{
				publicacion = PublicacionML.buscar(itemTiendaNube.getVariant_id(), Ecommerce.TiendaNube);
			}catch(ValidationException e){
				if(Is.empty(itemTiendaNube.getSku())){
					throw new ValidationException(" El sku del producto nombre de TN: " + itemTiendaNube.getName() + " esta vacio");
				}
				Producto prod = (Producto) Producto.buscarPorCodigoError(itemTiendaNube.getSku(),Producto.class.getSimpleName());
				publicacion = new PublicacionML();
				publicacion.setProducto(prod);
				publicacion.setIdMercadoLibre(itemTiendaNube.getVariant_id());
				publicacion.setTipoEcommerce(Ecommerce.TiendaNube);
				publicacion.setIdProducto(String.valueOf(itemTiendaNube.getProduct_id()));
				publicacion.setConfiguracionEcommerce(conTN);
				XPersistence.getManager().persist(publicacion);
			}
			itemPedido.setPublicacion(publicacion);
			itemPedido.setCantidad(new BigDecimal(itemTiendaNube.getQuantity()));
			itemPedido.setPrecioUnitario(new BigDecimal(itemTiendaNube.getPrice()));
			itemPedido.setPrecioOriginal(new BigDecimal(itemTiendaNube.getOriginal_price()));
			itemPedido.setPorcentajeDescuento(pedido.getPorcentajeDescuento());
			itemPedido.recalcular();
			items.add(itemPedido);						
			XPersistence.getManager().persist(itemPedido);
		}

		pedido.setItems(items);	
		pedido.abrirTransaccion();		
		

		return pedido;
	} 
	

	private static String traerNombre(String name) {
		String[] nomyape = null;
		if(name != null && name.contains(" ")){
			nomyape = name.split(" ");
		}
		if(nomyape != null && nomyape.length >= 0){
			return nomyape[0];
		}else return name;
	}

	private static String traerApellido(String name) {
		String[] nomyape = null;
		if(name != null && name.contains(" ")){
			nomyape = name.split(" ");
		}
		if(nomyape != null && nomyape.length == 1){
			return nomyape[0];
		}else if(nomyape != null && nomyape.length == 2){
			return nomyape[1];
		}else return name;
	}

}
