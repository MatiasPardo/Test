package org.openxava.contratos.model;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.FlushModeType;
import javax.persistence.ManyToOne;
import javax.persistence.OrderBy;
import javax.persistence.Query;

import org.openxava.annotations.CollectionView;
import org.openxava.annotations.Condition;
import org.openxava.annotations.ListProperties;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.ReferenceView;
import org.openxava.annotations.Tab;
import org.openxava.annotations.View;
import org.openxava.annotations.Views;
import org.openxava.base.filter.EmpresaFilter;
import org.openxava.base.model.Esquema;
import org.openxava.base.model.Estado;
import org.openxava.base.model.EstadoEntidad;
import org.openxava.base.model.Transaccion;
import org.openxava.base.model.UtilERP;
import org.openxava.jpa.XPersistence;
import org.openxava.util.Is;
import org.openxava.util.Messages;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.Cliente;
import org.openxava.ventas.model.FacturaManual;
import org.openxava.ventas.model.FacturaVenta;
import org.openxava.ventas.model.ItemVentaElectronica;
import org.openxava.ventas.model.VentaElectronica;

@Entity

@Views({
	@View(members=
		"Principal[#" + 
				"fecha, numero, fechaCreacion;" +
				"empresa, moneda;" + 
				"estado, subestado, usuario];" +
		"cliente;" + 
		"observaciones;" +
		"novedades{novedades}facturas{facturas};"),
	@View(name="Simple", 
		members="numero, estado"), 
	@View(name="NovedadContrato", 
		members="numero, estado, empresa, moneda;" +  
			"cliente;")
})

@Tab(
	filter=EmpresaFilter.class,
	baseCondition=EmpresaFilter.BASECONDITION,
	properties="fecha, numero, estado, subestado.nombre, cliente.codigo, cliente.nombre, moneda.nombre, usuario",
	defaultOrder="${fechaCreacion} desc")


public class Contrato extends Transaccion{

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@ReferenceView("Simple")
	private Cliente cliente;
	
	@Condition("${contrato.id} = ${this.id}")
	@ListProperties("fecha, subestado.nombre, proximaEmisionFactura, concepto.codigo, concepto.nombre, utilizaListaPrecio, precioFijo, fechaCreacion, usuario, estado")
	@OrderBy("fechaCreacion desc")
	@ReadOnly
	public Collection<NovedadContrato> getNovedades(){
		return null;
	}
	
	@Condition("${novedadContrato.contrato.id} = ${this.id}")
	@ReadOnly
	@ListProperties(value="venta.fecha, venta.numero, venta.estado, venta.total, fechaCreacion, usuario")
	@OrderBy("fechaCreacion desc")
	@CollectionView(value="Factura")
	public Collection<ItemVentaElectronica> getFacturas(){
		return null;
	}
	
