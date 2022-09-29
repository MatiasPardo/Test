package org.openxava.base.model;

import javax.persistence.RollbackException;
import javax.validation.ConstraintViolationException;

import org.openxava.util.Is;
import org.openxava.util.Messages;
import org.openxava.util.XavaResources;
import org.openxava.validators.ValidationException;

public class ErroresERP {

	public static void agregarErrores(Messages errores, Exception ex) {		
		if (ex instanceof ValidationException) {
			errores.add(((ValidationException) ex).getErrors());
		} else if (ex instanceof javax.validation.ConstraintViolationException) {
			manageConstraintViolationException(errores,	(javax.validation.ConstraintViolationException) ex);
		} else if (ex instanceof RollbackException) {			
			if (ex.getCause() instanceof javax.validation.ConstraintViolationException) {
				manageConstraintViolationException(errores, (javax.validation.ConstraintViolationException) ex.getCause());
			} else if (ex.getCause() != null
					&& ex.getCause().getCause() instanceof javax.validation.ConstraintViolationException) {
				manageConstraintViolationException(errores, (ConstraintViolationException) ex.getCause()
								.getCause());
			} else {
				manageRegularException(errores, ex);
			}

		} else if (ex instanceof javax.persistence.PersistenceException) {
			if (ex.getCause() instanceof org.hibernate.exception.ConstraintViolationException
					&& ((org.hibernate.exception.ConstraintViolationException) ex
							.getCause()).getConstraintName() != null) {
				manageHibernateConstraintViolationlException(
						errores,
						(org.hibernate.exception.ConstraintViolationException) ex
								.getCause());
			} else {
				manageRegularException(errores, ex);
			}			
		} else if (ex instanceof javax.validation.ValidationException) {
			errores.add(ex.getMessage());
		} else {
			manageRegularException(errores, ex);
		}
	}
	
	
	private static void manageConstraintViolationException(Messages errores, javax.validation.ConstraintViolationException ex) {
		for (javax.validation.ConstraintViolation<?> violation : ex
				.getConstraintViolations()) {
			String attrName = violation.getPropertyPath() == null ? null
					: violation.getPropertyPath().toString();
			String domainClass = violation.getRootBeanClass().getSimpleName();
			String message = violation.getMessage();			
			if (message.startsWith("{") && message.endsWith("}")) {
				message = message.substring(1, message.length() - 1);
			}
			javax.validation.metadata.ConstraintDescriptor<?> descriptor = violation
					.getConstraintDescriptor();
			java.lang.annotation.Annotation annotation = descriptor
					.getAnnotation();
			if (annotation instanceof javax.validation.constraints.AssertTrue || 
				annotation instanceof org.openxava.annotations.EntityValidator) 
			{
				Object bean = violation.getRootBean();
				errores.add(message, bean);
				continue;
			}			
			Object invalidValue = violation.getInvalidValue();
			if (Is.emptyString(attrName) || domainClass == null	|| invalidValue == null) {
				errores.add(message);
			} else {
				errores.add("invalid_state", attrName, domainClass, "'" +
						   XavaResources.getString(message) + "'", invalidValue);
			}
		}		
	}
	
	private static void manageRegularException(Messages errores, Exception ex) {
		errores.add(ex.getLocalizedMessage());
	}
	
	private static void manageHibernateConstraintViolationlException(Messages errores, org.hibernate.exception.ConstraintViolationException ex) {
		String constraintName = ex.getConstraintName().toLowerCase();
		errores.add(constraintName);
	}
}
