package com.arextest.config.utils.parse.sqlparse;

import com.arextest.config.utils.parse.sqlparse.constants.DbParseConstants;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author niyan
 * @date 2024/4/23
 * @since 1.0.0
 */
@Slf4j
public class SqlParseManager {
    private static SqlParseManager INSTANCE;

    private SqlParseManager() {
    }

    public static SqlParseManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new SqlParseManager();
        }
        return INSTANCE;
    }

    /**
     * parse table and action from sql
     * @param sql sql
     * @return Map<String, String> table and action
     */
    public Map<String, String> parseTableAndAction(String sql) {
        Map<String, String> result = new HashMap<>(2);

        if (StringUtils.isEmpty(sql)) {
            return result;
        }
        try {
            sql = sql.replaceAll("\\s+", " ");

            Statement statement = CCJSqlParserUtil.parse(sql);
            result.put(DbParseConstants.ACTION, getAction(statement));

            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            List<String> tableNames = tablesNamesFinder.getTableList(statement);
            if (tableNames != null && !tableNames.isEmpty()) {
                result.put(DbParseConstants.TABLE, String.join(",", tableNames));
            }
        } catch (Exception e) {
            LOGGER.warn("parse sql error, sql: {}", sql, e);
        }
        return result;
    }

    private String getAction(Statement statement) {
        if (statement instanceof Select) {
            return DbParseConstants.SELECT;
        } else if (statement instanceof Execute) {
            return DbParseConstants.EXECUTE;
        } else if (statement instanceof Delete) {
            return DbParseConstants.DELETE;
        } else if (statement instanceof Insert) {
            return DbParseConstants.INSERT;
        } else if (statement instanceof Replace) {
            return DbParseConstants.REPLACE;
        } else if (statement instanceof Update) {
            return DbParseConstants.UPDATE;
        } else {
            return "";
        }
    }
}
