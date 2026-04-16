package com.erp.manufacturing;

import com.erp.integration.IUIService;
import com.erp.integration.SqlManufacturingUIService;
import com.erp.manufacturing.command.DefaultManufacturingCommandFactory;
import com.erp.manufacturing.command.ManufacturingCommandFactory;
import com.erp.manufacturing.facade.DefaultManufacturingFacade;
import com.erp.manufacturing.facade.ManufacturingFacade;
import com.erp.manufacturing.integration.SqlSubsystemAdapter;
import com.erp.manufacturing.repository.JdbcManufacturingRepository;
import com.erp.manufacturing.repository.ManufacturingRepository;

/**
 * Creates manufacturing module infrastructure.
 *
 * PATTERN: Factory (Creational)
 */
public final class ManufacturingModuleFactory {

    private ManufacturingModuleFactory() {}

    public static IUIService createSqlBackedUIService(IUIService fallbackService) {
        String jdbcUrl = jdbcUrlFromSystemProperty();
        ManufacturingRepository repository = new JdbcManufacturingRepository(jdbcUrl);
        SqlSubsystemAdapter subsystemAdapter = new SqlSubsystemAdapter(repository);
        ManufacturingCommandFactory commandFactory =
                new DefaultManufacturingCommandFactory(repository, subsystemAdapter);

        ManufacturingFacade facade = new DefaultManufacturingFacade(
                repository,
                commandFactory,
                subsystemAdapter,
                subsystemAdapter,
                subsystemAdapter,
                subsystemAdapter,
                subsystemAdapter,
                subsystemAdapter);

        return new SqlManufacturingUIService(fallbackService, facade);
    }

    private static String jdbcUrlFromSystemProperty() {
        String raw = System.getProperty("com.erp.mfg.db", "data/mfg_demo.db");
        if (raw.startsWith("jdbc:")) {
            return raw;
        }
        return "jdbc:sqlite:" + raw;
    }
}
