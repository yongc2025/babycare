package com.huigrowth.babycare.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseCharsetInitializer {

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.database.ensure-utf8mb4:false}")
    private boolean ensureUtf8mb4;

    @EventListener(ApplicationReadyEvent.class)
    public void ensureUtf8mb4Charset() {
        if (!ensureUtf8mb4) {
            return;
        }
        if (!isMysql()) {
            log.info("跳过数据库字符集检查：当前数据源不是 MySQL");
            return;
        }

        String databaseName = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
        if (databaseName == null || databaseName.isBlank()) {
            log.warn("跳过数据库字符集检查：当前连接没有选中数据库");
            return;
        }

        jdbcTemplate.execute("ALTER DATABASE `" + databaseName + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        List<String> tableNames = jdbcTemplate.queryForList(
                """
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = DATABASE()
                  AND table_type = 'BASE TABLE'
                  AND table_collation NOT LIKE 'utf8mb4%'
                """,
                String.class);

        for (String tableName : tableNames) {
            jdbcTemplate.execute("ALTER TABLE `" + tableName + "` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }

        if (!tableNames.isEmpty()) {
            log.info("已将数据库 {} 的 {} 张表转换为 utf8mb4", databaseName, tableNames.size());
        }
    }

    private boolean isMysql() {
        try (Connection connection = dataSource.getConnection()) {
            String productName = connection.getMetaData().getDatabaseProductName();
            return productName != null && productName.toLowerCase().contains("mysql");
        } catch (SQLException error) {
            log.warn("数据库类型检查失败，跳过字符集检查", error);
            return false;
        }
    }
}
