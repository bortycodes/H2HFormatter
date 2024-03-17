package com.bdo.cms.bob_data_migration_utility.service.file;

import com.bdo.cms.bob_data_migration_utility.config.FieldsAndTablesConfig;
import com.bdo.cms.bob_data_migration_utility.config.FileConfig;
import com.bdo.cms.bob_data_migration_utility.config.QueriesConfig;
import com.bdo.cms.bob_data_migration_utility.domain.Input;
import com.bdo.cms.bob_data_migration_utility.exception.FileServiceException;
import static com.bdo.cms.bob_data_migration_utility.constant.Constants.*;
import com.bdo.cms.bob_data_migration_utility.exception.MigrateException;
import com.bdo.cms.bob_data_migration_utility.service.migrate.MigrateServiceImpl;

import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;


import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.StringUtils.join;

@Slf4j
@Service("fileService")
public class FileServiceImpl implements FileService {

    @Autowired
    FileConfig cfg;
    
    @Autowired
    FieldsAndTablesConfig ftcfg;
    
    @Autowired
    QueriesConfig qcfg;
    
    public static Map<String, Integer> corpcifscount = new HashMap<String, Integer>();

    @Override
    public List<Input> read() throws FileServiceException {
        List<Input> inputs = new ArrayList<>();
        
        File inputFile = new File(cfg.inputPath + cfg.inputFilename);
        if(!inputFile.exists())
            throw new FileServiceException("Input file not found.", new FileNotFoundException());
        
        File org_mapping_file = new File(cfg.inputPath + "org_id_mapping.csv");        
        if(!org_mapping_file.exists())
            throw new FileServiceException("Org Mapping file not found.", new FileNotFoundException());
        
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String input;
            while ((input = br.readLine()) != null) {
                String[] s = input.split(cfg.inputDelimiter);
                if(s.length > 1) {
                    String cif = String.format("%10s", s[0].trim()).replace(' ', '0');
                    String corp_cd = s[1].trim().toUpperCase();
                    inputs.add(
                            Input.builder()
                            .cif(cif)
                            .corpCd(corp_cd)
                            .override((s.length == 3 ? (s[2].trim().equalsIgnoreCase("Y") ? "Y" : "N") : "N"))
                            .build()
                    );
                    corpcifscount.put(corp_cd, corpcifscount.getOrDefault(corp_cd, 0) + 1);
                }
            }
        } catch (IOException e) {
            log.info("Input file not found!");
            throw new FileServiceException(e.getMessage(), e);
        }

