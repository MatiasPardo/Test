package org.openxava.mercadolibre.model;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.*;

import org.Mercadolibre.ConexionMeli;
import org.openxava.annotations.Action;
import org.openxava.annotations.DefaultValueCalculator;
import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.ListProperties;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.OnChange;
import org.openxava.annotations.ReadOnly;
import org.openxava.annotations.Required;
import org.openxava.annotations.Stereotype;
import org.openxava.annotations.Tab;
import org.openxava.annotations.Tabs;
import org.openxava.annotations.View;
import org.openxava.base.model.*;
import org.openxava.inventario.model.Deposito;
import org.openxava.jpa.XPersistence;
import org.openxava.mercadolibre.actions.EcommerceDefaulCalculator;
import org.openxava.mercadolibre.actions.OnChangeTipoEcommerceAction;
import org.openxava.negocio.model.Sucursal;
import org.openxava.tesoreria.model.CuentaBancaria;
import org.openxava.tesoreria.model.TipoValorConfiguracion;
import org.openxava.util.Is;
import org.openxava.validators.ValidationException;
import org.openxava.ventas.model.*;

import com.tiendanube.base.ApiCredentials;
import com.tiendanube.base.TiendaNube;

@Entity

@View(members="Principal{ecommerce;" +
		"accessToken, storeID;" + 
		"codeToken, fechaBusqueda;" +
		"appId, secretKey;" + 
		"codigo, activo; " +
		"nombre;"+
		"sucursal, stockMercadoLibre, listaPrecio;" + 
		"puntoFacturacion, cuentaBancaria, tipoValor;" +
		"mediosPago};"
		+ "ItemsAdicionales{costoEnvio; montoDescuento;} ")

@Tabs({
	@Tab(name=ObjetoEstatico.TABNAME_INACTIVOS,
		baseCondition=ObjetoEstatico.CONDITION_INACTIVOS)
})

public class ConfiguracionMercadoLibre extends ObjetoEstatico{
	
	public static ConfiguracionMercadoLibre buscarConfigurador(Ecommerce ecommerce){
		try{
			Query query = XPersistence.getManager().createQuery("from ConfiguracionMercadoLibre where ecommerce = :ecommerce and activo = 't'");
			query.setParameter("ecommerce", ecommerce);
			query.setFlushMode(FlushModeType.COMMIT);
			return (ConfiguracionMercadoLibre) query.getSingleResult();
		}
		catch(Exception e){
			throw new ValidationException("No esta definido el configurador o hay más de uno definido: " + ecommerce.toString() + " "+ e.toString());
		}
	}
	
	public static ConfiguracionMercadoLibre buscarConfiguradorMercadoLibre(){
		try{
			Query query = XPersistence.getManager().createQuery("from ConfiguracionMercadoLibre where ecommerce = :ml and and activo = 't'");
			query.setParameter("ml", Ecommerce.MercadoLibre);
			query.setFlushMode(FlushModeType.COMMIT);
			return (ConfiguracionMercadoLibre) query.getSingleResult();
		}
		catch(Exception e){
			throw new ValidationException("No esta definido el configurador de mercado libre o hay más de uno definido: " + e.toString());
		}
	}
	
	public static ConfiguracionMercadoLibre buscarConfiguradorTiendaNube(){
		try{
			Query query = XPersistence.getManager().createQuery("from ConfiguracionMercadoLibre where ecommerce = :tienda and activo = 't'");
			query.setParameter("tienda", Ecommerce.TiendaNube);
			query.setFlushMode(FlushModeType.COMMIT);
			return (ConfiguracionMercadoLibre) query.getSingleResult();
		}
		catch(Exception e){
			throw new ValidationException("No esta definido el configurador de tienda nube o hay más de uno definido: " + e.toString());
		}
	}
	
