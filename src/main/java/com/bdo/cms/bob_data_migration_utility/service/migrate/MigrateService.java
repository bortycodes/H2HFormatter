package com.bdo.cms.bob_data_migration_utility.service.migrate;

import com.bdo.cms.bob_data_migration_utility.domain.Input;
import com.bdo.cms.bob_data_migration_utility.exception.MigrateException;

import java.util.List;
import java.util.Map;

public interface MigrateService {
    Map<String, Integer> migratePerTable(String tableName, List<Input> inputs) throws MigrateException;
}
