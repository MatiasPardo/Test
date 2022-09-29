package org.openxava.tesoreria.model;

import java.math.*;
import java.util.*;

import javax.persistence.*;

import org.openxava.annotations.*;
import org.openxava.base.model.*;
import org.openxava.compras.model.*;
import org.openxava.conciliacionbancaria.model.ExtractoBancario;
import org.openxava.conciliacionbancaria.model.IObjetoConciliable;
import org.openxava.conciliacionbancaria.model.TipoConciliacionBancaria;
import org.openxava.jpa.*;
import org.openxava.negocio.filter.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.*;
import org.openxava.ventas.model.*;

@Entity

@Views({
	@View(members="Ppal{" +
			"Principal[#fechaComprobante, numeroComprobante, tipoComprobante;" + 
				"empresa, fechaCreacion, anulacion, usuario];" + 
				"tesoreria;" +
			"Detalle[tipoValor, importeOriginal, moneda;" +
				"numero, fechaEmision, fechaVencimiento;" +
				"detalle];" + 
			"Importe[" +
				"moneda1, importeMoneda1;" +
				"moneda2, importeMoneda2];" + 
			"cliente; proveedor; concepto;" + 
			"}" + 
			"Conciliacion{" +
				"conciliado, tipoConciliacion;" + 
				"extractoBancarioConciliado;" +	
			"}"
			),
	@View(name="Simple", 
		members="fechaComprobante, numeroComprobante, tipoComprobante;" +
				"fechaCreacion, anulacion, usuario;" + 
				"tipoValor, importeOriginal")
})

@Tabs({
	@Tab(		
		properties="fechaCreacion, tipoComprobante, fechaComprobante, numeroComprobante, anulacion, tesoreria.nombre, empresa.nombre, tipoValor.nombre, importeOriginal, importeMoneda1, importeMoneda2, numero, fechaEmision, fechaVencimiento, detalle, usuario, cliente.nombre, proveedor.nombre, concepto.nombre",
		filter=SucursalEmpresaFilter.class,		
		baseCondition=SucursalEmpresaFilter.BASECONDITION_EMPRESASUCURSAL,
		defaultOrder="${fechaCreacion} desc")	
})	

public class MovimientoValores implements IGeneradoPor, IObjetoConciliable{

	@Id @Hidden
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(length=32)
	@Hidden
	@Required
	private String idTransaccion;
	
	@Hidden
	@Column(length=100)
	@Required
	private String tipoTrDestino;
	
	@Column(length=32)
	@Hidden
	@ReadOnly
	private String idItem;
	
	@Hidden
	@Column(length=100)
	@ReadOnly
	private String tipoItem;
	
	@ReadOnly
	@Column(length=100)
	private String tipoComprobante = "";
			
	@ReadOnly
	private Date fechaComprobante = new Date();
	
	@Column(length=20)
	@ReadOnly
	private String numeroComprobante = "";
	
	@ReadOnly
	private Boolean anulacion = Boolean.FALSE;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@DescriptionsList(descriptionProperties="nombre")
	private Empresa empresa;
	
	@Stereotype("DATETIME")
	@ReadOnly
	private Date fechaCreacion = new Date();
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@ReferenceView("Simple")
	private Tesoreria tesoreria;
	
	@ReadOnly
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private Sucursal sucursal;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@DescriptionsList(descriptionProperties="nombre")
	private TipoValorConfiguracion tipoValor;
	
	@Stereotype("MONEY")
	@ReadOnly
	private BigDecimal importeOriginal;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@DescriptionsList(descriptionProperties="nombre")
	private Moneda moneda;
	
	@Stereotype("MONEY")
	@ReadOnly
	private BigDecimal importeMoneda1;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@DescriptionsList(descriptionProperties="nombre")
	private Moneda moneda1;
	
	@Stereotype("MONEY")
	@ReadOnly
	private BigDecimal importeMoneda2;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@ReadOnly
	@DescriptionsList(descriptionProperties="nombre")
	private Moneda moneda2;
	
	@Column(length=30)
	@ReadOnly
	private String numero;
	
	@ReadOnly
	private Date fechaEmision = new Date();
	
	@ReadOnly
	private Date fechaVencimiento = new Date();
	
