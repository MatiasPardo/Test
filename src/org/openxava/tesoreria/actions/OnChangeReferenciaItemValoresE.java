package org.openxava.tesoreria.actions;

public class OnChangeReferenciaItemValoresE extends OnChangeReferenciaItemValores{
	
	@Override
	public void execute() throws Exception {
		super.execute();
		if (this.getNewValue() != null){
			if (getView().getParent() != null){
				int editingRow = getView().getCollectionEditingRow();		
				getView().getParent().setFocus("items." + editingRow + "." + "referencia.id");
			}
		}
		
	}
}
