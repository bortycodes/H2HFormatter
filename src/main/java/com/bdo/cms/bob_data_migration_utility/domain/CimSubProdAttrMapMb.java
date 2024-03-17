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
public class CimSubProdAttrMapMb {
    
    private String custId;
    private String subProduct;
    private String product;
    private String attrLevel;
    private String type;
    private String subProductName;
    
}
