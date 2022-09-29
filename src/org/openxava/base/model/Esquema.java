package org.openxava.base.model;

import java.math.*;

import javax.persistence.*;
import javax.validation.constraints.*;

import org.openxava.annotations.*;
import org.openxava.base.validators.*;
import org.openxava.calculators.*;
import org.openxava.contabilidad.model.*;
import org.openxava.impuestos.model.*;
import org.openxava.jpa.*;
import org.openxava.negocio.model.*;
import org.openxava.util.*;
import org.openxava.validators.ValidationException;

import com.clouderp.maps.model.MapCloud;
import com.openxava.naviox.model.*;

@Entity

@View(members="Principal{nombre;" +
			"usuarioARBA, claveARBA;" +
			"sucursalUnica, stockObligatorio;" + 
			"listaPrecioUnica;" +
			"creditoIntercompany;" +
			"usuarioTest;" + 
			"keyApiGoogleMap;" +
			"}" + 		
			"ValoresPorDefecto{" + 
				"tipoDocumento;" +
				"Cliente[limiteCredito; cuentaContableVentas];" + 
				"Proveedor[cuentaContableCompras];" +
				"Producto[cuentaContableVentasProducto; cuentaContableComprasProducto; retGananciasProducto;" +
						"utilizaDespacho, utilizaLote]" + 
			"}" +
			"Etiquetas{" +
				"etiquetasPorFila;" +
			"}" + 
			"Version{" +
			"versionCloud;" +
			"}"	
	)


@EntityValidator(value=EsquemaValidator.class, 
	properties={@PropertyValue(name="sucursalUnica")})

@Tab(properties="nombre, versionCloud")

public class Esquema {
	
	public static Esquema getEsquemaApp(){		
		return Esquemas.getEsquemaApp();
	}
	
	public static String concatenarEsquema(String tabla){
		return Esquema.nombreEsquema() + "." + tabla;		
	}
	
	public static String nombreEsquema(){
		String organizacion = Users.getCurrentUserInfo().getOrganization();		
		if (Is.emptyString(organizacion)){
			organizacion = Esquema.getEsquemaApp().nombre;
			
		}
		return organizacion;	
	}
	
	public static String redireccionarUrl(String url) {
		return "javascript:void(window.open('/CloudERP/" + url + "'))";
	}
	
	@Id
	@Column(length=50)
	private String nombre;

	@DefaultValueCalculator(value=TrueCalculator.class)
	private Boolean sucursalUnica = Boolean.TRUE;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean listaPrecioUnica = Boolean.FALSE;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean utilizaDespacho = Boolean.FALSE;
	
	@DefaultValueCalculator(value=FalseCalculator.class)
	private Boolean utilizaLote = Boolean.FALSE;
	
	@Column(length=15)
	private String usuarioARBA;
	
	@Column(length=25)
	@Hidden
	@Stereotype("PASSWORD")
	private String claveARBA;
	
	@DefaultValueCalculator(value=TrueCalculator.class)
	private Boolean creditoIntercompany = Boolean.TRUE;
	
	@DefaultValueCalculator(value=TrueCalculator.class)
	private Boolean stockObligatorio = Boolean.TRUE;
	
	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getUsuarioARBA() {
		return usuarioARBA;
	}

	public void setUsuarioARBA(String usuarioARBA) {
		this.usuarioARBA = usuarioARBA;
	}

	public String getClaveARBA() {
		return claveARBA;
	}

	public void setClaveARBA(String claveARBA) {
		this.claveARBA = claveARBA;
	}
	
	private TipoDocumento tipoDocumento;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	private CuentaContable cuentaContableVentas;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	private CuentaContable cuentaContableCompras;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	private CuentaContable cuentaContableVentasProducto;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@NoCreate @NoModify
	private CuentaContable cuentaContableComprasProducto;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@ReferenceView("Simple")
	@SearchListCondition("${tipo} = 3")
	@NoCreate @NoModify
	private Impuesto retGananciasProducto;
	
	@Min(value=0)
	private BigDecimal limiteCredito = BigDecimal.ZERO;

	@DescriptionsList(descriptionProperties="name")
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	private User usuarioTest;
	
	private Integer etiquetasPorFila = 0;
	
	@Hidden
	@Column(length=50)
	private String keyApiGoogleMap;
	
	public TipoDocumento getTipoDocumento() {
		return tipoDocumento == null ? TipoDocumento.CUIT : this.tipoDocumento;
	}

