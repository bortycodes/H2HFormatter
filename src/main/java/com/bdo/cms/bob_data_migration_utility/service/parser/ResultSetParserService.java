package com.bdo.cms.bob_data_migration_utility.service.parser;

import com.bdo.cms.bob_data_migration_utility.exception.ResultSetParserException;
import com.bdo.cms.bob_data_migration_utility.domain.Input;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface ResultSetParserService {
    List<String> parseToString(List<Map<String, String>> rs, String tableName) throws ResultSetParserException;
    List<String> rsParserInsertToDb(List<Map<String, String>> rs, String tableName, Input input, Connection connection) throws ResultSetParserException;
	String replaceSpecialCharacters(String string, Map<String, String> specialCharacters, boolean isUsersTable);
	String padLoginId(String loginId);
}
