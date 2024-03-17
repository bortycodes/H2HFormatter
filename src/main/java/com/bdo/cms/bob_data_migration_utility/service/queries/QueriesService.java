package com.bdo.cms.bob_data_migration_utility.service.queries;

import com.bdo.cms.bob_data_migration_utility.domain.Input;
import com.bdo.cms.bob_data_migration_utility.exception.QueriesServiceException;
import java.sql.Connection;

import java.util.List;

public interface QueriesService {
    String getGnrtSelectQuery(String tableName);
    String getGnrtSelectSubQuery(String tableName, String field);
    String getMgrtSelectQuery(String tableName);
    List<Input> removeDuplicates(List<Input> inputs, Connection cibut_connection) throws QueriesServiceException;
    void saveProcessedInputs(List<Input> inputs) throws QueriesServiceException;
    List<Input> retrieveGenerated() throws QueriesServiceException;
    void updateProcessedInputs(List<Input> inputs) throws QueriesServiceException;
    void deleteInvalidInput(Connection cibut_connection) throws QueriesServiceException;
    void truncateTempTables(Connection cibut_connection) throws QueriesServiceException;
    void truncateCMSrawTables(Connection cibut_conection) throws QueriesServiceException;
    boolean checkDuplicate(String tableName, Input inputs, Connection cibut_connection) throws QueriesServiceException;
    boolean checkPaymentsDuplicate(String tableName, Input input, String payment_product_field_name, String payment_product_name, Connection cibut_connection) throws QueriesServiceException;

}
