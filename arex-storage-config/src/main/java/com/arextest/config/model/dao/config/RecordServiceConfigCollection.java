package com.arextest.config.model.dao.config;

import com.arextest.config.model.dao.BaseEntity;
import com.arextest.config.model.dao.MultiEnvBaseEntity;
import com.arextest.config.model.dto.record.SerializeSkipInfoConfiguration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@FieldNameConstants
@Document(RecordServiceConfigCollection.DOCUMENT_NAME)
public class RecordServiceConfigCollection extends
    MultiEnvBaseEntity<RecordServiceConfigCollection> {

  public static final String DOCUMENT_NAME = "RecordServiceConfig";

  @NonNull
  private String appId;

  private int sampleRate;

  private int allowDayOfWeeks;

  private boolean timeMock;
  @NonNull
  private String allowTimeOfDayFrom;
  @NonNull
  private String allowTimeOfDayTo;

  private Set<String> excludeServiceOperationSet;

  private Integer recordMachineCountLimit;

  private Map<String, String> extendField;

  private List<SerializeSkipInfoConfiguration> serializeSkipInfoList;
}
