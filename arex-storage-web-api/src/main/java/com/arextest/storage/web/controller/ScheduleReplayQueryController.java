package com.arextest.storage.web.controller;

import com.arextest.model.mock.AREXMocker;
import com.arextest.model.replay.CountOperationCaseRequestType;
import com.arextest.model.replay.CountOperationCaseResponseType;
import com.arextest.model.replay.PagedRequestType;
import com.arextest.model.replay.PagedResponseType;
import com.arextest.model.replay.QueryCaseCountRequestType;
import com.arextest.model.replay.QueryCaseCountResponseType;
import com.arextest.model.replay.QueryMockCacheRequestType;
import com.arextest.model.replay.QueryMockCacheResponseType;
import com.arextest.model.replay.QueryReplayResultRequestType;
import com.arextest.model.replay.QueryReplayResultResponseType;
import com.arextest.model.replay.ViewRecordRequestType;
import com.arextest.model.replay.ViewRecordResponseType;
import com.arextest.model.replay.dto.ViewRecordDTO;
import com.arextest.model.replay.holder.ListResultHolder;
import com.arextest.model.response.Response;
import com.arextest.storage.mock.MockerPostProcessor;
import com.arextest.storage.repository.ProviderNames;
import com.arextest.storage.service.InvalidRecordService;
import com.arextest.storage.service.PrepareMockResultService;
import com.arextest.storage.service.ScheduleReplayingService;
import com.arextest.storage.trace.MDCTracer;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * this class defined all api list for scheduler replaying
 *
 * @author jmo
 * @since 2021/11/3
 */
