package com.bdo.cms.bob_data_migration_utility.service.custom;

import com.bdo.cms.bob_data_migration_utility.domain.Input;
import com.bdo.cms.bob_data_migration_utility.exception.CustomException;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface CustomService {
    List<String> customInsertToDb(List<Map<String, String>> rs, String tableName, Input input, Connection connection) throws CustomException;    
    List<String> insertDefaultValuesToDb(String insert_query, String tableName, Map<String, String> field_value_map, Input input, Connection cibut_connection, Connection cib_connection) throws CustomException;
    List<String> parseToString(List<Map<String, String>> rs, String tableName, Input input, Connection cibut_connection) throws CustomException;
    List<String> rulesParsing(List<Map<String, String>> rs, String tableName, Input input, Connection cib_connection, Connection cibut_connection) throws CustomException;
    List<String> rolesParsing(List<Map<String, String>> rs, String tableName, Input input, Connection cib_connection, Connection cibut_connection) throws CustomException;
}
