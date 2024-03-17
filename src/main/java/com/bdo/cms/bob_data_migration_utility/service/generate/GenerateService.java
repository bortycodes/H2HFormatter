package com.bdo.cms.bob_data_migration_utility.service.generate;

import com.bdo.cms.bob_data_migration_utility.domain.Input;
import com.bdo.cms.bob_data_migration_utility.exception.GenerateException;
import java.sql.Connection;

import java.util.List;
import java.util.Map;

public interface GenerateService {
    Map<String, Integer> generatePerTable(String tableName, List<Input> inputs, Connection cib, Connection cibut) throws GenerateException;
    Map<String, Integer> generateUserTables(List<Input> unique_corp_inputs, List<Input> inputs, Connection cib, Connection cibut) throws GenerateException;
}
