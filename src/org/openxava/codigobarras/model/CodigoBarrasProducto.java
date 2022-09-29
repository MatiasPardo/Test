package org.openxava.codigobarras.model;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FlushModeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.Transient;
import javax.validation.constraints.Min;

import org.openxava.annotations.DefaultValueCalculator;
import org.openxava.annotations.EntityValidator;
import org.openxava.annotations.EntityValidators;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.PropertyValue;
import org.openxava.annotations.Required;
import org.openxava.annotations.SearchKey;
import org.openxava.annotations.View;
import org.openxava.base.model.ObjetoNegocio;
import org.openxava.base.model.UtilERP;
import org.openxava.calculators.FalseCalculator;
import org.openxava.inventario.model.Lote;
import org.openxava.jpa.XPersistence;
import org.openxava.negocio.validators.PrincipalValidator;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.Producto;

@Entity

@EntityValidators({
	@EntityValidator(
		value=PrincipalValidator.class, 
		properties= {
			@PropertyValue(name="idEntidad", from="id"), 
			@PropertyValue(name="modelo", value="CodigoBarrasProducto"),
			@PropertyValue(name="principal")
		}
	)
})

@View(members="Principal[#nombre, principal;" +
			"productoDesde, productoHasta, productoBusqueda;" + 
			"loteDesde, loteHasta;" +
			"vencimientoDesde, vencimientoHasta, vencimientoMascara;" +  
		"];" +
		"Test[lecturaCodigoBarras]"
		)

public class CodigoBarrasProducto extends ObjetoNegocio{
	
	public enum TipoBusquedaProducto{
		Codigo("Producto", "codigo"), 
		CodigoAnterior("Producto", "codigoAnterior"), 
		CodigoProveedor("Producto", "codigoProveedor");		
		
		TipoBusquedaProducto(String entidad, String atributo){
			this.entidad = entidad;
			this.atributoCodigo = atributo;
		}
		
		private String entidad;
		
		private String atributoCodigo;

		public String getEntidad() {
			return entidad;
		}

		public String getAtributoCodigo() {
			return atributoCodigo;
		}		
	};
	
	private class ComparatorItemLectorCodBarras implements Comparator<IItemControlCodigoBarras>{
		
		@Override
		public int compare(IItemControlCodigoBarras arg0, IItemControlCodigoBarras arg1) {
			// Los que no tienen cantidad, no se deben controlar, se ponen al final
			if (arg0.getCantidad().abs().compareTo(BigDecimal.ZERO) == 0 && arg1.getCantidad().abs().compareTo(BigDecimal.ZERO) == 0){
				return 0;
			}
			else if (arg0.getCantidad().abs().compareTo(BigDecimal.ZERO) == 0){
				return 1;
			}
			else if (arg1.getCantidad().abs().compareTo(BigDecimal.ZERO) == 0){
				return -1;
			}
			
			// Los que ya fueron controlados, van luego
			BigDecimal pendiente0 = arg0.getCantidad().abs().subtract(arg0.getControlado());
			BigDecimal pendiente1 = arg1.getCantidad().abs().subtract(arg1.getControlado());
			if (pendiente0.compareTo(BigDecimal.ZERO) <= 0 && pendiente1.compareTo(BigDecimal.ZERO) <= 0){
				return 0;
			}
			else if (pendiente0.compareTo(BigDecimal.ZERO) <= 0){
				return 1;	
			}
			else if (pendiente1.compareTo(BigDecimal.ZERO) <= 0){
				return -1;
			}
			
			// primero los que están controlados en forma parcial.
			return pendiente0.compareTo(pendiente1);			
		}
		
	}
	
	private class ComparatorItemLectorCodBarrasLote extends ComparatorItemLectorCodBarras{
		private String codigoLote;

		public String getCodigoLote() {
			return codigoLote;
		}

		public void setCodigoLote(String codigoLote) {
			this.codigoLote = codigoLote;
		}
		
