/*
 * Copyright 2018 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.adaptris.core.services.cache;

import java.util.ArrayList;
import java.util.Arrays;

import com.adaptris.core.BranchingServiceCollection;
import com.adaptris.core.jms.JmsConstants;
import com.adaptris.core.services.LogMessageService;
import com.adaptris.core.services.cache.CacheServiceExample.CacheExampleServiceGenerator;
import com.adaptris.core.services.cache.translators.JmsReplyToCacheValueTranslator;
import com.adaptris.core.services.cache.translators.MetadataCacheValueTranslator;
import com.adaptris.core.services.cache.translators.ObjectMetadataCacheValueTranslator;
import com.adaptris.core.services.cache.translators.StringPayloadCacheTranslator;
import com.adaptris.core.services.cache.translators.XpathCacheValueTranslator;

public abstract class BasicCacheExampleGenerator {

  public static Iterable<CacheExampleServiceGenerator> generators() {
    return Arrays.asList(
        () -> { return createAddToCacheService(); },
        () -> { return createRetrieveFromCache(); },
        () -> { return createRemoveFromCache(); },
        () -> { return createCheckCache(); },
        () -> { return createCheckAndRetrieveCache(); }
    );
  }

  public static AddToCacheService createAddToCacheService() {
    AddToCacheService service = new AddToCacheService();
    CacheEntryEvaluator eval1 = new CacheEntryEvaluator();
    CacheEntryEvaluator eval2 = new CacheEntryEvaluator();
    CacheEntryEvaluator eval3 = new CacheEntryEvaluator();
    CacheEntryEvaluator eval4 = new CacheEntryEvaluator();
    CacheEntryEvaluator eval5 = new CacheEntryEvaluator();

    eval1.setKeyTranslator(new MetadataCacheValueTranslator("A_MetadataKey_Whose_Value_Makes_The_Cache_Key"));
    eval1.setValueTranslator(new MetadataCacheValueTranslator("Another_MetadataKey_Whose_Value_Makes_The_Cache_CacheValue"));

    eval2.setKeyTranslator(new MetadataCacheValueTranslator("A_MetadataKey_Whose_Value_Makes_The_Cache_Key"));
    eval2.setValueTranslator(new StringPayloadCacheTranslator());

    eval3.setKeyTranslator(new MetadataCacheValueTranslator("A_MetadataKey_Whose_Value_Makes_The_Cache_Key"));
    eval3.setValueTranslator(new XpathCacheValueTranslator("/some/xpath/value"));

    eval4.setKeyTranslator(new MetadataCacheValueTranslator("JMSCorrelationID"));
    eval4.setValueTranslator(new JmsReplyToCacheValueTranslator());

    eval5.setKeyTranslator(new MetadataCacheValueTranslator("A_MetadataKey_Whose_Value_Makes_The_Cache_Key"));
    eval5.setValueTranslator(new ObjectMetadataCacheValueTranslator(JmsConstants.OBJ_JMS_REPLY_TO_KEY));

    service.setCacheEntryEvaluators(new ArrayList(Arrays.asList(new CacheEntryEvaluator[]
    {
        eval1, eval2, eval3, eval4, eval5
    })));

    return service;
  }

  public static RetrieveFromCacheService createRetrieveFromCache() {
    return configureRetrieveService(new RetrieveFromCacheService());
  }

  public static RemoveFromCacheService createRemoveFromCache() {
    return configureRetrieveService(new RemoveFromCacheService());
  }

  private static <T extends RetrieveFromCacheService> T configureRetrieveService(T service) {
    CacheEntryEvaluator eval1 = new CacheEntryEvaluator();
    CacheEntryEvaluator eval2 = new CacheEntryEvaluator();
    CacheEntryEvaluator eval3 = new CacheEntryEvaluator();

    eval1.setKeyTranslator(new MetadataCacheValueTranslator("A_MetadataKey_Whose_Value_Is_The_Cache_key"));
    eval1.setValueTranslator(new MetadataCacheValueTranslator("A_MetadataKey_Which_Will_Contain_What_We_Find_in_The_Cache"));

    eval2.setKeyTranslator(
        new MetadataCacheValueTranslator("MetadataKey_Whose_Value_Is_The_Cache_Key_And_This_Key_Contains_A_Payload"));
    eval2.setValueTranslator(new StringPayloadCacheTranslator());

    eval3.setKeyTranslator(new MetadataCacheValueTranslator("JMSCorrelationID"));
    eval3.setValueTranslator(new JmsReplyToCacheValueTranslator());

    service.setCacheEntryEvaluators(new ArrayList(Arrays.asList(new CacheEntryEvaluator[]
    {
        eval1, eval2, eval3
    })));
    return (T) service;
  }

  public static BranchingServiceCollection createCheckCache() {
    return new BranchingServiceCollection().withFirstServiceId("checkCache").withServices(
        configureCheckCache(new CheckCacheService()), new LogMessageService("AllKeysFoundInCache"),
        new LogMessageService("Not_All_Keys_In_Cache"));
  }

  public static BranchingServiceCollection createCheckAndRetrieveCache() {
    return new BranchingServiceCollection().withFirstServiceId("checkCache").withServices(
        configureCheckCache(new CheckAndRetrieve()), new LogMessageService("AllKeysFoundInCache"),
        new LogMessageService("Not_All_Keys_In_Cache"));
  }

  private static <T extends CheckCacheService> T configureCheckCache(T cacheService) {
    CacheEntryEvaluator eval1 = new CacheEntryEvaluator();
    CacheEntryEvaluator eval2 = new CacheEntryEvaluator();
    eval1.setKeyTranslator(new MetadataCacheValueTranslator("A_MetadataKey_Whose_Value_Makes_The_Cache_Key"));
    eval2.setKeyTranslator(new MetadataCacheValueTranslator("Another_MetadataKey_Whose_Value_Makes_The_Cache_Key"));
    cacheService.setCacheEntryEvaluators(new ArrayList(Arrays.asList(new CacheEntryEvaluator[]
    {
        eval1, eval2
    })));
    cacheService.setKeysFoundServiceId("AllKeysFoundInCache");
    cacheService.setKeysNotFoundServiceId("Not_All_Keys_In_Cache");
    cacheService.setUniqueId("checkCache");
    return (T) cacheService;
  }
}
