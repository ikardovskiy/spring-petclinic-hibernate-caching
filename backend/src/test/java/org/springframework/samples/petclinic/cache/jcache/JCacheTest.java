package org.springframework.samples.petclinic.cache.jcache;

import org.junit.jupiter.api.Test;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.spi.CachingProvider;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JCacheTest {
    @Test
    void shouldFindCacheProviders(){
        Iterable<CachingProvider> cachingProviders = Caching.getCachingProviders();
        List<CachingProvider> cachingProvidersList = new ArrayList<>();
        cachingProviders.forEach(cachingProvidersList::add);

        assertThat(cachingProvidersList)
                .isNotEmpty();
        cachingProvidersList.stream()
                .map(CachingProvider::getClass)
                .map(Class::getCanonicalName)
                .forEach(System.out::println);
        //org.ehcache.jsr107.EhcacheCachingProvider
    }

    @Test
    void shouldReturnPreconfiguredCaches(){
        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();
        Iterable<String> cacheNames = cacheManager.getCacheNames();
        List<String> cacheNameList = new ArrayList<>();
        cacheNames.forEach(cacheNameList::add);
        cacheNameList.forEach(System.out::println);
    }

    @Test
    void shouldDoSimpleCaching(){
        CachingProvider cachingProvider = Caching.getCachingProvider();
        CacheManager cacheManager = cachingProvider.getCacheManager();
        MutableConfiguration<Integer,Integer> configuration = new MutableConfiguration<>();
        configuration.setStatisticsEnabled(true);
        Cache<Integer, Integer> cache = cacheManager.createCache("test", configuration);
        cache.put(1,1);
        Integer notCachedValue = cache.get(0);
        Integer cachedValue = cache.get(1);
        assertThat(notCachedValue)
                .isNull();
        assertThat(cachedValue)
                .isEqualTo(1);
    }

}
