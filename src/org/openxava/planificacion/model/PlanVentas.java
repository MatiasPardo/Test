package org.openxava.planificacion.model;

import java.math.BigDecimal;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.filter.*;
import org.openxava.base.model.*;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.Producto;

@Entity

@Views({
	@View(members=
	"Principal{" + 
			"numero, fecha, estado;" +
			"periodo;" +
			"observaciones;" + 
			"items;}" + 
	"Auditoria{fechaCreacion, usuario; fechaConfirmacion; subestado, ultimaTransicion}"		
	),
	@View(name="Simple", members="numero, estado;")
})

@Tab(filter=EmpresaFilter.class,
	baseCondition=EmpresaFilter.BASECONDITION,
	properties="fecha, numero, estado, subestado.nombre, fechaCreacion, usuario",
	defaultOrder="${fechaCreacion} desc")

public class PlanVentas extends Transaccion implements IImportadorItemCSV{
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre", order="${nombre} desc")
	@NoCreate @NoModify
	private PeriodoPlanificacion periodo;
	
	@OneToMany(mappedBy="planVentas", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@ListProperties("producto.codigo, producto.nombre, periodoPlanificacion1, periodoPlanificacion2, periodoPlanificacion3, periodoPlanificacion4, periodoPlanificacion5," +
			"periodoPlanificacion6, periodoPlanificacion7, periodoPlanificacion8, periodoPlanificacion9, periodoPlanificacion10, periodoPlanificacion11, periodoPlanificacion12")
	@SaveAction(value="ItemTransaccion.save")
	@NewAction(value="ItemTransaccion.new")
	@HideDetailAction(value="ItemTransaccion.hideDetail")
	@RemoveAction(value="ItemTransaccion.remove")
	@RemoveSelectedAction(value="ItemTransaccion.removeSelected")
	@EditAction("ItemTransaccion.edit")
	private Collection<ItemPlanVentas> items;
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Plan de Ventas";
	}

	public Collection<ItemPlanVentas> getItems() {
		return items;
	}

	public void setItems(Collection<ItemPlanVentas> items) {
		this.items = items;
	}

	public PeriodoPlanificacion getPeriodo() {
		return periodo;
	}

	public void setPeriodo(PeriodoPlanificacion periodo) {
		this.periodo = periodo;
	}
	
	@Transient
	private Map<String, ItemPlanificacion> itemsPorProductoCSV = null; 
	
	@Override
	public void iniciarImportacionCSV() {
		this.itemsPorProductoCSV = new HashMap<String, ItemPlanificacion>();
		for(ItemPlanificacion item: this.getItems()){
			if (!itemsPorProductoCSV.containsKey(item.getProducto().getCodigo())){
				itemsPorProductoCSV.put(item.getProducto().getCodigo(), item);
			}
		}		
	}

	@Override
	public void finalizarImportacionCSV() {
		this.itemsPorProductoCSV.clear();
		this.itemsPorProductoCSV = null;
	}
	
	@Override
	public ItemTransaccion crearItemDesdeCSV(String[] values) {
		Integer cantidadCampos = 3;
		ItemPlanificacion item = null;
		
		if (values.length >= cantidadCampos){
			String codigoProducto = values[0];
			Producto producto = (Producto)ObjetoEstatico.buscarPorCodigo(codigoProducto, Producto.class.getSimpleName());
			if (producto == null){
				throw new ValidationException("No existe el producto de código " + codigoProducto);
			}
			
			if (this.itemsPorProductoCSV.containsKey(codigoProducto)){
				item = this.itemsPorProductoCSV.get(codigoProducto);
			}
			else{
				item = new ItemPlanVentas();
				((ItemPlanVentas)item).setPlanVentas(this);
				item.setProducto(producto);
				this.itemsPorProductoCSV.put(codigoProducto, item);
				this.getItems().add((ItemPlanVentas)item);
			}
			
			try{
				for(Integer i = 1; i<= 12; i++){
					if (values.length >= (2 + i)){
						item.getClass().getMethod("setPeriodoPlanificacion" + i.toString(), BigDecimal.class).invoke(item, ProcesadorCSV.convertirTextoANumero(values[i + 1]));
					}
					else{
						break;
					}
				}
			}
			catch(Exception e){
				throw new ValidationException(e.toString());
			}
			
			
			return item;
		}
		else{
			throw new ValidationException("Faltan campos: deben ser " + cantidadCampos.toString());
		}	
	}
	
	@Override
	public void getTransaccionesGeneradas(Collection<Transaccion> trs){
	}
}
