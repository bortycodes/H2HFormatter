package com.bdo.cms.bob_data_migration_utility.domain;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UsergpLimit {
    private String odGcif;
    private String odUsergroupCode;
    private String unitId;
    private String unitCcy;    
    private BigDecimal odDailyMaxAmt;
    private String odDailyMaxNoTrans;
    private BigDecimal odDailyMaxAmtUpl;
    private String odDailyMaxNoTransUpl;
    private BigDecimal odDailyMaxAuthAmt;
    private String odSelfFlag;
    private BigDecimal odSeltAuthAmt;
    private String odApprovalFlag;
    private String odProductCode;
    private String odSubprodCode;
    private BigDecimal odTransMaxAmt;
    private BigDecimal odCumMaxAmt;
    private String odAmtMaskingFlag;
    private String dayConsolidatedTxnNo;
    private BigDecimal dayConsolidatedTxnAmt;
    private BigDecimal dayConsolMaxApprovalAmt;
    private BigDecimal dayMaxBulkTxnApprovalAmt;
    private BigDecimal maxBulkTxnApprovalAmt;
    private String txnMaxApprLimitFlag;
    private BigDecimal txnMaxApprovalAmt;
    private String dayMaxBulkApprLimitFlag;
    private String txnMaxBulkApprLimitFlag;
}

