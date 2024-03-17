package com.bdo.cms.bob_data_migration_utility.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class FileConfig {

    @Value("${file.delimiter.input}")
    public String inputDelimiter;

    @Value("${file.delimiter.output}")
    public String outputDelimiter;

    @Value("${file.date.format}")
    public String dateFormat;

    @Value("${file.date.keywords}")
    public List<String> dateKeywords;

    @Value("${file.path.output}")
    public String outputPath;

    @Value("${file.path.input}")
    public String inputPath;

    @Value("${file.path.input.filename}")
    public String inputFilename;

    @Value("${file.path.report}")
    public String reportPath;

    @Value("${file.report.extract}")
    public String extractReportFilename;

    @Value("${file.report.migrate}")
    public String migrateReportFilename;

    @Value("${file.path.archive}")
    public String archivePath;
    
//  added by dave 12182023
    @Value("${file.report.error}")
    public String errorReport;
}
