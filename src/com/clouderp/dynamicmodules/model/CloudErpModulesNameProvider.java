package com.clouderp.dynamicmodules.model;

import java.util.Collection;

import org.openxava.application.meta.MetaApplication;
import org.openxava.jpa.XPersistence;
import org.openxava.util.Is;

import com.openxava.naviox.impl.AllModulesNamesProvider;

// naviox.properties
// allModulesNamesProviderClass=com.clouderp.dynamicmodules.model.CloudErpModulesNameProvider

public class CloudErpModulesNameProvider extends AllModulesNamesProvider{

	public Collection<String> getAllModulesNames(MetaApplication app) {
		return super.getAllModulesNames(app);
	}
	
	@Override
	protected boolean moduleForClass(String className) {
		boolean isModule = super.moduleForClass(className);
		if (isModule){			
			String schema = XPersistence.getDefaultSchema();
			try {
				Class<?> classEntity = Class.forName(className);
				if (classEntity.isAnnotationPresent(DynamicEntity.class)){
					final DynamicEntity dinamicEntity = classEntity.getAnnotation(DynamicEntity.class);
					if (!Is.equalAsStringIgnoreCase(schema, dinamicEntity.schema())){
						isModule = false;
					}
				}
			} 
			catch (ClassNotFoundException e) {
				return isModule;
			}
		}		
		return isModule;		
	}
}
