package com.arextest.storage.service.mockerhandlers;

import com.arextest.config.utils.parse.sqlparse.SqlParseManager;
import com.arextest.model.constants.MockAttributeNames;
import com.arextest.model.mock.AREXMocker;
import com.arextest.model.mock.MockCategoryType;
import com.arextest.model.mock.Mocker;
import com.arextest.storage.mock.MockResultProvider;
import com.arextest.storage.repository.ProviderNames;
import com.arextest.storage.service.MockSourceEditionService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author niyan
 * @date 2024/4/23
 * @since 1.0.0
 */
@Component
@Slf4j
public class DatabaseMockerHandel implements MockerSaveHandler {
    private MockResultProvider mockResultProvider;
    private MockSourceEditionService mockSourceEditionService;

    @Override
    public MockCategoryType getMockCategoryType() {
        return MockCategoryType.DATABASE;
    }

    @Override
    public void handle(Mocker item) {

        AREXMocker mocker = (AREXMocker) parseMocker(item);
        mockResultProvider.calculateEigen(mocker, true);
        mockSourceEditionService.add(ProviderNames.DEFAULT, mocker);
    }


    public Mocker parseMocker(Mocker item) {
        AREXMocker mocker = (AREXMocker) item;

        String originOperationName = mocker.getOperationName();
        Mocker.Target targetRequest = mocker.getTargetRequest();
        String dbName = "";
        String sqlBody = "";
        if (targetRequest != null) {
            dbName = (String) targetRequest.getAttribute(MockAttributeNames.DB_NAME);
            sqlBody = targetRequest.getBody();
        }
        // sqlParse
        if (StringUtils.isNotBlank(sqlBody)) {
            String[] splitSql = sqlBody.split(";");
            StringBuilder basicInfo = new StringBuilder();
            Map<String, String> tableAndAction = null;
            for (String subSql : splitSql) {
                tableAndAction = SqlParseManager.getInstance().parseTableAndAction(subSql);
                if (MapUtils.isNotEmpty(tableAndAction)) {
                    String action = tableAndAction.getOrDefault("action", "");
                    String tableName = tableAndAction.getOrDefault("table", "");
                    basicInfo.append(dbName).append('-').append(tableName).append('-').append(action).append("-").append(originOperationName).append(";");
                }
            }
            mocker.setOperationName(basicInfo.toString());
        }
        return mocker;
    }
}
