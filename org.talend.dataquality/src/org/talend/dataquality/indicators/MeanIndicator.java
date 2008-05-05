package org.talend.dataquality.indicators;

import java.math.BigDecimal;
import org.eclipse.emf.ecore.EObject;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Mean Indicator</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see org.talend.dataquality.indicators.IndicatorsPackage#getMeanIndicator()
 * @model
 * @generated
 */
public interface MeanIndicator extends SumIndicator {
    /**
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Mean</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @model kind="operation"
     * @generated
     */
    BigDecimal getMean();

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * Return the mean by taking into account the null values replaced by the given parameter (usually 0). 
     * <!-- end-model-doc -->
     * @model
     * @generated
     */
    double getMeanWithNulls(double valueForNull);

} // MeanIndicator