		@Override
		public int compare(IItemControlCodigoBarras arg0, IItemControlCodigoBarras arg1) {
			if (!Is.emptyString(codigoLote)){
				if (arg0.getLote() != null && arg1.getLote() != null){
					if (arg0.getLote().getCodigo().equals(this.getCodigoLote())){
						if (!arg1.getLote().getCodigo().equals(this.getCodigoLote())){
							return -1;
						}
						else{
							return super.compare(arg0, arg1);
						}
					}
					else if (arg1.getLote().getCodigo().equals(this.getCodigoLote())){
						return 1;
					}
					else{
						return super.compare(arg0, arg1);
					}
				}				
				else if (arg0.getLote() != null){
					if (arg0.getLote().getCodigo().equals(this.getCodigoLote())){
						return -1;
					}
					else{
						return 1;
					}
				}
				else if (arg1.getLote() != null){
					if (arg1.getLote().getCodigo().equals(this.getCodigoLote())){
						return 1;
					}
					else{
						return -1;
					}
				}
				else{
					return super.compare(arg0, arg1);
				}
			}
			else{
				return super.compare(arg0, arg1);
			}
		}
	}
	
	@Column(length=20, unique=true) @Required
	@SearchKey
    private String nombre;
		
	@Hidden
	@Min(value=0, message="No puede ser negativo")
	@Required
	private Integer productoDesde;
	
	@Hidden
	@Min(value=0, message="No puede ser negativo")
	@Required
	private Integer productoHasta;
	
	@Required
	@Hidden
	private TipoBusquedaProducto productoBusqueda = TipoBusquedaProducto.Codigo;
	
	@Hidden
	@Min(value=0, message="No puede ser negativo")
	private Integer loteDesde;
	
	@Hidden
	@Min(value=0, message="No puede ser negativo")
	private Integer loteHasta;
	
	@Hidden
	@Min(value=0, message="No puede ser negativo")
	private Integer vencimientoDesde;
	
	@Hidden
	@Min(value=0, message="No puede ser negativo")
	private Integer vencimientoHasta;
	
	@Hidden
	@Column(length=10)
	private String vencimientoMascara;
	
	@DefaultValueCalculator(FalseCalculator.class)
	private Boolean principal = false;
	
	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Integer getProductoDesde() {
		return productoDesde;
	}

	public void setProductoDesde(Integer productoDesde) {
		this.productoDesde = productoDesde;
	}

	public Integer getProductoHasta() {
		return productoHasta;
	}

	public void setProductoHasta(Integer productoHasta) {
		this.productoHasta = productoHasta;
	}

	public TipoBusquedaProducto getProductoBusqueda() {
		return productoBusqueda == null ? TipoBusquedaProducto.Codigo : this.productoBusqueda;
	}

	public void setProductoBusqueda(TipoBusquedaProducto productoBusqueda) {
		if (productoBusqueda != null){
			this.productoBusqueda = productoBusqueda;
		}
	}

	public Integer getLoteDesde() {
		return loteDesde;
	}

	public void setLoteDesde(Integer loteDesde) {
		this.loteDesde = loteDesde;
	}

	public Integer getLoteHasta() {
		return loteHasta;
	}

	public void setLoteHasta(Integer loteHasta) {
		this.loteHasta = loteHasta;
	}
	
	public Integer getVencimientoDesde() {
		return vencimientoDesde;
	}

	public void setVencimientoDesde(Integer vencimientoDesde) {
		this.vencimientoDesde = vencimientoDesde;
	}

	public Integer getVencimientoHasta() {
		return vencimientoHasta;
	}

	public void setVencimientoHasta(Integer vencimientoHasta) {
		this.vencimientoHasta = vencimientoHasta;
	}