	public static void validarNotificacion(String appid) {
		try{
			Query query = XPersistence.getManager().createQuery("from ConfiguracionMercadoLibre where appid = :appid and activo = 't' ");
			query.setParameter("appid", appid);
			query.setFlushMode(FlushModeType.COMMIT);
			query.getSingleResult();
		}
		catch(Exception e){
			throw new ValidationException("No esta definido el configurador para el app id: "+appid);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static List<ConfiguracionMercadoLibre> buscarConfiguradores(Ecommerce ecommerce){
		try{
			Query query = XPersistence.getManager().createQuery("from ConfiguracionMercadoLibre where ecommerce =:ecommerce and activo = 't'");
			query.setParameter("ecommerce", ecommerce);
			query.setFlushMode(FlushModeType.COMMIT);
			return (List<ConfiguracionMercadoLibre>) query.getResultList();
		}
		catch(Exception e){
			throw new ValidationException("No esta definido ningun configurador configurador" + e.toString());
		}
	} 
	
	/*public static Ecommerce tipoEcommerceUnico(){
		StringBuilder sql = new StringBuilder();
		sql.append("select ecommerce from ");
		sql.append(Esquema.concatenarEsquema("ConfiguracionMercadoLibre "));
		sql.append("group by ecommerce");
		
		Query query = XPersistence.getManager().createNativeQuery(sql.toString());
		query.setFlushMode(FlushModeType.COMMIT);
		List<?> results = query.getResultList();
		if (results.size() == 1){			
			return Ecommerce.values()[(Integer)results.get(0)];
		}
		else{
			return null;
		}
		
	}*/
	
	@OnChange(OnChangeTipoEcommerceAction.class)
	@DefaultValueCalculator(EcommerceDefaulCalculator.class)
	@Required
	private Ecommerce ecommerce;
	
	@Column(length = 9)
	@ReadOnly
	private String storeID;
	
	@Stereotype("PASSWORD")
	@Column(length = 45)
	@ReadOnly
	private String accessToken;
	
	@Transient
	@Action(value="TiendaNube.Conectar", alwaysEnabled=true)
	private String codeToken;
	
	@Stereotype("DATETIME")
	private Date fechaBusqueda;
	
	@Required
	@Column(length = 20)
	private String appId;
	
	@Required
	@Column(length = 50)
	@Hidden
	@Stereotype("PASSWORD")
	private String secretKey;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	private Sucursal sucursal;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	private Deposito stockMercadoLibre;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@DescriptionsList(descriptionProperties="nombre")
	@NoCreate @NoModify
	private ListaPrecio listaPrecio;

	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private PuntoVenta puntoFacturacion;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private CuentaBancaria cuentaBancaria;
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre")
	private TipoValorConfiguracion tipoValor;
	
	@ElementCollection
	@ListProperties("medioPago, tipoValor.nombre, tesoreria, porcentajeDescuento")
	private Collection<MediosPagoEcommerce> mediosPago;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre", 
						condition="${tipo} = 1")
	private Producto costoEnvio;
	
	@ManyToOne(optional=true, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="nombre", 
						condition="${tipo} = 1")
	private Producto montoDescuento;

	public Producto getCostoEnvio() {
		return costoEnvio;
	}

	public void setCostoEnvio(Producto costoEnvio) {
		this.costoEnvio = costoEnvio;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}

	public Sucursal getSucursal() {
		return sucursal;
	}

	public void setSucursal(Sucursal sucursal) {
		this.sucursal = sucursal;
	}

	public Deposito getStockMercadoLibre() {
		return stockMercadoLibre;
	}

	public void setStockMercadoLibre(Deposito stockMercadoLibre) {
		this.stockMercadoLibre = stockMercadoLibre;
	}

	public ListaPrecio getListaPrecio() {
		return listaPrecio;
	}

	public void setListaPrecio(ListaPrecio listaPrecio) {
		this.listaPrecio = listaPrecio;
	}
	
	public PuntoVenta getPuntoFacturacion() {
		return puntoFacturacion;
	}

	public void setPuntoFacturacion(PuntoVenta puntoFacturacion) {
		this.puntoFacturacion = puntoFacturacion;
	}

	public CuentaBancaria getCuentaBancaria() {
		return cuentaBancaria;
	}

	public void setCuentaBancaria(CuentaBancaria cuentaBancaria) {
		this.cuentaBancaria = cuentaBancaria;
	}

	public TipoValorConfiguracion getTipoValor() {
		return tipoValor;
	}

	public void setTipoValor(TipoValorConfiguracion tipoValor) {
		this.tipoValor = tipoValor;
	}

	public Ecommerce getEcommerce() {
		return ecommerce;
	}

	public void setEcommerce(Ecommerce ecommerce) {
		this.ecommerce = ecommerce;
	}
	
	public String getStoreID() {
		return storeID;
	}

	public void setStoreID(String storeID) {
		this.storeID = storeID;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	
	public String getCodeToken() {
		return codeToken;
	}

	public void setCodeToken(String codeToken) {
		this.codeToken = codeToken;
	}

	public Date getFechaBusqueda() {
		return fechaBusqueda == null ? this.getFechaCreacion() : fechaBusqueda;
	}
	
	public void setFechaBusqueda(Date fechaBusqueda) {
		this.fechaBusqueda = fechaBusqueda;
	}
	
	public void conectarMercadoLibre(ConexionMeli con) {
		try {			
			Long app = Long.decode(this.getAppId());
			con.conectarConML(app, this.getSecretKey(), null, null);					
		}
		catch (Exception e){
			throw new ValidationException("Ocurrió un error al intentar conectarse a Mercado Libre: " + e.toString());
		}
	}
	
	public TiendaNube conectarTiendaNube() {		
		return new TiendaNube(new ApiCredentials(this.getStoreID(), this.getAccessToken()));				 
	}
	
	@Override
	protected void onPrePersist() {
		super.onPrePersist();
		
		this.validarSucursal();		
	}
	
	@Override
	protected void onPreUpdate() {
		super.onPreUpdate();
		
		this.validarSucursal();		
	}
	
	private void validarSucursal(){
		if (this.getSucursal() != null){
			if (this.getPuntoFacturacion() != null){
				if (!this.getPuntoFacturacion().getSucursal().equals(this.getSucursal())){
					throw new ValidationException("Punto de venta no coincide con la sucursal");
				}
			}
			if (this.getStockMercadoLibre() != null){
				if (!this.getStockMercadoLibre().getSucursal().equals(this.getSucursal())){
					throw new ValidationException("Depósito no coincide con la sucursal");
				}
			}
			if (this.getCuentaBancaria() != null){
				if (!this.getCuentaBancaria().getSucursal().equals(this.getSucursal())){
					throw new ValidationException("Cuenta Bancaria no coincide con la sucursal");
				}
			}
		}
	}
		
	@Override
	public void propiedadesSoloLecturaAlEditar(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion) {
		super.propiedadesSoloLecturaAlEditar(propiedadesSoloLectura, propiedadesEditables, configuracion);
		
		propiedadesSoloLectura.add("ecommerce");
		if(this.getEcommerce().equals(Ecommerce.MercadoLibre)){
			propiedadesSoloLectura.add("codeToken");
			propiedadesSoloLectura.add("fechaBusqueda");
		}else if(this.getEcommerce().equals(Ecommerce.TiendaNube)){
			propiedadesEditables.add("codeToken");
			propiedadesEditables.add("fechaBusqueda");		
		} 	
	}
	
	@Override
	public void propiedadesSoloLecturaAlCrear(List<String> propiedadesSoloLectura, List<String> propiedadesEditables, ConfiguracionEntidad configuracion){
		super.propiedadesSoloLecturaAlCrear(propiedadesSoloLectura, propiedadesEditables, configuracion);
		
		propiedadesEditables.add("ecommerce");
		
	}

	public Collection<MediosPagoEcommerce> getMediosPago() {
		return mediosPago;
	}

	public void setMediosPago(Collection<MediosPagoEcommerce> mediosPago) {
		this.mediosPago = mediosPago;
	}

	public MediosPagoEcommerce buscarMedioPago(String formaPago) {
		if (!Is.emptyString(formaPago)){
			if (this.getMediosPago() != null){
				for(MediosPagoEcommerce medio: this.getMediosPago()){
					if (Is.equalAsStringIgnoreCase(medio.getMedioPago(), formaPago)){
						return medio;
					}
				}
			}
		}
		return null;
	}

	public Producto getMontoDescuento() {
		return montoDescuento;
	}

	public void setMontoDescuento(Producto montoDescuento) {
		this.montoDescuento = montoDescuento;
	}
}
