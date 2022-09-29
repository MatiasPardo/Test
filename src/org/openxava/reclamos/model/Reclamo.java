package org.openxava.reclamos.model;

import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.calculators.ObjetoPrincipalCalculator;
import org.openxava.reclamos.filter.*;
import org.openxava.validators.*;


@Entity
@Views({
	@View(members= "Principal[fecha, numero; usuario, fechaCreacion;"
			+ "tipoReclamo, origen, fechaServicio, asignadoA];"
			+ "observaciones, objetoReclamo;"
			+ "cumplimiento;"),
	@View(name="Simple", members="fecha, numero"),
	@View(name="Cumplimiento",
		members="fecha, numero; " +
				"tipoReclamo, origen, fechaServicio, asignadoA;" +
				"observaciones;"
	)

})
@Tabs({
	@Tab(
		name="ReclamosPendientes",
		properties="numero, tipoReclamo.nombre , origen.nombre, fechaServicio, asignadoA.nombre, cumplimiento.numero, estado",
		filter=ReclamoFilter.class,
		baseCondition= ReclamoFilter.RECLAMOBASECONDITION + " and ${cumplimiento} is null and ${estado} = 1"),
	@Tab(name="ImportacionCSV",
		properties="origen.codigo, objetoReclamo.calle, objetoReclamo.altura, objetoReclamo.calle1, objetoReclamo.zona.codigo, objetoReclamo.subzona.codigo, objetoReclamo.codigo, tipoReclamo.codigo, observaciones" 
		),
	@Tab(
		name="ReclamosTodos",
		properties="numero, tipoReclamo.nombre , origen.nombre, fechaServicio, asignadoA.nombre, cumplimiento.numero, estado",
		filter=ReclamoFilter.class,		
		baseCondition=ReclamoFilter.RECLAMOBASECONDITION,
		defaultOrder="${fechaCreacion} desc"
	),
	@Tab(
		properties="numero, tipoReclamo.nombre , origen.nombre, fechaServicio, asignadoA.nombre, cumplimiento.numero, estado",
		filter=ReclamoFilter.class,		
		baseCondition=ReclamoFilter.RECLAMOBASECONDITION  + " and ${cumplimiento} is null ",
		defaultOrder="${fechaCreacion} desc"
		)
})
public class Reclamo extends Transaccion {
	