	@Override
	public String descripcionTipoTransaccion() {
		return "Contrato";
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
		
	@Override
	protected void validacionesPreGrabarTransaccion(Messages errores){
		super.validacionesPreGrabarTransaccion(errores);
		
		this.validarContratoVigentes(errores);
	}
	
	private void validarContratoVigentes(Messages errores){
		if (this.getCliente() != null && this.getMoneda() != null && this.getEmpresa() != null){
			// Solo puede haber un contrato vigente por cliente, moneda, empresa
			String sql = "from Contrato where cliente.id = :cliente and empresa.id = :empresa and moneda.id = :moneda and estado != :anulado and estado != :cancelado";					
			if (!this.esNuevo()){
				sql += " and id != :id";
			}
			
			Query query = XPersistence.getManager().createQuery(sql);
			query.setParameter("cliente", this.getCliente().getId());
			query.setParameter("moneda", this.getMoneda().getId());
			query.setParameter("empresa", this.getEmpresa().getId());
			query.setParameter("anulado", Estado.Anulada);
			query.setParameter("cancelado", Estado.Cancelada);
			if (!this.esNuevo()){
				query.setParameter("id", this.getId());
			}
			query.setFlushMode(FlushModeType.COMMIT);
			query.setMaxResults(1);
			
			if (!query.getResultList().isEmpty()){
				errores.add("Ya existe un contrato para el cliente " + this.getCliente().getCodigo() + " empresa " + this.getEmpresa().getNombre() + " moneda " + this.getMoneda().getNombre());
			}
		}
	}
	
	public void facturar(List<VentaElectronica> facturas){
		if (this.getEstado().equals(Estado.Confirmada)){
			List<NovedadContrato> novedades = new LinkedList<NovedadContrato>();
			this.novedadesParaFacturarPorCiclo(novedades);
			if (!novedades.isEmpty()){				
				CicloFacturacion cicloFacturacion = null;
				Date fechaEmisionFactura = null;
				VentaElectronica factura = null;
				EstadoEntidad novedadFinalizada = EstadoEntidad.buscarPorCodigo(NovedadContrato.ESTADOFINALIZADO, NovedadContrato.class.getSimpleName());
				for(NovedadContrato novedad: novedades){
					if (!Is.equal(novedad.getCiclo(), cicloFacturacion) || !Is.equal(novedad.getProximaEmisionFactura(), fechaEmisionFactura)) {
						cicloFacturacion = novedad.getCiclo();
						fechaEmisionFactura = novedad.getProximaEmisionFactura();
						factura = this.crearFactura(novedad);
						facturas.add(factura);
					}
					this.crearItemTransaccion(novedad, factura);
					
					novedad.avanzarProximoCicloFacturacion();
					if (novedad.facturacionFinalizada()){
						novedad.setSubestado(novedadFinalizada);
					}
				}				
				for(Transaccion tr: facturas){
					tr.abrirTransaccion();
				}
			}
		}
		else{
			throw new ValidationException(this.toString() + " no esta confirmado");
		}
	}

	private ItemVentaElectronica crearItemTransaccion(NovedadContrato novedad, VentaElectronica factura) {
		ItemVentaElectronica itemVenta = new ItemVentaElectronica();
		itemVenta.setVenta(factura);
		itemVenta.setNovedadContrato(novedad);
		itemVenta.setProducto(novedad.getConcepto());
		itemVenta.setCantidad(novedad.getCantidad());
		itemVenta.setDetalle(novedad.detalleFacturacion(factura.getFecha()));
		// si utiliza lista de precio se deja vacío, para que la factura lo calcule.
		if (!novedad.getUtilizaListaPrecio()){
			if (factura.getMoneda().equals(novedad.getMoneda())){
				itemVenta.setPrecioUnitario(novedad.getPrecioFijo());
			}
			else{
				itemVenta.setPrecioUnitario(factura.convertirImporteEnMonedaTr(novedad.getMoneda(), novedad.getPrecioFijo()));
			}
		}
		itemVenta.setPorcentajeDescuento(novedad.getPorcentajeDescuento());
		factura.getItems().add(itemVenta);
		itemVenta.recalcular();
		
		if (itemVenta.getPrecioUnitario().compareTo(BigDecimal.ZERO) == 0){
			throw new ValidationException(this.toString() + " esta generando una factura con importe cero: " + novedad.toString());
		}
		
		XPersistence.getManager().persist(itemVenta);
		return itemVenta;
	}

	private VentaElectronica crearFactura(NovedadContrato novedad) {
		VentaElectronica factura;
		if (this.getEmpresa().getInscriptoIva()){
			factura = new FacturaVenta();
		}
		else{
			factura = new FacturaManual();
		}
		factura.copiarPropiedades(this);
		factura.setMoneda(this.getMoneda());
		// para que calcule siempre la cotización
		factura.setCotizacion(BigDecimal.ZERO);
		factura.asignarCreadoPor(this);		
		factura.setFecha(novedad.getProximaEmisionFactura());
		factura.setFechaVencimiento(novedad.getProximoVencimientoFactura());
				
		XPersistence.getManager().persist(factura);
		factura.setItems(new LinkedList<ItemVentaElectronica>());
		return factura;
	}

	private void novedadesParaFacturarPorCiclo(List<NovedadContrato> novedades) {
		StringBuilder sql = new StringBuilder();
		sql.append("from NovedadContrato c where c.contrato.id = :contrato");
		sql.append(" and c.subestado.codigo = :vigente and c.estado = :confirmada and c.fecha <= :fechaActual");
		sql.append(" order by c.ciclo.codigo, c.proximaEmisionFactura asc");
		
		Date fechaActual = UtilERP.trucarDateTime(new Date());
		Query query = XPersistence.getManager().createQuery(sql.toString());
		query.setParameter("contrato", this.getId());
		query.setParameter("vigente", NovedadContrato.ESTADOVIGENTE);
		query.setParameter("fechaActual", fechaActual);
		query.setParameter("confirmada", Estado.Confirmada);
				
		List<?> results = query.getResultList();
		for(Object res: results){
			NovedadContrato novedad = (NovedadContrato)res;
			
			CicloFacturacion ciclo = novedad.getCiclo();
			if (ciclo.puedeFacturar(fechaActual, novedad.getProximaEmisionFactura(), !novedad.haFacturado())){
				novedades.add(novedad);
			}						
		}		
	}

	public static void buscarTodosContratosParaFacturar(List<String> ids) {
		EstadoEntidad estadoVigente = EstadoEntidad.buscarPorCodigo(NovedadContrato.ESTADOVIGENTE, NovedadContrato.class.getSimpleName());
		
		StringBuilder sql = new StringBuilder();
		sql.append("select n.contrato_id from ").append(Esquema.concatenarEsquema("Contrato c join "));
		sql.append(Esquema.concatenarEsquema("NovedadContrato n on c.id = n.contrato_id "));
		sql.append("where n.subestado_id = :idEstadoVigente and c.estado = :confirmada ");
		sql.append("group by n.contrato_id");
		
		Query query = XPersistence.getManager().createNativeQuery(sql.toString());
		query.setParameter("idEstadoVigente", estadoVigente.getId());
		query.setParameter("confirmada", Estado.Confirmada.ordinal());
		List<?> results = query.getResultList();
		for(Object res: results){
			ids.add(res.toString());
		}
	}
}
