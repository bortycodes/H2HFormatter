package com.bdo.cms.bob_data_migration_utility.service.convert;

import com.bdo.cms.bob_data_migration_utility.domain.Input;
import com.bdo.cms.bob_data_migration_utility.exception.ConvertException;
import java.sql.Connection;

import java.util.List;

public interface UsergpCodeConvertService {
    void convert(List<Input> inputs, Connection cib, Connection cibut) throws ConvertException;
}
