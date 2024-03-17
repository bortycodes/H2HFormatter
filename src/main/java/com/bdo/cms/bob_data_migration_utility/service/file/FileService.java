package com.bdo.cms.bob_data_migration_utility.service.file;

import com.bdo.cms.bob_data_migration_utility.domain.Input;
import com.bdo.cms.bob_data_migration_utility.exception.FileServiceException;

import java.util.List;
import java.util.Map;

public interface FileService {

    List<Input> read() throws FileServiceException;   
    void write(List<String> records, String tableName) throws FileServiceException;    
    void archive() throws FileServiceException;
    Map<Integer, String> fieldMap(List<String> fields);
    void createReport(Map<String, Integer> numberOfRecordsPerTable, int newRecords, int duplicates, String process) throws FileServiceException;
    
//    added by dave 12152023
    void createErrReport(String strCIF, String strCorpCD, String errMsg,String strTableName, String strColValue,String strCol, String process);
}