@Slf4j
@RequestMapping(path = "/api/storage/replay/query", produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class ScheduleReplayQueryController {

  private final ScheduleReplayingService scheduleReplayingService;

  private final PrepareMockResultService prepareMockResultService;

  private final InvalidRecordService invalidRecordService;

  /**
   * fetch the replay result for compare
   *
   * @param requestType which record id &amp; replay id should be fetch.
   * @return response
   * @see QueryReplayResultResponseType
   */
  @PostMapping(value = "/replayResult")
  @ResponseBody
  public Response replayResult(@RequestBody QueryReplayResultRequestType requestType) {
    if (requestType == null) {
      return ResponseUtils.requestBodyEmptyResponse();
    }
    final String recordId = requestType.getRecordId();
    if (StringUtils.isEmpty(recordId)) {
      return ResponseUtils.emptyRecordIdResponse();
    }
    String replayResultId = requestType.getReplayResultId();
    if (StringUtils.isEmpty(replayResultId)) {
      return ResponseUtils.emptyReplayResultIdResponse();
    }
    try {
      MDCTracer.addRecordId(recordId);
      MDCTracer.addReplayId(replayResultId);
      List<ListResultHolder> resultHolderList =
          scheduleReplayingService.queryReplayResult(recordId, replayResultId);
      QueryReplayResultResponseType responseType = new QueryReplayResultResponseType();
      responseType.setResultHolderList(resultHolderList);
      responseType.setInvalidResult(invalidRecordService.isInvalidReplayIncompleteCase(replayResultId));
      return ResponseUtils.successResponse(responseType);
    } catch (Throwable throwable) {
      LOGGER.error("replayResult error:{} ,recordId:{} ,replayResultId:{}",
          throwable.getMessage(),
          recordId,
          replayResultId);
      return ResponseUtils.exceptionResponse(throwable.getMessage());
    } finally {
      MDCTracer.clear();
    }
  }

  /**
   * a record list by special query to replay
   *
   * @param requestType range query
   * @return response
   * @see PagedResponseType
   */
  @PostMapping(value = "/replayCase")
  @ResponseBody
  public Response replayCase(@RequestBody PagedRequestType requestType) {
    Response validateResult = rangeParameterValidate(requestType);
    if (validateResult != null) {
      return validateResult;
    }

    validateResult = pageParameterValidate(requestType);
    if (validateResult != null) {
      return validateResult;
    }

    try {
      PagedResponseType responseType = new PagedResponseType();
      responseType.setRecords(scheduleReplayingService.queryEntryPointByRange(requestType));
      return ResponseUtils.successResponse(responseType);
    } catch (Throwable throwable) {
      LOGGER.error("error:{},request:{}", throwable.getMessage(), requestType);
      return ResponseUtils.exceptionResponse(throwable.getMessage());
    }
  }

  private Response rangeParameterValidate(PagedRequestType requestType) {
    if (requestType == null) {
      return ResponseUtils.requestBodyEmptyResponse();
    }
    if (StringUtils.isEmpty(requestType.getAppId())) {
      return ResponseUtils.parameterInvalidResponse("The appId of requested is empty");
    }
    if (requestType.getBeginTime() == null) {
      return ResponseUtils.parameterInvalidResponse("The beginTime of requested is null");
    }
    if (requestType.getEndTime() == null) {
      return ResponseUtils.parameterInvalidResponse("The endTime of requested is null");
    }
    if (requestType.getBeginTime() >= requestType.getEndTime()) {
      return ResponseUtils.parameterInvalidResponse("The beginTime >= endTime from requested");
    }
    if (StringUtils.isEmpty(requestType.getSourceProvider())) {
      requestType.setSourceProvider(ProviderNames.DEFAULT);
    }
    return null;
  }

  private Response pageParameterValidate(PagedRequestType requestType) {
    if (requestType.getPageSize() <= 0) {
      return ResponseUtils.parameterInvalidResponse("The max case size <= 0 from requested");
    }
    if (requestType.getCategory() == null) {
      return ResponseUtils.parameterInvalidResponse("The category of requested is empty");
    }
    return null;
  }

  /**
   * count for query how many records should be preload to replay
   *
   * @param requestType range query
   * @return a size value
   * @see QueryCaseCountResponseType
   */
  @PostMapping(value = "/countByRange")
  @ResponseBody
  public Response countByRange(@RequestBody QueryCaseCountRequestType requestType) {
    Response validateResult = rangeParameterValidate(requestType);
    if (validateResult != null) {
      return validateResult;
    }
    try {
      QueryCaseCountResponseType responseType = new QueryCaseCountResponseType();
      long countResult = scheduleReplayingService.countByRange(requestType);
      responseType.setCount(countResult);
      return ResponseUtils.successResponse(responseType);
    } catch (Throwable throwable) {
      LOGGER.error("replayCaseCount error:{},request:{}", throwable.getMessage(), requestType,
          throwable);
      return ResponseUtils.exceptionResponse(throwable.getMessage());
    } finally {
      MDCTracer.clear();
    }
  }

  /**
   * count records cases for each operationName.
   */
  @PostMapping(value = "/countByOperationName")
  @ResponseBody
  public Response countByOperationName(@RequestBody CountOperationCaseRequestType requestType) {
    Response validateResult = rangeParameterValidate(requestType);
    if (validateResult != null) {
      return validateResult;
    }
    try {
      CountOperationCaseResponseType responseType = new CountOperationCaseResponseType();
      Map<String, Long> countResult = scheduleReplayingService.countByOperationName(requestType);
      responseType.setCountMap(countResult);
      return ResponseUtils.successResponse(responseType);
    } catch (Throwable throwable) {
      LOGGER.error("countByOperationName  error:{},request:{}", throwable.getMessage(), requestType,
          throwable);
      return ResponseUtils.exceptionResponse(throwable.getMessage());
    } finally {
      MDCTracer.clear();
    }
  }

  @ResponseBody
  @GetMapping(value = "/viewRecord/")
  public Response viewRecord(String recordId,
      @RequestParam(required = false) String category,
      @RequestParam(required = false, defaultValue = ProviderNames.DEFAULT) String srcProvider,
      @RequestHeader(name = "downgrade", required = false) String downgrade) {
    ViewRecordRequestType recordRequestType = new ViewRecordRequestType();
    recordRequestType.setRecordId(recordId);
    recordRequestType.setSourceProvider(srcProvider);
    recordRequestType.setCategoryType(category);
    return viewRecord(recordRequestType, downgrade);
  }

  /**
   * show the all records (includes entryPoint &amp; dependencies) by special recordId
   *
   * @param requestType recordId
   * @return the record content
   * @see ViewRecordResponseType
   */
  @PostMapping("/viewRecord")
  @ResponseBody
  public Response viewRecord(@RequestBody ViewRecordRequestType requestType,
      @RequestHeader(name = "downgrade", required = false) String downgrade) {
    if (requestType == null) {
      return ResponseUtils.requestBodyEmptyResponse();
    }
    String recordId = requestType.getRecordId();
    if (StringUtils.isEmpty(recordId)) {
      return ResponseUtils.emptyRecordIdResponse();
    }

    MDCTracer.addRecordId(recordId);
    ViewRecordResponseType responseType = new ViewRecordResponseType();
    try {
      ViewRecordDTO viewRecordDto = scheduleReplayingService.queryRecordList(requestType);
      List<AREXMocker> mockers = viewRecordDto.getRecordResult();
      if (CollectionUtils.isEmpty(mockers)) {
        LOGGER.info("could not found any resources for recordId: {}", recordId);
      }

      responseType.setRecordResult(mockers);
      responseType.setReplayResult(viewRecordDto.getReplayResult());
      responseType.setSourceProvider(viewRecordDto.getSourceProvider());
      if (Boolean.TRUE.toString().equals(downgrade)) {
        MockerPostProcessor.desensitize(mockers);
        responseType.setDesensitized(true);
      }
    } catch (JsonProcessingException exception) {
      responseType.setDesensitized(false);
      LOGGER.error("responseDesensitization error:{}", exception.getMessage(), exception);
    } catch (Throwable throwable) {
      LOGGER.error("viewRecord error:{}, request:{}", throwable.getMessage(), requestType);
      return ResponseUtils.exceptionResponse(throwable.getMessage());
    } finally {
      MDCTracer.clear();
    }
    return ResponseUtils.successResponse(responseType);
  }


  /**
   * preload record content to cache from repository provider
   *
   * @param requestType recordId
   * @return true loaded success
   */
  @PostMapping(value = "/cacheLoad")
  @ResponseBody
  public Response cacheLoad(@RequestBody QueryMockCacheRequestType requestType) {
    if (requestType == null) {
      return ResponseUtils.requestBodyEmptyResponse();
    }
    String recordId = requestType.getRecordId();
    if (StringUtils.isEmpty(recordId)) {
      return ResponseUtils.emptyRecordIdResponse();
    }
    if (StringUtils.isEmpty(requestType.getSourceProvider())) {
      requestType.setSourceProvider(ProviderNames.DEFAULT);
    }

    MDCTracer.addRecordId(recordId);
    long beginTime = System.currentTimeMillis();
    try {
      return toResponse(
          prepareMockResultService.preloadAll(requestType.getSourceProvider(), recordId));
    } catch (Throwable throwable) {
      LOGGER.error("QueryMockCache error:{},request:{}", throwable.getMessage(), requestType);
      return ResponseUtils.exceptionResponse(throwable.getMessage());
    } finally {
      long timeUsed = System.currentTimeMillis() - beginTime;
      LOGGER.info("cacheLoad timeUsed:{} ms,record id:{}", timeUsed, recordId);
      MDCTracer.clear();
    }
  }

  /**
   * initiative clean the cache
   *
   * @param requestType the recordId
   * @return true remove success
   */
  @PostMapping(value = "/cacheRemove")
  @ResponseBody
  public Response cacheRemove(@RequestBody QueryMockCacheRequestType requestType) {
    if (requestType == null) {
      return ResponseUtils.requestBodyEmptyResponse();
    }
    String recordId = requestType.getRecordId();
    if (StringUtils.isEmpty(recordId)) {
      return ResponseUtils.emptyRecordIdResponse();
    }
    MDCTracer.addRecordId(recordId);
    try {
      return toResponse(prepareMockResultService.removeAllRecordCache(recordId, requestType.getSourceProvider()));
    } catch (Throwable throwable) {
      LOGGER.error("QueryMockCache error:{},request:{}", throwable.getMessage(), requestType);
      return ResponseUtils.exceptionResponse(throwable.getMessage());
    } finally {
      MDCTracer.clear();
    }
  }

  private Response toResponse(boolean actionResult) {
    return actionResult ?
        ResponseUtils.successResponse(new QueryMockCacheResponseType()) :
        ResponseUtils.resourceNotFoundResponse();
  }
}