	@ReadOnly
	@Column(length=100)
	private String detalle;
	
	@Column(length=30)
	@ReadOnly
	private String usuario;

	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly
	@ReferenceView("Simple")
	private Cliente cliente;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly
	@ReferenceView("Simple")
	private Proveedor proveedor;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly
	@ReferenceView("Simple")
	private ConceptoTesoreria concepto;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReadOnly	
	private Valor valor;
	
	@SuppressWarnings("unchecked")
	@ReadOnly
	@ListProperties("fecha, concepto, importe, observaciones")
	@CollectionView("Simple") 
	public Collection<ExtractoBancario> getExtractoBancarioConciliado(){
		if (this.getConciliado()){
			if (this.getTipoConciliacion().equals(TipoConciliacionBancaria.Anulacion)){
				return Collections.EMPTY_LIST;
			}
			else{
				if (this.getConciliadoCon() != null){
					Query query = XPersistence.getManager().createQuery("from ExtractoBancario where id = :id");
					query.setParameter("id", this.getConciliadoCon());
					query.setMaxResults(1);
					return query.getResultList();
				}
				else{
					Query query = XPersistence.getManager().createQuery("from ExtractoBancario where conciliadoCon = :id");
					query.setParameter("id", this.getId());
					return query.getResultList();
				}
			}
		}
		else{
			return Collections.EMPTY_LIST;
		}
	}
		
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIdTransaccion() {
		return idTransaccion;
	}

	public void setIdTransaccion(String idTransaccion) {
		this.idTransaccion = idTransaccion;
	}

	public String getTipoTrDestino() {
		return tipoTrDestino;
	}

	public void setTipoTrDestino(String tipoTrDestino) {
		this.tipoTrDestino = tipoTrDestino;
	}

	public String getIdItem() {
		return idItem;
	}

	public void setIdItem(String idItem) {
		this.idItem = idItem;
	}

	public String getTipoItem() {
		return tipoItem;
	}

	public void setTipoItem(String tipoItem) {
		this.tipoItem = tipoItem;
	}

	public Date getFechaComprobante() {
		return fechaComprobante;
	}

