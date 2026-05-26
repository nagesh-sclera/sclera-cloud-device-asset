package io.sclera.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class SearchConfig {

    private static final Logger log = LoggerFactory.getLogger(SearchConfig.class);

    @Autowired
    private DataSource dataSource;

    @Bean
    public JdbcTemplate jdbcTemplate() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        registerLevenshteinFunction(jdbcTemplate);
        return jdbcTemplate;
    }

    private void registerLevenshteinFunction(JdbcTemplate jdbcTemplate) {
        // PostgreSQL migration (Phase 3): use the built-in fuzzystrmatch extension
        // which provides levenshtein() natively. The MySQL stored-procedure version
        // is skipped on PostgreSQL — callers using native SQL LEVENSHTEIN() must be
        // updated to use levenshtein() (lowercase, from fuzzystrmatch) in Phase 4.
        try {
            jdbcTemplate.execute("CREATE EXTENSION IF NOT EXISTS fuzzystrmatch");
            log.info("SearchConfig: fuzzystrmatch extension enabled (PostgreSQL levenshtein available)");
        } catch (Exception e) {
            log.warn("SearchConfig: could not enable fuzzystrmatch extension — levenshtein search may be unavailable: {}", e.getMessage());
        }
    }

}
