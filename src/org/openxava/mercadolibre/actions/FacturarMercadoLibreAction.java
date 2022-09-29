package org.openxava.mercadolibre.actions;

import java.util.HashMap;
import java.util.Map;

import org.openxava.actions.TabBaseAction;
import org.openxava.afip.model.FacturaElectronicaAfip;
import org.openxava.base.model.*;
import org.openxava.jpa.XPersistence;
import org.openxava.mercadolibre.model.*;
import org.openxava.model.MapFacade;
import org.openxava.tesoreria.model.ReciboCobranza;
import org.openxava.ventas.model.FacturaVentaContado;

public class FacturarMercadoLibreAction extends TabBaseAction{
	
	private Boolean confirma;

	@SuppressWarnings("rawtypes")
	@Override
	public void execute() throws Exception {
		Map [] selectedOnes = getSelectedKeys(); 
		if ((selectedOnes != null) && (selectedOnes.length > 0)){
			Map<String, Object> procesados = new HashMap<String, Object>();
			for(Map key: selectedOnes){
				PedidoML pedido = instanciarPedidoML(key);
				if (pedido != null){
					if (!procesados.containsKey(pedido.getId())){
						try{
							this.ejecutarAccionMercadoLibre(pedido);
							this.commit();
						}
						catch(Exception e){
							addError(e.getMessage());
							this.rollback();
						}
						
						procesados.put(pedido.getId(), null);
					}
				}				
			}
			this.getTab().deselectAll();
			this.addMessage("ejecucion_OK");
		}
		else{
			this.addError("sin_seleccionar_comprobantes");
		}
	}
			
	private PedidoML instanciarPedidoML(Map<?, ?> key){
		PedidoML pedido = null;
		try{
			ObjetoNegocio obj = (ObjetoNegocio)MapFacade.findEntity(this.getTab().getModelName(), key);
			if (this.getTab().getModelName().equals(PedidoML.class.getSimpleName())){
				pedido = (PedidoML)obj;
			}
			else{
				pedido = ((ItemPedidoML)obj).getPedido();
			}
		}
		catch(Exception e){
			this.addError(e.getMessage());			
		}  
		return pedido;
	}
	
	private void ejecutarAccionMercadoLibre(PedidoML pedido) throws Exception {
		ConfiguracionMercadoLibre config = pedido.getConfiguracionEcommerce();
		if (pedido.getFactura() != null && pedido.getFactura().getEstado().equals(Estado.Borrador))
			this.addError("Factura ya generada en borrador - Pedido nro: "+ pedido.getNumero());
		FacturaVentaContado factura = pedido.facturar();
		factura.setEmpresa(config.getCuentaBancaria().getEmpresa());
		this.commit();
		
		if(this.getConfirma() && this.getErrors().isEmpty()){
			factura = (FacturaVentaContado)XPersistence.getManager().find(FacturaVentaContado.class, factura.getId());
			FacturaElectronicaAfip facturadorAfip = new FacturaElectronicaAfip();
			facturadorAfip.SolicitarCAE(factura, FacturaVentaContado.class);
			
			factura = (FacturaVentaContado)XPersistence.getManager().find(FacturaVentaContado.class, factura.getId());
			ReciboCobranza recibo = factura.generarReciboContado();
			recibo.confirmarTransaccion(); 				
			this.commit();
		}
		
	}

	public Boolean getConfirma() {
		return confirma == null?true:confirma;
	}

	public void setConfirma(Boolean confirma) {
		this.confirma = confirma;
	}
}