	public void setFechaComprobante(Date fechaComprobante) {
		this.fechaComprobante = fechaComprobante;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public Tesoreria getTesoreria() {
		return tesoreria;
	}

	public void setTesoreria(Tesoreria tesoreria) {
		this.tesoreria = tesoreria;
		if (tesoreria != null){
			this.setSucursal(tesoreria.getSucursal());
		}
		else{
			this.setSucursal(null);
		}
	}

	public TipoValorConfiguracion getTipoValor() {
		return tipoValor;
	}

	public void setTipoValor(TipoValorConfiguracion tipoValor) {
		this.tipoValor = tipoValor;
	}

	public BigDecimal getImporteOriginal() {
		return importeOriginal == null ? BigDecimal.ZERO: importeOriginal;
	}

	public void setImporteOriginal(BigDecimal importeOriginal) {
		this.importeOriginal = importeOriginal;
	}

	public BigDecimal getImporteMoneda1() {
		return importeMoneda1 == null ? BigDecimal.ZERO: importeMoneda1;
	}

	public void setImporteMoneda1(BigDecimal importeMoneda1) {
		this.importeMoneda1 = importeMoneda1;
	}

	public BigDecimal getImporteMoneda2() {
		return importeMoneda2 == null ? BigDecimal.ZERO: importeMoneda2;
	}

	public void setImporteMoneda2(BigDecimal importeMoneda2) {
		this.importeMoneda2 = importeMoneda2;
	}

	public String getNumero() {
		return numero;
	}

	public void setNumero(String numero) {
		this.numero = numero;
	}

	public Date getFechaEmision() {
		return fechaEmision;
	}

	public void setFechaEmision(Date fechaEmision) {
		this.fechaEmision = fechaEmision;
	}

	public Date getFechaVencimiento() {
		return fechaVencimiento;
	}

	public void setFechaVencimiento(Date fechaVencimiento) {
		this.fechaVencimiento = fechaVencimiento;
	}

	public String getDetalle() {
		return detalle;
	}

	public void setDetalle(String detalle) {
		int maxLong = 100;
		if (!Is.emptyString(detalle)){
			if (detalle.length() > maxLong){
				this.detalle = detalle.substring(0, maxLong);
			}
			else{
				this.detalle = detalle;
			}
		}
		else{
			this.detalle = detalle;
		}
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		String u = usuario;
		if (usuario != null){
			if (usuario.length() > 30){
				u = usuario.substring(0, 30);
			}
		}
		this.usuario = u;
	}

	public String getTipoComprobante() {
		return tipoComprobante;
	}

	public void setTipoComprobante(String tipoComprobante) {
		this.tipoComprobante = tipoComprobante;
	}

	public Boolean getAnulacion() {
		return anulacion;
	}

	public void setAnulacion(Boolean anulacion) {
		this.anulacion = anulacion;
	}

	public Moneda getMoneda() {
		return moneda;
	}

	public void setMoneda(Moneda moneda) {
		this.moneda = moneda;
	}

	public Moneda getMoneda1() {
		return moneda1;
	}

	public void setMoneda1(Moneda moneda1) {
		this.moneda1 = moneda1;
	}

	public Moneda getMoneda2() {
		return moneda2;
	}

	public void setMoneda2(Moneda moneda2) {
		this.moneda2 = moneda2;
	}

	public String getNumeroComprobante() {
		return numeroComprobante;
	}

	public void setNumeroComprobante(String numeroComprobante) {
		this.numeroComprobante = numeroComprobante;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public Proveedor getProveedor() {
		return proveedor;
	}

	public void setProveedor(Proveedor proveedor) {
		this.proveedor = proveedor;
	}

	public ConceptoTesoreria getConcepto() {
		return concepto;
	}

	public void setConcepto(ConceptoTesoreria concepto) {
		this.concepto = concepto;
	}

	public Valor getValor() {
		return valor;
	}

	public void setValor(Valor valor) {
		this.valor = valor;
	}

	@Override
	public String generadaPorId() {
		return this.getIdTransaccion();
	}

	@Override
	public String generadaPorTipoEntidad() {
		return this.getTipoTrDestino();
	}
	
	public IItemMovimientoValores generadaPorItemMovValores(){
		if (!Is.emptyString(this.getIdItem()) && (!Is.emptyString(this.getTipoItem()))){
			Query query = XPersistence.getManager().createQuery("from " + this.getTipoItem() + " where id = :id");
			query.setParameter("id", this.getIdItem());
			query.setMaxResults(1);
			List<?> result = query.getResultList();
			if (!result.isEmpty()){
				return (IItemMovimientoValores)result.get(0);
			}
			else{
				throw new ValidationException("No se pudo encontrar la referencia " + this.getIdItem() + " de " + this.getTipoItem());
			}
		}
		else{
			return null;
		}
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}
	
	@ReadOnly
	private Boolean conciliado = Boolean.FALSE;
	
	@ReadOnly 
	@Hidden
	private TipoConciliacionBancaria tipoConciliacion = TipoConciliacionBancaria.SinConciliar;
	
	@ReadOnly 
	@Hidden
	private Long conciliadoCon;
	
	public Boolean getConciliado() {
		return conciliado == null ? Boolean.FALSE : conciliado;
	}

	public void setConciliado(Boolean conciliado) {
		if (conciliado != null){
			this.conciliado = conciliado;
		}
	}
	
	public TipoConciliacionBancaria getTipoConciliacion() {
		return tipoConciliacion == null ? TipoConciliacionBancaria.SinConciliar : tipoConciliacion;
	}

	public void setTipoConciliacion(TipoConciliacionBancaria tipoConciliacion) {
		if (tipoConciliacion != null){
			this.tipoConciliacion = tipoConciliacion;
		}
	}

	public Long getConciliadoCon() {
		return conciliadoCon;
	}

	public void setConciliadoCon(Long conciliadoCon) {
		this.conciliadoCon = conciliadoCon;
	}

	@Override
	public String toString(){
		return this.getTipoComprobante() + " " + this.getNumeroComprobante();
	}

	@Override
	public void anularConciliacion() {
		ExtractoBancario.anularConciliacionMovimientoValores(this);		
	}
}