	public void setTipoDocumento(TipoDocumento tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public CuentaContable getCuentaContableVentas() {
		return cuentaContableVentas;
	}

	public void setCuentaContableVentas(CuentaContable cuentaContableVentas) {
		this.cuentaContableVentas = cuentaContableVentas;
	}

	public CuentaContable getCuentaContableCompras() {
		return cuentaContableCompras;
	}

	public void setCuentaContableCompras(CuentaContable cuentaContableCompras) {
		this.cuentaContableCompras = cuentaContableCompras;
	}

	public BigDecimal getLimiteCredito() {
		return limiteCredito == null ? BigDecimal.ZERO : this.limiteCredito;
	}

	public void setLimiteCredito(BigDecimal limiteCredito) {
		this.limiteCredito = limiteCredito;
	}

	public User getUsuarioTest() {
		return usuarioTest;
	}

	public void setUsuarioTest(User usuarioTest) {
		this.usuarioTest = usuarioTest;
	}

	public String nombreUsuarioTest(){
		if (usuarioTest != null){
			return usuarioTest.getName();
		}
		else{
			return null;
		}
	}
	
	public Integer getEtiquetasPorFila() {
		return etiquetasPorFila == null ? 0 : this.etiquetasPorFila;
	}

	public void setEtiquetasPorFila(Integer etiquetasPorFila) {
		this.etiquetasPorFila = etiquetasPorFila;
	}

	public Boolean getListaPrecioUnica() {
		return listaPrecioUnica == null ? Boolean.FALSE : this.listaPrecioUnica;
	}

	public void setListaPrecioUnica(Boolean listaPrecioUnica) {
		this.listaPrecioUnica = listaPrecioUnica;
	}

	public CuentaContable getCuentaContableVentasProducto() {
		return cuentaContableVentasProducto;
	}

	public void setCuentaContableVentasProducto(CuentaContable cuentaContableVentasProducto) {
		this.cuentaContableVentasProducto = cuentaContableVentasProducto;
	}

	public CuentaContable getCuentaContableComprasProducto() {
		return cuentaContableComprasProducto;
	}

	public void setCuentaContableComprasProducto(CuentaContable cuentaContableComprasProducto) {
		this.cuentaContableComprasProducto = cuentaContableComprasProducto;
	}

	public Impuesto getRetGananciasProducto() {
		return retGananciasProducto;
	}

	public void setRetGananciasProducto(Impuesto retGananciasProducto) {
		this.retGananciasProducto = retGananciasProducto;
	}

	public Boolean getSucursalUnica() {
		return sucursalUnica == null ? Boolean.TRUE : this.sucursalUnica;
	}

	public void setSucursalUnica(Boolean sucursalUnica) {
		this.sucursalUnica = sucursalUnica;
	}

	private void sincronizarListaPrecios(boolean precioBaseEsCosto) {
		String p = "'f'";
		if (precioBaseEsCosto){
			p = "'t'";
		}
		String sql = "update " + Esquema.concatenarEsquema("ListaPrecio") + " set precioBaseCosto = " + p;
		Query query = XPersistence.getManager().createNativeQuery(sql);
		query.executeUpdate();
	}

	public void grabar() {
		this.sincronizarListaPrecios(this.getListaPrecioUnica());		
	}
		
	public Boolean getUtilizaDespacho() {
		return utilizaDespacho == null ? Boolean.FALSE : this.utilizaDespacho;
	}

	public void setUtilizaDespacho(Boolean utilizaDespacho) {
		this.utilizaDespacho = utilizaDespacho;
	}
	
	public Boolean getUtilizaLote() {
		return utilizaLote == null ? Boolean.FALSE : utilizaLote;
	}

	public void setUtilizaLote(Boolean utilizaLote) {
		this.utilizaLote = utilizaLote;
	}

	public String getKeyApiGoogleMap() {
		return keyApiGoogleMap;
	}

	public void setKeyApiGoogleMap(String keyApiGoogleMap) {
		this.keyApiGoogleMap = keyApiGoogleMap;
	}
	
	public Boolean getCreditoIntercompany() {
		return creditoIntercompany == null ? Boolean.TRUE : creditoIntercompany;
	}

	public void setCreditoIntercompany(Boolean creditoIntercompany) {
		this.creditoIntercompany = creditoIntercompany;
	}

	public Boolean getStockObligatorio() {
		return stockObligatorio == null ? Boolean.TRUE : stockObligatorio;
	}

	public void setStockObligatorio(Boolean stockObligatorio) {
		if (stockObligatorio != null){
			this.stockObligatorio = stockObligatorio;
		}
	}

	public MapCloud crearMapCloud(){
		MapCloud map = new MapCloud();
		map.setKey(this.getKeyApiGoogleMap());
		if (Is.emptyString(map.getKey())){
			throw new ValidationException("No esta habilitada la geolocalización: Falta asignar Key Google Maps");
		}
		return map;
	}

		
	public String getVersionCloud(){
		return "CloudErp OX5.6.1_30.2";
	}
}
