/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bdo.cms.bob_data_migration_utility.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 *
 * @author a025012567
 */
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CimPaymentParamsMb {
    
    private String paymentProduct;
    private String extCutoffTimeApplicable;
    private String extCutoffTime;
    private String debitArrDays;
    private String debitBasedOnDate;
    private String holdDebitDays;
    private String splittingAllowed;
    private String splittingThresholdAmt;
    private String slaReq;
    private String slaMinutes;
    
}
