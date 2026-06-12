package io.sclera.config;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.metamodel.model.domain.ReturnableType;
import org.hibernate.query.sqm.function.AbstractSqmSelfRenderingFunctionDescriptor;
import org.hibernate.query.sqm.function.FunctionKind;
import org.hibernate.query.sqm.produce.function.ArgumentTypesValidator;
import org.hibernate.query.sqm.produce.function.StandardArgumentsValidators;
import org.hibernate.query.sqm.produce.function.StandardFunctionReturnTypeResolvers;
import org.hibernate.sql.ast.SqlAstTranslator;
import org.hibernate.sql.ast.spi.SqlAppender;
import org.hibernate.sql.ast.tree.SqlAstNode;
import org.hibernate.type.BasicType;
import org.hibernate.type.StandardBasicTypes;

import java.util.List;

/**
 * Registers PostgreSQL-specific SQL as named HQL/Criteria functions so dynamic queries
 * (DeviceSearchQueryBuilder) stay type-safe without raw SQL strings.
 *
 * Registered via META-INF/services/org.hibernate.boot.model.FunctionContributor.
 *
 * NOTE both REGEXP_REPLACE patterns carry the 'g' flag — the earlier PG port omitted it,
 * which strips only the FIRST special character (MySQL strips all). Documented port-bug fix.
 *
 * The two character classes are intentionally DIFFERENT (verbatim from DeviceSearchService):
 *  - strip_specials:        leading "[ -." opens a space-to-dot RANGE (covers " # $ % & ' ( ) * + , - .)
 *    double-quotes ARE in the range and are stripped.
 *  - strip_custom_specials: no range; double quotes survive so %"term"% patterns can match
 *
 * API note: registerPattern() rejects literal '?' in patterns (Hibernate 7.2 treats it as
 * a positional placeholder marker). The regexp functions are therefore implemented as
 * AbstractSqmSelfRenderingFunctionDescriptor subclasses which have full rendering control.
 */
public class ScleraPgFunctionContributor implements FunctionContributor {

    @Override
    public void contributeFunctions(FunctionContributions fc) {
        var stringType = fc.getTypeConfiguration().getBasicTypeRegistry()
                .resolve(StandardBasicTypes.STRING);

        // jsonb path query — no '?' in pattern, registerPattern is fine
        fc.getFunctionRegistry().registerPattern(
                "custom_field_text",
                "jsonb_path_query_first(CAST(?1 AS jsonb), CAST(?2 AS jsonpath)) #>> '{}'",
                stringType);

        fc.getFunctionRegistry().registerPattern(
                "custom_field_array_text",
                "CAST(jsonb_path_query_array(CAST(?1 AS jsonb), CAST(?2 AS jsonpath)) AS text)",
                stringType);

        // inet cast — no '?' in pattern
        fc.getFunctionRegistry().registerPattern(
                "inet_val",
                "CAST(?1 AS inet)",
                stringType);

        // REGEXP_REPLACE functions: registerPattern() cannot encode literal '?' in pattern
        // strings (Hibernate 7.2 treats all '?' as positional argument markers), so we use
        // self-rendering function descriptors instead.
        fc.getFunctionRegistry().register(
                "strip_specials",
                new StripSpecialsFunction(stringType));

        fc.getFunctionRegistry().register(
                "strip_custom_specials",
                new StripCustomSpecialsFunction(stringType));
    }

    /**
     * strip_specials(?1):
     *   REGEXP_REPLACE(?1, '[ -.!<TAB>_+#~`@$%^&*()=;:<>?,/{}|\\]', '', 'g')
     *
     * The leading "[ -." is a space-to-dot range (ASCII 32-46), covering:
     *   SPACE ! " # $ % & ' ( ) * + , - .
     * So double-quotes ARE stripped by this function.
     */
    private static class StripSpecialsFunction extends AbstractSqmSelfRenderingFunctionDescriptor {

        StripSpecialsFunction(BasicType<String> stringType) {
            super(
                    "strip_specials",
                    FunctionKind.NORMAL,
                    StandardArgumentsValidators.exactly(1),
                    StandardFunctionReturnTypeResolvers.invariant(stringType),
                    null
            );
        }

        @Override
        public void render(SqlAppender sqlAppender, List<? extends SqlAstNode> sqlAstArguments,
                           ReturnableType<?> returnType, SqlAstTranslator<?> translator) {
            sqlAppender.appendSql("REGEXP_REPLACE(");
            sqlAstArguments.get(0).accept(translator);
            sqlAppender.appendSql(", '[ -.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\]', '', 'g')");
        }
    }

    /**
     * strip_custom_specials(?1):
     *   REGEXP_REPLACE(?1, '[-.!<TAB>_+#~`@$%^&*()=;:<>?,/{}|\\' ]', '', 'g')
     *
     * NO leading range — the '-' at position 1 (after '[') is a literal hyphen.
     * Double-quotes are NOT in the class, so they survive (needed for %"term"% patterns).
     * The single-quote inside the char class is escaped as '' in SQL.
     */
    private static class StripCustomSpecialsFunction extends AbstractSqmSelfRenderingFunctionDescriptor {

        StripCustomSpecialsFunction(BasicType<String> stringType) {
            super(
                    "strip_custom_specials",
                    FunctionKind.NORMAL,
                    StandardArgumentsValidators.exactly(1),
                    StandardFunctionReturnTypeResolvers.invariant(stringType),
                    null
            );
        }

        @Override
        public void render(SqlAppender sqlAppender, List<? extends SqlAstNode> sqlAstArguments,
                           ReturnableType<?> returnType, SqlAstTranslator<?> translator) {
            sqlAppender.appendSql("REGEXP_REPLACE(");
            sqlAstArguments.get(0).accept(translator);
            sqlAppender.appendSql(", '[-.!\t_+#~`@$%^&*()=;:<>?,/{}|\\\\'' ]', '', 'g')");
        }
    }
}
