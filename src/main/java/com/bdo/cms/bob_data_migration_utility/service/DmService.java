package com.bdo.cms.bob_data_migration_utility.service;

import java.util.Map;

import com.bdo.cms.bob_data_migration_utility.exception.DmServiceException;

public interface DmService {
    void generate() throws DmServiceException;
    void migrate() throws DmServiceException;
    Map<String, String> getSpecialCharacters();
}