        return inputs;
    }

    @Override
    public void write(List<String> records, String tableName) throws FileServiceException {
        try {
            File file = new File(cfg.outputPath + tableName + ".csv");            
            records.add(0,getHeaders(tableName));
            FileUtils.writeLines(file, records);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new FileServiceException(e.getMessage(), e);
        }
    }

    @Override
    public void archive() throws FileServiceException {
        String archivedFileName = join(
                cfg.archivePath,
                "BOBDMU_",
                FMT_YYMMDDHHMMSS.format(new Date()),
                ".zip"
        );

        File outputPath = new File(cfg.outputPath);
        List<String> filePaths = new ArrayList<>();

        if (outputPath.list() != null) {
            for (String outputFile : outputPath.list()) {
                filePaths.add(cfg.outputPath + outputFile);
            }
        }
        filePaths.add(cfg.inputPath + cfg.inputFilename);
        filePaths.add(cfg.inputPath + "org_id_mapping.csv");

        String password = getRandomPassword();

        try {
            ZipFile zipFile = new ZipFile(archivedFileName, password.toCharArray());
            if(!zipFile.getFile().getParentFile().exists())
                zipFile.getFile().getParentFile().mkdirs();
            
            List<File> files = new ArrayList<>();
            for (String filePath : filePaths) {
                files.add(new File(filePath));
            }

            ZipParameters params = new ZipParameters();
            params.setCompressionLevel(CompressionLevel.HIGHER);
            params.setEncryptFiles(true);
            params.setEncryptionMethod(EncryptionMethod.AES);
            params.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
            zipFile.addFiles(files, params);

            File zipPasswordFile = new File(FilenameUtils.removeExtension(archivedFileName) + "_password.txt");
            FileUtils.write(zipPasswordFile, password);

        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new FileServiceException(e.getMessage(), e);
        }

        for (String filePath : filePaths) {
            try {
                
                if(!filePath.contains("org_id_mapping.csv"))
                    Files.deleteIfExists(new File(filePath).toPath());
                
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new FileServiceException(e.getMessage(), e);
            }
        }

        log.info("Output files have been archived. {}", archivedFileName);
    }

    @Override
    public Map<Integer, String> fieldMap(List<String> fields) {
        Map<Integer, String> map = new HashMap<>();
        for (int i = 1; i <= fields.size(); i++) {
            map.put(i, fields.get(i-1));
        }
        return map;
    }

    @Override
    public void createReport(Map<String, Integer> numberOfRecordsPerTable,
                             int newRecords,
                             int duplicates,
                             String process) throws FileServiceException {

        String reportFilename = process.equals("generate") ? cfg.extractReportFilename : cfg.migrateReportFilename;

        File file = new File(cfg.reportPath + reportFilename);
        
        if(file.exists()) {
            Date lastModified = new Date(file.lastModified());
            File backup = new File(cfg.reportPath + FilenameUtils.removeExtension(reportFilename) + "_" + new SimpleDateFormat("yyMMddHHmmss").format(lastModified) + "." + FilenameUtils.getExtension(reportFilename));
            boolean fileRenamed = file.renameTo(backup);
            if(!fileRenamed)
                log.error("Unable to rename report file.");
        }
        
        List<String> lines = new ArrayList<>();
        try {
            if(process.equals("generate")) {            
                for (String tableName : numberOfRecordsPerTable.keySet()) {

                    String tbl_no = MigrateServiceImpl.getTableMap("temp").get(tableName);
                    String tbl_details = "";
                    tbl_details += StringUtils.leftPad(tbl_no, 2, "0") + "\t";
                    tbl_details += StringUtils.rightPad(tableName, 35, " ");
                    tbl_details += StringUtils.rightPad(Integer.toString(numberOfRecordsPerTable.get(tableName)), 20, " ") + "\t";
                    lines.add(tbl_details);
                    
                }
            }
        
            String header = "==========BOBDMU SUMMARY REPORT - "
                    + (process.equals("generate") ? "EXTRACT" : "MIGRATE")
                    + "==========\n\n";

            String recordsSummary = (process.equals("generate") ?
                    "Number of new inputs processed: " : "Number of processed inputs migrated: ")
                            + newRecords + "\n";

            String dups = "Number of inputs already processed/duplicates: " + duplicates + "\n\n";

            String endPartition = "\n\n=====================================================";

            String upper = process.equals("generate") ?
                    header + recordsSummary + dups : header + recordsSummary;

            FileUtils.write(file, upper + "\n", true);
            if(process.equals("generate")) {
                Collections.sort(lines);
                FileUtils.writeLines(file, lines, true);
            }
            else if(process.equals("migrate")) {
                Collections.sort(MigrateServiceImpl.tableRecordCountList);
                FileUtils.writeLines(file, MigrateServiceImpl.tableRecordCountList, true);
            }
            FileUtils.write(file, "\nReport produced: " + new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(new Date()) + endPartition + "\n\n", true);
        } catch (IOException | MigrateException e) {
            log.error(e.getMessage(), e);
            throw new FileServiceException(e.getMessage(), e);
        }
        log.info("Summary Report generated.");
    }

    private String getRandomPassword() {
        Random random = new Random();
        StringBuilder pw = new StringBuilder();
        String letters = "abcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) { // letters
                int index = random.nextInt(26);
                String letter = "" + letters.charAt(index);
                int c = random.nextInt(2);
                if (c == 0) pw.append(letter.toLowerCase());
                if (c == 1) pw.append(letter.toUpperCase());
            } else {
                pw.append(random.nextInt(10));
            }
        }
        return pw.toString();
    }

    private String getHeaders(String tableName) throws FileServiceException {
        
        List<String> fields = new ArrayList<>();
        switch(tableName) {
            case CIM_CUST_DEFN_MB:
                fields = ftcfg.cimCustDefnMbFields;
                break;
            case CIM_CUST_CONTACT_INFO_MB:
                fields = ftcfg.cimCustContactInfoMbFields;
                break;
            case CIM_CUST_ACCT_MB:
                fields = ftcfg.cimCustAcctMbFields;
                break;
            case CIM_CUST_BILLING_DETAILS_MB:
                String[] fieldsArray = qcfg.gnrtSelectCimCustBillingDetailsMb.split("\\|");
                fields = getFields(fieldsArray);
                break;
            case CIM_PAYMENT_BKFT_DT_MB:
                fields = ftcfg.paymntProductsCimPaymentBkftDtMbFields;
                break;
            case CIM_PAYMENT_CUST_PREF_MB:
                fieldsArray = qcfg.gnrtSelectCimPaymentCustPrefMb.split("\\|");
                fields = getFields(fieldsArray);
                break;
            case CIM_PAYMENT_PARAMS_MB:                
                fields = ftcfg.paymntProductsCimPaymentParamsMbFields;
                break;
            case CIM_BUSINESS_PARAMS_MB:                
                fields = ftcfg.paymntProductsCimBusinessParamsMbFields;
                break;
            case CIM_DOMAIN_DEFN:
                fields = ftcfg.cimDomainDefnFields;
                break;
            case CIM_CUSTOMER_LIMIT_MB:
                fields = ftcfg.cimCustomerLimitMbFields;
                break;
            case OD_CORPORATE_LIMITS_MB:                
                fields = ftcfg.odCorporateLimitsMbFields;
                break;
            case OD_CORPORATE_FUNCTION_MB:
                fields = ftcfg.odCorporateFunctionMbFields;
                break;
            case OD_USERGROUP_MB:
                fields = ftcfg.odUsergroupMbFields;
                break;
            case OD_USERGP_FUNCTION_MB:
                fields = ftcfg.odUsergpFunctionMbFields;
                break;
            case OD_USERGP_LIMITS_MB:
                fields = ftcfg.odUsergpLimitsMbFields;
                break;
            case OD_USERS_MB:
                fields = ftcfg.odUsersMbFields;
                break;
            case OD_USER_LIMITS_MB:                
                fields = ftcfg.odUserLimitsMbFields;
                break;
            case ORBIIBS_NICKNAME:
                fields = ftcfg.orbiibsNicknameFields;
                break;
            case OD_USER_FUNCTION_MB:
                fields = ftcfg.odUserFunctionMbFields;
                break;
            case OD_ROLES_MB:
                fields = ftcfg.odRolesMbFields;
                break;
            case OD_USER_ROLES_MAP_MB:
                fields = ftcfg.odUserRolesMapMbFields;
                break;
            case OD_RULES_MB:
                fields = ftcfg.odRulesMbFields;
                break;
            case OD_RULES_ACC_MAP_MB:
                fields = ftcfg.odRulesAccMapMbFields;
                break;
            case OD_RULES_DEF_MB:
                fields = ftcfg.odRulesDefMbFields;
                break;
            case OD_PARSED_RULE_MB:
                fields = ftcfg.odParsedRuleMbFields;
                break;
            case BENEFICIARY_MAINTENANCE:
                fields = ftcfg.beneficiaryMaintenanceFields;
                break;
            case CIM_SUBPROD_ATTR_MAP_MB:
                fields = ftcfg.cimSubProdAttrMapMbFields;
                break;
        }
        
        String header = "";
        for(String field : fields)
        {
            header += field + "|";
        }
        
        header = header.substring(0, header.lastIndexOf("|"));        
        return header;
            
        
    }
    
    public static List<String> getFields(String[] fields)
    {
        List<String> fieldList = new ArrayList<>();
        for(String field : fields)
        {
            if(field.contains(":"))
                field = field.split(":")[0];
            fieldList.add(field);
        }
        return fieldList;
    }
    
