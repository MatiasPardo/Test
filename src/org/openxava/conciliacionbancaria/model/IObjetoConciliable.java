package org.openxava.conciliacionbancaria.model;

public interface IObjetoConciliable {

	public Boolean getConciliado();

	public void anularConciliacion();
	
	public void setConciliado(Boolean conciliado);
	
	public void setConciliadoCon(Long conciliadoCon);
	
	public void setTipoConciliacion(TipoConciliacionBancaria tipoConciliacion);
	
	public TipoConciliacionBancaria getTipoConciliacion();
}
