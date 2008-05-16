// ============================================================================
//
// Copyright (C) 2006-2007 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprofiler.core.ui.wizard.indicator;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.talend.dataprofiler.core.ui.utils.AbstractIndicatorForm;
import org.talend.dataprofiler.core.ui.wizard.indicator.parameter.AbstractIndicatorParameter;
import org.talend.dataprofiler.core.ui.wizard.indicator.parameter.TimeSlicesParameter;
import org.talend.dataquality.indicators.DateGrain;


/**
 * DOC zqin  class global comment. Detailled comment
 */
public class TimeSlicesForm extends AbstractIndicatorForm {

    private Button btn;
    
    private ArrayList<Button> allBtns = new ArrayList<Button>();
    
    private TimeSlicesParameter parameter;
    /**
     * DOC zqin TimeSlicesForm constructor comment.
     * @param parent
     * @param style
     */
    public TimeSlicesForm(Composite parent, int style) {
        super(parent, style);
        
        setupForm();
    }

    /* (non-Javadoc)
     * @see org.talend.dataprofiler.core.ui.utils.AbstractIndicatorForm#getFormName()
     */
    @Override
    public String getFormName() {
        return AbstractIndicatorForm.TIME_SLICES_FROM;
    }

    /* (non-Javadoc)
     * @see org.talend.dataprofiler.core.ui.utils.AbstractForm#adaptFormToReadOnly()
     */
    @Override
    protected void adaptFormToReadOnly() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.talend.dataprofiler.core.ui.utils.AbstractForm#addFields()
     */
    @Override
    protected void addFields() {
        this.setLayout(new GridLayout());
        
        Group group = new Group(this, SWT.NONE);
        group.setLayout(new GridLayout(4, true));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText("Aggregate date by");  
        
        for (DateGrain oneDate : DateGrain.VALUES) {
            btn = new Button(group, SWT.RADIO);
            btn.setText(oneDate.getLiteral());

            btn.addSelectionListener(new SelectionAdapter() {

                /* (non-Javadoc)
                 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
                 */
                @Override
                public void widgetSelected(SelectionEvent e) {
                    
                    parameter.setDataUnit(btn.getText());
                }
                
            });
            
            allBtns.add(btn);
        }
    }

    /* (non-Javadoc)
     * @see org.talend.dataprofiler.core.ui.utils.AbstractForm#addFieldsListeners()
     */
    @Override
    protected void addFieldsListeners() {

        for (final Button oneBTN : allBtns) {
            
            oneBTN.addSelectionListener(new SelectionAdapter() {

                /* (non-Javadoc)
                 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
                 */
                @Override
                public void widgetSelected(SelectionEvent e) {

                    parameter.setDataUnit(oneBTN.getText());
                }
                
            });
        }
    }

    /* (non-Javadoc)
     * @see org.talend.dataprofiler.core.ui.utils.AbstractForm#addUtilsButtonListeners()
     */
    @Override
    protected void addUtilsButtonListeners() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.talend.dataprofiler.core.ui.utils.AbstractForm#checkFieldsValue()
     */
    @Override
    protected boolean checkFieldsValue() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.talend.dataprofiler.core.ui.utils.AbstractForm#initialize()
     */
    @Override
    protected void initialize() {

        if (parameter == null) {
            
            parameter = new TimeSlicesParameter();
        } else {
            
            for (Button oneBtn : allBtns) {
                if (oneBtn.getText().equals(parameter.getDataUnit())) {
                    
                    oneBtn.setSelection(true);
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.talend.dataprofiler.core.ui.utils.AbstractIndicatorForm#getParameter()
     */
    @Override
    public AbstractIndicatorParameter getParameter() {

        return this.parameter;
    }

    /* (non-Javadoc)
     * @see org.talend.dataprofiler.core.ui.utils.AbstractIndicatorForm#
     * setParameter(org.talend.dataprofiler.core.ui.wizard.indicator.parameter.AbstractIndicatorParameter)
     */
    @Override
    public void setParameter(AbstractIndicatorParameter parameter) {

        this.parameter = (TimeSlicesParameter) parameter;
        
        this.initialize();
    }

}
