/**
 * <copyright> </copyright>
 * 
 * $Id$
 */
package org.talend.dataquality.indicators.impl;

import java.math.BigInteger;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.talend.dataquality.indicators.IndicatorsPackage;
import org.talend.dataquality.indicators.RowCountIndicator;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Row Count Indicator</b></em>'. <!--
 * end-user-doc -->
 * <p>
 * </p>
 *
 * @generated
 */
public class RowCountIndicatorImpl extends IndicatorImpl implements RowCountIndicator {

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    protected RowCountIndicatorImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return IndicatorsPackage.Literals.ROW_COUNT_INDICATOR;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.indicators.impl.IndicatorImpl#handle(java.lang.Object)
     * 
     * @generated NOT
     */
    @Override
    public boolean handle(Object value) {
        return super.handle(value);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.indicators.impl.IndicatorImpl#storeSqlResults(java.lang.Object[])
     * 
     * ADDED scorreia 2008-04-30 storeSqlResults(List<Object[]> objects)
     */
    @Override
    public boolean storeSqlResults(List<Object[]> objects) {
        if (!checkResults(objects, 1)) {
            return false;
        }
        BigInteger c = new BigInteger(String.valueOf(objects.get(0)[0]));
        this.setCount(c);
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.dataquality.indicators.impl.IndicatorImpl#toString()
     * 
     * @generated NOT
     */
    @Override
    public String toString() {
        return "Count = " + count;
    }

} // RowCountIndicatorImpl