	public Boolean getPrincipal() {
		return principal;
	}

	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}

	private String removerCaracteresInvalidos(String str){
		if (!Is.emptyString(str)){
			char[] chars = str.trim().toCharArray();
			StringBuilder result = new StringBuilder();
			for(char c: chars){
				if ((int)c < 126){
					result.append(c);
				}				
			}
			if (result.length() > 0){
				return result.toString().trim();
			}
			else{
				return "";
			}
		}
		else{
			return "";
		}
	}
	
	public void escanear(String codigoBarras) {
		this.productoEscaneado = null;
		this.loteEscaneado = "";
		this.vencimientoEscaneado = null;
		
		int len = codigoBarras.length();
		if (this.getProductoDesde() <= len){
			int hasta = this.getProductoHasta();
			if (hasta > len) hasta = len; 
			String codigoProducto = this.removerCaracteresInvalidos(codigoBarras.substring(this.getProductoDesde() - 1, hasta));
			
			
			Query query = XPersistence.getManager().createQuery("from " + this.getProductoBusqueda().getEntidad() + " where UPPER(" + this.getProductoBusqueda().getAtributoCodigo() + ") like :codigo");
			query.setParameter("codigo", codigoProducto.toUpperCase());		
			query.setMaxResults(1);
			query.setFlushMode(FlushModeType.COMMIT);
			try{
				this.productoEscaneado = (Producto)query.getSingleResult();				
			}
			catch(NoResultException e){
				throw new ValidationException("No existe el producto " + codigoProducto);
			}			
		}
		else{
			throw new ValidationException("No se pudo obtener el producto de " + codigoBarras);
		}
		if (this.getLoteDesde() != null){
			try{
				if (this.getLoteDesde() <= len){
					int hasta = this.getLoteHasta();
					if (hasta > len) hasta = len;
					this.loteEscaneado = this.removerCaracteresInvalidos(codigoBarras.substring(this.getLoteDesde() - 1, hasta));
				}
			}
			catch(IndexOutOfBoundsException e){
				
			}
		}
				
		if (this.getVencimientoDesde() != null){
			if (Is.emptyString(this.getVencimientoMascara())){
				throw new ValidationException("Falta asignar mascara en el vencimiento del código de barras");
			}
			try{
				if (this.getVencimientoDesde() <= len){
					int hasta = this.getVencimientoHasta();
					if (hasta > len) hasta = len;
					String vencimiento = this.removerCaracteresInvalidos(codigoBarras.substring(this.getVencimientoDesde() - 1, hasta));
					if (!Is.emptyString(vencimiento)){
						SimpleDateFormat format = new SimpleDateFormat(this.mascaraFecha(this.getVencimientoMascara()));					
						try{
							this.vencimientoEscaneado = format.parse(vencimiento);
						}
						catch(Exception e){
							throw new ValidationException("No se pudo obtener la fecha de vencimiento del código de barras: " + vencimiento + ". Error: " + e.toString());
						}
					}
				}
			}
			catch(IndexOutOfBoundsException e){
			}
		}
	}
	
	private String mascaraFecha(String mascara) {
		String str = mascara.replace('Y', 'y').replace('A', 'y');
		str = str.replace('m', 'M');
		str = str.replace('D', 'd');
		return str;
	}

	@Column(length=100)
	@Transient
	@Hidden
	private String lecturaCodigoBarras;
	
	public String getLecturaCodigoBarras() {
		return lecturaCodigoBarras;
	}

	public void setLecturaCodigoBarras(String lecturaCodigoBarras) {
		this.lecturaCodigoBarras = lecturaCodigoBarras;
	}

	@Transient
	private Producto productoEscaneado;

	@Hidden
	public Producto getProductoEscaneado() {
		return productoEscaneado;
	}

	@Transient
	private String loteEscaneado;
	
	@Hidden
	public String getLoteEscaneado() {
		return loteEscaneado;
	}
	
	@Transient
	private String serieEscaneado;

	@Hidden
	public String getSerieEscaneado() {
		return serieEscaneado;
	}
	
	@Transient
	private Date vencimientoEscaneado;

	@Hidden
	public Date getVencimientoEscaneado() {
		return vencimientoEscaneado;
	}
	
	public String getVencimientoMascara() {
		return vencimientoMascara;
	}

	public void setVencimientoMascara(String vencimientoMascara) {
		this.vencimientoMascara = vencimientoMascara;
	}

	public void controlarItems(IControlCodigoBarra transaccion, String codigoBarras, BigDecimal cantidadControlar, Boolean isCreacion){
		BigDecimal cantidad = cantidadControlar;
		if (cantidadControlar.compareTo(BigDecimal.ZERO) < 0){
			if (!transaccion.permiteCantidadesNegativas()){
				throw new ValidationException("Cantidad no puede ser negativa");
			}
			cantidad = cantidad.negate();
		}
				 
		this.escanear(codigoBarras);
		Producto producto = this.getProductoEscaneado();
		String codigoLote = this.getLoteEscaneado();
		String codigoSerie = this.getSerieEscaneado();
		
		List<IItemControlCodigoBarras> items = new LinkedList<IItemControlCodigoBarras>();
		IItemControlCodigoBarras itemCreado = null;
		
		transaccion.itemsParaControlarPorCodigoBarra(items, producto, cantidadControlar);

		if(isCreacion){
			// alta
			
			itemCreado = buscarItem(items, producto, codigoLote, codigoSerie);
		
			if(itemCreado == null){
				itemCreado = transaccion.crearItemDesdeCodigoBarras(producto, cantidadControlar, codigoLote, codigoSerie, this.vencimientoEscaneado);
			}
			else{
				// Cuando se crea items, si coincide con uno que ya existe se acumula 
				// Si el producto usa serie no se acumula, porque la cantidad solo es de 1.
				itemCreado.setCantidad(itemCreado.getCantidad().add(cantidadControlar));
			}
			
			if(itemCreado == null){
				throw new ValidationException("Hubo un error al crear el item.");
			}
			
		}
		else{ 
			// control
			
			if (!items.isEmpty()){
				if (producto.getLote() && !Is.emptyString(codigoLote)){
					this.controlarProductoLote(producto, cantidad, items, codigoLote);
				}
				else{
					this.controlarProductoSinAtributos(producto, cantidad, items);
				}
			}
			else{
				if (!transaccion.permiteCantidadesNegativas()){
					throw new ValidationException("No hay items: " + this.detalleError(producto, codigoLote, codigoSerie));
				}
				else{
					throw new ValidationException("No hay items para la cantidad " + UtilERP.convertirString(cantidadControlar) + ": " + this.detalleError(producto, codigoLote, codigoSerie));
				}
			}
		}
	}
	
	private IItemControlCodigoBarras buscarItem(List<IItemControlCodigoBarras> items, Producto producto, String codigoLote, String codigoSerie) {
		IItemControlCodigoBarras itemEncontrado = null;
		for(IItemControlCodigoBarras item: items){
			if(producto.getLote()){
				if(item.getLote() != null){
					if(item.getLote().getCodigo().equals(codigoLote)){
						itemEncontrado = item;
						break;
					}
				}
			}
			else{
				itemEncontrado = item;
				break;
			}
		}
		return itemEncontrado;

	}
	
	private void controlarProductoLote(Producto producto, BigDecimal cantidad, List<IItemControlCodigoBarras> items, String codigoLote){
		ComparatorItemLectorCodBarrasLote comparator = new ComparatorItemLectorCodBarrasLote();
		comparator.setCodigoLote(codigoLote);
		items.sort(comparator);
		
		BigDecimal pendienteControlar = cantidad;
		for(IItemControlCodigoBarras item: items){
			BigDecimal pendienteControlarItem = item.getCantidad().abs().subtract(item.getControlado());
			if (pendienteControlarItem.compareTo(BigDecimal.ZERO) > 0){				
				if (item.getLote() != null && !item.getLote().getCodigo().equals(codigoLote)){
					// es un item que tiene otro lote, pero como hay más pendientes para controlar, 
					// se dividirá ese item
					try{
						Class<?> claseItem = UtilERP.tipoEntidad(item);
						
						IItemControlCodigoBarras itemNuevo = (IItemControlCodigoBarras)claseItem.newInstance();
						((ObjetoNegocio)itemNuevo).copiarPropiedades(item);
						itemNuevo.setCantidad(pendienteControlarItem);
						itemNuevo.setLote(null);
						XPersistence.getManager().persist(itemNuevo);
						item.setCantidad(item.getCantidad().subtract(pendienteControlarItem));
						
						item = itemNuevo;
					}
					catch(Exception e){
						throw new ValidationException("no se pudo dividir el item: " + e.toString());
					}
				}
				
				BigDecimal cantidadControlar = pendienteControlar;
				if (cantidadControlar.compareTo(pendienteControlarItem) > 0){
					cantidadControlar = pendienteControlarItem;
				}
				this.asignarControlado(item, producto, cantidadControlar, codigoLote, null);
				pendienteControlar = pendienteControlar.subtract(cantidadControlar);							
			}
			if (pendienteControlar.compareTo(BigDecimal.ZERO) <= 0){
				break;
			}			
		}										
						
		if (pendienteControlar.compareTo(BigDecimal.ZERO) > 0){
			throw new ValidationException("Excede cantidad " + UtilERP.convertirString(pendienteControlar) + " " + producto.getUnidadMedida().getNombre()+": " + this.detalleError(producto, codigoLote, null));
		}
	}
	
	private void controlarProductoSinAtributos(Producto producto, BigDecimal cantidad, List<IItemControlCodigoBarras> items){
		ComparatorItemLectorCodBarras comparator = new ComparatorItemLectorCodBarras();
		items.sort(comparator);
		
		BigDecimal pendienteControlar = cantidad;
		for(IItemControlCodigoBarras item: items){		
			BigDecimal pendienteControlarItem = item.getCantidad().abs().subtract(item.getControlado());
			pendienteControlar = item.convertirUnidadesLeidas(pendienteControlar);
			if (pendienteControlarItem.compareTo(BigDecimal.ZERO) > 0){
				BigDecimal cantidadControlar = pendienteControlar;
				if (cantidadControlar.compareTo(pendienteControlarItem) > 0){
					cantidadControlar = pendienteControlarItem;
				}
				this.asignarControlado(item, producto, cantidadControlar, null, null);
				pendienteControlar = pendienteControlar.subtract(cantidadControlar);							
			}
			if (pendienteControlar.compareTo(BigDecimal.ZERO) <= 0){
				break;
			}
		}										
	
		if (pendienteControlar.compareTo(BigDecimal.ZERO) > 0){
			throw new ValidationException("Excede cantidad " + UtilERP.convertirString(pendienteControlar) + " " + producto.getUnidadMedida().getNombre()+": " + this.detalleError(producto, null, null));
		}
	}
	
	private void asignarControlado(IItemControlCodigoBarras item, Producto producto, BigDecimal cantidadControlar, 
			String codigoLote, String codigoSerie) {
		
		item.setControlado(item.getControlado().add(cantidadControlar));
		if (producto.getLote()){
			if (!Is.emptyString(codigoLote) && item.getLote() == null){
				this.asignarLote(item, producto, codigoLote, this.getVencimientoEscaneado());
			}
		}		
	}

	public void asignarLote(IItemControlCodigoBarras item, Producto producto, String codigoLote, Date fechaVencimiento) {
		Lote lote = Lote.buscarPorCodigo(codigoLote, producto.getCodigo());
		if (lote == null){
			if (item.crearEntidadesPorControl()){
				Date vencimiento = fechaVencimiento;
				if (vencimiento == null) vencimiento = new Date();
			
				lote = Lote.crearLote(codigoLote, producto, vencimiento);
			}
			else{
				throw new ValidationException("Lote no encontrado " + codigoLote + ": " + this.detalleError(producto, null, null));
			}
		}
		item.setLote(lote);
	}
	
	private String detalleError(Producto producto, String lote, String serie){
		String error = "Producto " + producto.getCodigo();
		if (producto.getLote() && !Is.emptyString(lote)) error += " - lote " + lote;
		return error;
	}	
}
