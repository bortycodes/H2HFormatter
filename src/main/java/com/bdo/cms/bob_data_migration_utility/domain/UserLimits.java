/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bdo.cms.bob_data_migration_utility.domain;

import java.math.BigDecimal;
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
public class UserLimits {
    
    Input input;
    String od_gcif;
    String od_user_no;
    String unit_id;
    String unit_ccy;
    BigDecimal od_daily_max_amt;
    int od_daily_max_no_trans;
    BigDecimal od_daily_max_amt_upl;
    int od_daily_max_no_trans_upl;
    BigDecimal od_daily_max_auth_amt;
    BigDecimal od_self_auth_amt;
    String od_self_flag;
    String od_approval_flag;
    String od_amt_masking_flag;
    int day_consolidated_txn_no;
    BigDecimal day_consolidated_txn_amt;
    BigDecimal day_consol_max_approval_amt;
    BigDecimal day_max_bulk_txn_approval_amt;
    BigDecimal max_bulk_txn_approval_amt;
    String txn_max_appr_limit_flag;
    BigDecimal txn_max_approval_amt;
    String day_max_bulk_appr_limit_flag;
    String txn_max_bulk_appr_limit_flag;
    
}
