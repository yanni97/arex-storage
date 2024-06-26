package com.arextest.config.model.dto.record;

import com.arextest.config.model.dto.AbstractConfiguration;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author jmo
 * @since 2021/12/22
 */
@Getter
@Setter
@ToString
public class DynamicClassConfiguration extends AbstractConfiguration {

  private String id;
  private String appId;
  private String fullClassName;
  private String methodName;
  private String parameterTypes;

  /**
   * from system provide or user custom provide
   */
  private int configType;

  private String keyFormula;

}

