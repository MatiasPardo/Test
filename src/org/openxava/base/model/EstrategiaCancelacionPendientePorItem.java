package org.openxava.base.model;


import java.util.*;

import org.openxava.validators.*;


public class EstrategiaCancelacionPendientePorItem implements IEstrategiaCancelacionPendiente {
	
	private Collection<IItemPendiente> itemsPendientes = new LinkedList<IItemPendiente>();
	
	public Collection<IItemPendiente> getItemsPendientes(){
		return this.itemsPendientes;
	}
	
	@Override
	public void cancelarPendientes(){
		HashMap<String, Pendiente> verificarPendientes = new HashMap<String, Pendiente>();
		HashMap<String, IItemPendiente> itemsNoCumplidos = new HashMap<String, IItemPendiente>();
		
		org.openxava.util.Messages messages = new org.openxava.util.Messages();
		for(IItemPendiente item: this.getItemsPendientes()){
			if (item.cumplido()){
				messages.add("Ya esta cumplido " + item.getItem().toString());
			}
			else{
				try{
					item.cancelarPendiente();
					Pendiente pendiente = item.getPendiente();
					pendiente.setFechaUltimaActualizacion(new Date());
					String clave = pendiente.getId();
					if (!verificarPendientes.containsKey(clave)){
						verificarPendientes.put(clave, pendiente);
					}
					
					String claveItem = item.getItem().getId();
					if (item.cumplido()){
						if (itemsNoCumplidos.containsKey(claveItem)){
							itemsNoCumplidos.remove(claveItem);
						}
					}
					else{
						if (!itemsNoCumplidos.containsKey(claveItem)){
							itemsNoCumplidos.put(claveItem, item);
						}
					}
				}
				catch(ValidationException e){
					messages.add(e.getMessage());
				}
			}
		}
		
		if (messages.isEmpty()){
			// no se verifican los pendientes que tengan algún item no cumpido
			for(IItemPendiente item: itemsNoCumplidos.values()){
				String clave = item.getPendiente().getId();
				if (verificarPendientes.containsKey(clave)){
					verificarPendientes.remove(clave);
				}
			}
			for(Pendiente pendiente: verificarPendientes.values()){
				pendiente.verificarCumplimiento();
			}
		}
		else{
			throw new ValidationException(messages);
		}
	}
	
	@Override
	public void liberarPendientes(){
		for(IItemPendiente item: this.getItemsPendientes()){
			item.liberar();
			Pendiente pendiente = item.getPendiente();
			pendiente.anularCumplimiento();	
		}	
	}
	
	public void pendientesProcesados(Collection<Pendiente> list){
		Map<String, Object> map = new HashMap<String, Object>();
		for(IItemPendiente item: this.getItemsPendientes()){
			Pendiente pendiente = item.getPendiente();
			if (!map.containsKey(pendiente.getId())){
				list.add(pendiente);
				map.put(pendiente.getId(), null);
			}
		}
	}
}
