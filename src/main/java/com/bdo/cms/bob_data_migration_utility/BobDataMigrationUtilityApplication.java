package com.bdo.cms.bob_data_migration_utility;

import com.bdo.cms.bob_data_migration_utility.command.BobDmuCommand;
import com.bdo.cms.bob_data_migration_utility.command.GenerateCommand;
import com.bdo.cms.bob_data_migration_utility.command.GenerateMigrateCommand;
import com.bdo.cms.bob_data_migration_utility.command.MigrateCommand;
import com.bdo.cms.bob_data_migration_utility.command.TruncateCommand;
import com.bdo.cms.bob_data_migration_utility.config.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import picocli.CommandLine;

@Slf4j
@SpringBootApplication
public class BobDataMigrationUtilityApplication implements CommandLineRunner {

    @Autowired
    AppConfig appCfg;
        
    private final BobDmuCommand bobDmuCommand;
    private final GenerateCommand generateCommand;
    private final MigrateCommand migrateCommand;
    private final TruncateCommand truncateCommand;
    private final GenerateMigrateCommand genemigrateCommand;

    public BobDataMigrationUtilityApplication(BobDmuCommand bobDmuCommand, GenerateCommand generateCommand, MigrateCommand migrateCommand, TruncateCommand truncateCommand, GenerateMigrateCommand genemigrateCommand) {
            this.bobDmuCommand = bobDmuCommand;
            this.generateCommand = generateCommand;
            this.migrateCommand = migrateCommand;
            this.truncateCommand = truncateCommand;
            this.genemigrateCommand = genemigrateCommand;
    }

    public static void main(String[] args) {
            new SpringApplicationBuilder(BobDataMigrationUtilityApplication.class).web(WebApplicationType.NONE).run(args);
    }

    @Override
    public void run(String... args) {
            log.info("========== BOB DMU started ==========");
            log.info("BOBDMU v-{}, r{}", appCfg.version, appCfg.build_release);

            CommandLine commandLine = new CommandLine(bobDmuCommand);
            commandLine.addSubcommand("generate", generateCommand);
            commandLine.addSubcommand("migrate", migrateCommand);
            commandLine.addSubcommand("truncate", truncateCommand);
            commandLine.addSubcommand("genemigrate", genemigrateCommand);

            commandLine.setExecutionStrategy(new CommandLine.RunLast()).execute(args);
    }
}