//   added by dave 12182023
    public void createErrReport(String strCIF, 
    		String strCorpCD, 
    		String strTableName,
    		String strColValue,
    		String strCol,
    		String errMsg, 
    		String process) {


    		Date latestDate = new Date();
    		String reportFilename = cfg.errorReport;
    		reportFilename =  FilenameUtils.removeExtension(reportFilename)+"_"+new SimpleDateFormat("MMddyy").format(latestDate)+"."+FilenameUtils.getExtension(reportFilename);
			File file = new File(cfg.reportPath + reportFilename);
			String strnxtLine = "\n";
			String strPKMsg = " Error Encountered ";
			if(errMsg.contains("unique constraint")) {
				strPKMsg = " PK Validation Error Encountered ";
			}
			
			
			try {
			
					if(file.exists()) {
						FileWriter myWriter = new FileWriter(cfg.reportPath + reportFilename, true);
						myWriter.write(strPKMsg +strnxtLine);
						myWriter.write("Error Msg= " + errMsg + strnxtLine);
						
						Date DateandTime = new Date();
						myWriter.write("Date and Time :  " + new SimpleDateFormat("MMddyy HH:mm:ss").format(DateandTime) +strnxtLine);
						myWriter.write("CIF="+strCIF+", CorpCode= "+strCorpCD + strnxtLine);
						myWriter.write("TableName = " + strTableName + strnxtLine);
						myWriter.write("Column= " + strCol + strnxtLine);
						myWriter.write("ColumnVal= " + strColValue + strnxtLine);
						myWriter.write("*****************************************************"+ strnxtLine);
					    myWriter.close();
					}else {
						FileWriter myWriter = new FileWriter(cfg.reportPath + reportFilename, true);
						myWriter.write("==========ERROR REPORT" + "==========\n\n");
						myWriter.write(strPKMsg +strnxtLine);
						myWriter.write("Error Msg= " + errMsg + strnxtLine);
						
						Date DateandTime = new Date();
						myWriter.write("Date and Time :  " + new SimpleDateFormat("MMddyy HH:mm:ss").format(DateandTime) +strnxtLine);
						myWriter.write("CIF="+strCIF+", CorpCode= "+strCorpCD + strnxtLine);
						myWriter.write("TableName = " + strTableName + strnxtLine);
						myWriter.write("Column= " + strCol + strnxtLine);
						myWriter.write("ColumnVal= " + strColValue + strnxtLine);
						myWriter.write("*****************************************************"+ strnxtLine);
						myWriter.close();
						
					}

			
			} catch ( Exception e) {
				log.error(e.getMessage(), e);
//				throw new FileServiceException(e.getMessage(), e);
			}
				log.info("Error Report generated.");
			}
	
	}
//added by dave 12182023
