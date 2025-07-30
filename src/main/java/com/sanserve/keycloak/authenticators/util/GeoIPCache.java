package com.sanserve.keycloak.authenticators.util;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CountryResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class GeoIPCache {
    private  final String DB_PATH;
    private  DatabaseReader reader;
    private static final ConcurrentHashMap<String, CacheEntry> countryCache = new ConcurrentHashMap<>();
    private static final long TTL_MILLIS = 24*60 * 60 * 1000; // 5 minutes
    private static GeoIPCache instance;

    private GeoIPCache(String dbPath){
        DB_PATH=dbPath;
        try {
            reader = new DatabaseReader.Builder(new File(DB_PATH)).build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load GeoIP DB", e);
        }
    }
    public static GeoIPCache getInstance(String dbPath){
        if(instance ==null){
            instance = new GeoIPCache(dbPath);
        }
        return instance;
    }

    public String getCountry(String ip) {
        try {
            CacheEntry entry = countryCache.get(ip);
            long now = System.currentTimeMillis();
            if (entry != null && now - entry.timestamp < TTL_MILLIS) {
                return entry.country;
            }

            InetAddress ipAddress = InetAddress.getByName(ip);
            CountryResponse response = reader.country(ipAddress);
            String country = response.getCountry().getIsoCode();

            countryCache.put(ip, new CacheEntry(country, now));
            return country;
        } catch (Exception e) {
            log.warn("Error to load country",e);
            return "UNKNOWN";
        }
    }

    private static class CacheEntry {
        String country;
        long timestamp;

        CacheEntry(String country, long timestamp) {
            this.country = country;
            this.timestamp = timestamp;
        }
    }
}
