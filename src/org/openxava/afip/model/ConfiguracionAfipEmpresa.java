package org.openxava.afip.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

import org.openxava.annotations.DescriptionsList;
import org.openxava.annotations.NoCreate;
import org.openxava.annotations.NoModify;
import org.openxava.annotations.Required;
import org.openxava.base.model.Empresa;

import com.allin.interfacesafip.model.AfipTipoSistemaFCE;

@Embeddable

public class ConfiguracionAfipEmpresa {
	
	public enum TipoCreditoFCE{
		RechazoCliente("S"), AntesEnviarCliente("N");
		
		private String tipoAfip;
		
		TipoCreditoFCE(String tipoCredito){
			this.tipoAfip = tipoCredito;
		}

		public String getTipoAfip() {
			return tipoAfip;
		}
	}
	
	@ManyToOne(optional=false, fetch=FetchType.LAZY)
	@NoCreate @NoModify
	@DescriptionsList(descriptionProperties="codigo")
	@Required
	private Empresa empresa;
	
	@Column(length=25)
	@Required
	private String cbu;
	
	@Column(length=50)
	@Required
	private String alias;

	@Required
	private AfipTipoSistemaFCE tipoSistemaFCE = AfipTipoSistemaFCE.SistemaCirculacionAbierta;
		
	private TipoCreditoFCE creditoFCE = TipoCreditoFCE.RechazoCliente;
	
	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public String getCbu() {
		return cbu;
	}

	public void setCbu(String cbu) {
		this.cbu = cbu;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	public AfipTipoSistemaFCE getTipoSistemaFCE() {
		return tipoSistemaFCE == null ? AfipTipoSistemaFCE.SistemaCirculacionAbierta : this.tipoSistemaFCE;
	}

	public void setTipoSistemaFCE(AfipTipoSistemaFCE tipoSistemaFCE) {
		if (tipoSistemaFCE != null){
			this.tipoSistemaFCE = tipoSistemaFCE;
		}
	}

	public TipoCreditoFCE getCreditoFCE() {
		return creditoFCE == null ? TipoCreditoFCE.RechazoCliente : this.creditoFCE;
	}

	public void setCreditoFCE(TipoCreditoFCE creditoFCE) {
		this.creditoFCE = creditoFCE;
	}
}
