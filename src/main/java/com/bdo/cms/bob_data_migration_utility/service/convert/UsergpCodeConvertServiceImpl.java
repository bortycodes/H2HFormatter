package com.bdo.cms.bob_data_migration_utility.service.convert;

import static com.bdo.cms.bob_data_migration_utility.constant.Constants.CORP_GROUP_ID_CONVERT;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bdo.cms.bob_data_migration_utility.config.QueriesConfig;
import com.bdo.cms.bob_data_migration_utility.domain.CgConvert;
import com.bdo.cms.bob_data_migration_utility.domain.Input;
import com.bdo.cms.bob_data_migration_utility.exception.ConvertException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UsergpCodeConvertServiceImpl implements UsergpCodeConvertService {    

    @Autowired
    QueriesConfig qcfg;
    
    @Override
    public void convert(List<Input> inputs, Connection cib, Connection cibut) throws ConvertException {
        log.info("[{}] - Updating...", CORP_GROUP_ID_CONVERT);

        if (!inputs.isEmpty()) {
            
            //get codes from input file
            List<String> cdList = inputs.stream()
                    .map(Input::getCorpCd)
                    .collect(Collectors.toList());

            //concat corp codes
            StringBuilder cds = new StringBuilder();
            List<String> corpCodes = new ArrayList<>();
            int corpCodeCount = 0;
            for (int i = 0; i < cdList.size(); i++) {
            	corpCodeCount++;
            	if (corpCodeCount <= 1000) {
            		cds.append("'").append(cdList.get(i)).append("'")
                        .append(corpCodeCount == 1000 || i == cdList.size() - 1 ? "" : ",");
            	}
            	if (corpCodeCount == 1000 || i == cdList.size() - 1) {
            		corpCodeCount = 0;
            		corpCodes.add(cds.toString());
                	cds.setLength(0);
            	}
            }

            List<String> usergroupcodes_bob = new ArrayList<>();
            List<CgConvert> cgConverts = new ArrayList<>();
            for (String corpCode : corpCodes) {
	            //SELECT * FROM CORP_GROUP_ID_CONVERT; put in list
	            String query1 = qcfg.cnvrtCheckSelect + "\n"
	                    + "WHERE CORP_CD IN (" + corpCode + ")";
	            
	            try (                    
	
	                    PreparedStatement ps1 = cibut.prepareStatement(query1,
	                            ResultSet.TYPE_SCROLL_INSENSITIVE,
	                            ResultSet.CONCUR_UPDATABLE);
	                    ResultSet rs1 = ps1.executeQuery();
	            ) {
	                
	                
	                
	                if (rs1.isBeforeFirst()) {
	                    while (rs1.next()) {                                                
	                        usergroupcodes_bob.add(rs1.getString("BOB_CG_ID"));                        
	                    }
	                }
	                
	                rs1.close();
	                ps1.close();
	                
	                String query2 = qcfg.cnvrtFirstSelect + "\n"
	                        + "AND T2.CD IN (" + corpCode + ")";
	
	                PreparedStatement ps2 = cib.prepareStatement(query2,
	                        ResultSet.TYPE_SCROLL_INSENSITIVE,
	                        ResultSet.CONCUR_UPDATABLE);
	
	
	                ResultSet rs2 = ps2.executeQuery();
	
	                if (rs2.isBeforeFirst()) {
	                    while (rs2.next()) {
	
	                        if(!usergroupcodes_bob.contains(rs2.getString("BOB_CG_ID"))){
	                            cgConverts.add(
	                                    CgConvert.builder()
	                                            .bobCgId(rs2.getString("BOB_CG_ID"))
	                                            .cmsCgId(rs2.getString("CMS_CG_ID"))
	                                            .corpCd(rs2.getString("CORP_CD"))
	                                            .build()
	                            );
	                        }
	                    }
	
	                    if (!cgConverts.isEmpty()) {
	                        insert(cibut, cgConverts);
	                    }
	                }
	                rs2.close();
	                ps2.close();
	                           
	
	            } catch (SQLException e) {
	                log.error(e.getMessage(), e);
	                throw new ConvertException(e.getMessage(), e);
	            }
            }
        }
        log.info("[{}] - Update done!", CORP_GROUP_ID_CONVERT);
    }

    private void insert(Connection to, List<CgConvert> cgConverts) throws ConvertException {
        try (PreparedStatement s2 = to.prepareStatement(
                "INSERT INTO " + CORP_GROUP_ID_CONVERT + " (CMS_CG_ID, BOB_CG_ID, CORP_CD) VALUES (?,?,?)"
        )) {
        	
            for (CgConvert c : cgConverts) {
            	String cmsCgId = c.getCmsCgId() + System.nanoTime();            	
                s2.setString(1, cmsCgId.substring(0, Math.min(cmsCgId.length(), 20))); 
                s2.setString(2, c.getBobCgId());
                s2.setString(3, c.getCorpCd());

                s2.addBatch();
            }

            s2.executeBatch();
        } catch (SQLException e) {
            throw new ConvertException(e.getMessage(), e);
        }
    }
}
