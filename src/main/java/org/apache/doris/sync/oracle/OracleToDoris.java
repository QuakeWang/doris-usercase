package org.apache.doris.sync.oracle;

import org.apache.doris.flink.table.DorisConfigOptions;
import org.apache.doris.flink.tools.cdc.DatabaseSync;
import org.apache.doris.flink.tools.cdc.oracle.OracleDatabaseSync;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.cdc.connectors.oracle.source.config.OracleSourceOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.HashMap;
import java.util.UUID;

public class OracleToDoris {
    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        env.disableOperatorChaining();
        env.enableCheckpointing(1000);

        String database = "test_db";
        String tablePrefix = "";
        String tableSuffix = "";
        HashMap<String, String> sourceConfig = new HashMap<>();
        sourceConfig.put(OracleSourceOptions.DATABASE_NAME.key(), "helowin");
        sourceConfig.put(OracleSourceOptions.SCHEMA_NAME.key(), "FLINKUSER");
        sourceConfig.put(OracleSourceOptions.HOSTNAME.key(), "127.0.0.1");
        sourceConfig.put(OracleSourceOptions.PORT.key(), "3051");
        sourceConfig.put(OracleSourceOptions.USERNAME.key(), "flinkuser");
        sourceConfig.put(OracleSourceOptions.PASSWORD.key(), "flinkpw");
        sourceConfig.put("debezium.log.mining.strategy", "online_catalog");
        sourceConfig.put("debezium.log.mining.continuous.mine", "true");
        sourceConfig.put("debezium.database.history.store.only.captured.tables.ddl", "true");
        Configuration sourceConf = Configuration.fromMap(sourceConfig);

        HashMap<String, String> sinkConfig = new HashMap<>();
        sinkConfig.put(DorisConfigOptions.FENODES.key(), "168.43.0.1:6280");
        sinkConfig.put(DorisConfigOptions.USERNAME.key(), "root");
        sinkConfig.put(DorisConfigOptions.PASSWORD.key(), "Doris@123");
        sinkConfig.put(DorisConfigOptions.JDBC_URL.key(), "jdbc:mysql://168.43.0.1:7327");
        sinkConfig.put(DorisConfigOptions.SINK_LABEL_PREFIX.key(), UUID.randomUUID().toString());
        Configuration sinkConf = Configuration.fromMap(sinkConfig);

        boolean ignoreDefaultValue = false;
        boolean useNewSchemaChange = true;
        boolean ignoreIncompatible = false;
        DatabaseSync databaseSync = new OracleDatabaseSync();
        databaseSync
                .setEnv(env)
                .setDatabase(database)
                .setConfig(sourceConf)
                .setTablePrefix(tablePrefix)
                .setTableSuffix(tableSuffix)
                .setIgnoreDefaultValue(ignoreDefaultValue)
                .setSinkConfig(sinkConf)
                .setCreateTableOnly(false)
                .setNewSchemaChange(useNewSchemaChange)
                .setIgnoreIncompatible(ignoreIncompatible)
                .create();
        databaseSync.build();
        env.execute(String.format("Oracle-Doris Database Sync: %s", database));

    }
}
