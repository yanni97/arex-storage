package com.arextest.model.mock;


import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

@Getter
@Setter
@FieldNameConstants
@NoArgsConstructor
@Document
public class AbstractMocker{

  /**
   * 1、Only for editing dependencies,the entry point ignored 2、During query, record the id of the
   * mock, and use the id to associate data during comparison
   */
  @Field(targetType = FieldType.STRING)
  private String id;

  private String replayId;
  private String recordId;
  private String appId;
  private int recordEnvironment;

  /**
   * millis from utc format without timezone
   */
  @Field(targetType = FieldType.DATE_TIME)
  private long creationTime;
  @Field(targetType = FieldType.DATE_TIME)
  private long updateTime;
  @Field(targetType = FieldType.DATE_TIME)
  private long expirationTime;

  /**
   * the value required and empty allowed for example: pattern of servlet web api
   */
  private String operationName;
  /**
   * record the version of recorded data
   */
  private String recordVersion;

  /**
   * add tag to mocker
   */
  private Map<String, String> tags;

  /**
   * index for mergedRecord.
   */
  private Integer index;

  /**
   * Whether debugging pinned case use mock data.
   */
  private Boolean useMock;
}