	public static Reclamo buscarReclamoPorObservaciones(String observaciones){
		Reclamo reclamo = null;
		Query query = XPersistence.getManager().createQuery("from Reclamo where observaciones like :obs and (estado = 0 or estado = 4) ");
		query.setParameter("obs", observaciones);
		query.setMaxResults(1);
		try{
			reclamo = (Reclamo) query.getSingleResult();
		}catch (Exception e){
		}
		return reclamo;				
	}

	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	private OrigenReclamo origen;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	private TipoReclamo tipoReclamo;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=true)
	@DescriptionsList(descriptionProperties="codigo")
	@NoCreate @NoModify
	@DefaultValueCalculator(value=ObjetoPrincipalCalculator.class, 
		properties={@PropertyValue(name="entidad", value="UsuarioReclamo")})
	private UsuarioReclamo asignadoA;
	
	@ManyToOne(fetch=FetchType.LAZY, optional=false)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	private ObjetoReclamo objetoReclamo;
	
	@OneToOne(fetch=FetchType.LAZY, optional=true)
	@ReadOnly
	@NoCreate @NoModify
	@ReferenceView("Reclamo")
	private CumplimientoReclamo cumplimiento;
	
	private Date fechaServicio;

	public CumplimientoReclamo getCumplimiento() {
		return cumplimiento;
	}
	public void setCumplimiento(CumplimientoReclamo cumplimiento) {
		this.cumplimiento = cumplimiento;
	}
	public UsuarioReclamo getAsignadoA() {
		return asignadoA;
	}
	public void setAsignadoA(UsuarioReclamo asignadoA) {
		this.asignadoA = asignadoA;
	}
	public TipoReclamo getTipoReclamo() {
		return tipoReclamo;
	}
	public void setTipoReclamo(TipoReclamo tipoReclamo) {
		this.tipoReclamo = tipoReclamo;
	}
	public OrigenReclamo getOrigen() {
		return origen;
	}
	public void setOrigen(OrigenReclamo origen) {
		this.origen = origen;
	}
	@Override
	public String descripcionTipoTransaccion() {
		// TODO Auto-generated method stub
		return null;
	}
	public Date getFechaServicio() {
		return fechaServicio;
	}
	public void setFechaServicio(Date fechaServicio) {
		this.fechaServicio = fechaServicio;
	}
	public ObjetoReclamo getObjetoReclamo() {
		return objetoReclamo;
	}
	public void setObjetoReclamo(ObjetoReclamo objetoReclamo) {
		this.objetoReclamo = objetoReclamo;
	}
	
	public CumplimientoReclamo buscarCumplimientoPendiente(){
		if (this.confirmada()){
			if (this.getCumplimiento() == null){
				CumplimientoReclamo cumpl = null;
				Query query = XPersistence.getManager().createQuery("from CumplimientoReclamo where reclamo.id = :id");
				query.setParameter("id", this.getId());
				List<?> results = query.getResultList();
				for(Object res: results){
					CumplimientoReclamo tr = (CumplimientoReclamo)res;						
					if (!tr.finalizada()){
						cumpl = tr;
						break;
					}
				}
				return cumpl;
			}
			else{
				throw new ValidationException("Ya esta cumplido");
			}
		}
		else{
			throw new ValidationException("El reclamo no esta confirmado");
		}
	}
	
	public CumplimientoReclamo generarCumplimiento(){
		if (this.getAsignadoA() != null){
			CumplimientoReclamo cumpl = new CumplimientoReclamo();
			cumpl.setReclamo(this);
			XPersistence.getManager().persist(cumpl);		
			
			
			ArrayList<ItemCumplimientoReclamo> items = new ArrayList<ItemCumplimientoReclamo>();
			cumpl.setItems(items);
			for(ItemObjetoReclamo itemObjReclamo: this.getObjetoReclamo().getItems()){
				ItemCumplimientoReclamo itemCumplimiento = new ItemCumplimientoReclamo();
				itemCumplimiento.setCumplimiento(cumpl);
				cumpl.getItems().add(itemCumplimiento);
				itemCumplimiento.setProducto(itemObjReclamo.getProducto());
				itemCumplimiento.setCantidad(itemObjReclamo.getCantidad());
				itemCumplimiento.setUnidadMedida(itemObjReclamo.getUnidadMedida());
				itemCumplimiento.recalcular();
				XPersistence.getManager().persist(itemCumplimiento);
			}			
			return cumpl;			
		}
		else{
			throw new ValidationException("El reclamo no esta asignado");
		}
	}
	
	@Override
	public void agregarParametrosImpresion(Map<String, Object> parameters) {
		super.agregarParametrosImpresion(parameters);
		
		parameters.put("ORIGENRECLAMO", this.getOrigen().getNombre());
		parameters.put("TIPORECLAMO", this.getTipoReclamo().getNombre());
		if (this.getObjetoReclamo() != null){
			parameters.put("OBJETORECLAMO_NOMBRE", this.getObjetoReclamo().getNombre());
			parameters.put("OBJETORECLAMO_CODIGO", this.getObjetoReclamo().getCodigo());
			parameters.put("OBJETORECLAMO_CALLE", this.getObjetoReclamo().getCalle());
			parameters.put("OBJETORECLAMO_CALLE1", this.getObjetoReclamo().getCalle1());
			parameters.put("OBJETORECLAMO_CALLE2", this.getObjetoReclamo().getCalle2());
			parameters.put("OBJETORECLAMO_ALTURA", this.getObjetoReclamo().getAltura());
			parameters.put("OBJETORECLAMO_POTENCIA", this.getObjetoReclamo().getPotencia());
			parameters.put("OBJETORECLAMO_PLANO", this.getObjetoReclamo().getPlano());
			parameters.put("OBJETORECLAMO_ZONA", this.getObjetoReclamo().getZona().getNombre());
			parameters.put("OBJETORECLAMO_SUBZONA", this.getObjetoReclamo().getSubzona().getNombre());
			parameters.put("OBJETORECLAMO_NUMEROPIQUETE", this.getObjetoReclamo().getNumeroPiquete());
			
		}
		if (this.getAsignadoA() != null){
			parameters.put("ASIGNADOA", this.getAsignadoA().getNombre());
		}
		else{
			parameters.put("ASIGNADOA", "");
		}
		parameters.put("FECHASERVICIO", this.getFechaServicio());
	}
}

