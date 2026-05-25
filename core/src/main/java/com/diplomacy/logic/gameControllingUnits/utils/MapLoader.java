package com.diplomacy.logic.gameControllingUnits.utils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.diplomacy.logic.geography.advanced.Country;
import com.diplomacy.logic.geography.advanced.GameMap;
import com.diplomacy.logic.geography.basic.Location;
import com.diplomacy.logic.geography.basic.Province;
import com.diplomacy.logic.geography.utils.ProvinceSignature;
import com.diplomacy.logic.geography.utils.ProvinceType;
import com.google.gson.Gson;

public class MapLoader {

    private static final String MAP_DATA_PATH = "/data/map_data_europe_1900.json";
    private static final Gson GSON = new Gson();

    private static class RawMapData {
        Map<String, CountryData> countries;
        Map<String, ProvinceData> provinces;
    }

    private static class CountryData {
        String name;
        List<String> home_centers;
        Map<String, String> starting_units;
    }

    private static class ProvinceData {
        String name;
        String type;
        boolean is_supply_center;
        String hex_ID;
        List<Double> supply_center_coords;
        Map<String, LocationData> locations;
    }

    private static class LocationData {
        List<String> adjacency;
        List<Double> unit_coords;
    }

    public static GameMap load() {
        RawMapData raw = parseJson(MAP_DATA_PATH, RawMapData.class);

        Map<String, Province> provincesById = new HashMap<>();
        Map<String, Location> locationsByFullId = new HashMap<>();

        createProvincesAndLocations(raw, provincesById, locationsByFullId); //++
        linkNeighboringLocations(raw, provincesById, locationsByFullId); 
        List<Country> countries = createCountries(raw, provincesById);

        System.out.println("Countries and map are loaded successfully");
        return new GameMap(new ArrayList<>(provincesById.values()), countries);
    } // ++

     public static Map<String, Map<String, Object>> loadUiData() {
        RawMapData raw = parseJson(MAP_DATA_PATH, RawMapData.class);
        Map<String, Map<String, Object>> uiDataMap = new HashMap<>();

        for (var entry : raw.provinces.entrySet()) {
            String provinceId = entry.getKey();
            ProvinceData data = entry.getValue();

            Map<String, Object> provinceUi = new HashMap<>();
            provinceUi.put("hex_ID", data.hex_ID);

            Map<String, List<Double>> locationCoords = new HashMap<>();
            if (data.locations != null) {
                data.locations.forEach((locName, locData) -> {
                    if (locData.unit_coords != null) {
                        locationCoords.put(locName, locData.unit_coords);
                    }
                });
            }
            provinceUi.put("locations", locationCoords);

            if (data.supply_center_coords != null) {
                provinceUi.put("supply_center_coords", data.supply_center_coords);
            }

            uiDataMap.put(provinceId, provinceUi);
        }
        return uiDataMap;
    } // ?? Надеюсь, что работает

    private static <T> T parseJson(String resourcePath, Class<T> clazz) {
        try (Reader reader = openResource(resourcePath)) {
            return GSON.fromJson(reader, clazz);
        } catch (Exception e) {
            throw new MapLoadException("Failed to parse " + resourcePath, e);
        }
    } // ++

    private static Reader openResource(String path) {
        var stream = MapLoader.class.getResourceAsStream(path);
        if (stream == null) {
            throw new MapLoadException("Resource not found: " + path);
        }
        return new InputStreamReader(stream);
    } // ++

    private static void createProvincesAndLocations(RawMapData raw,
                                                    Map<String, Province> provincesById,
                                                    Map<String, Location> locationsByFullId) {
        for (var entry : raw.provinces.entrySet()) {
            String provinceId = entry.getKey();
            ProvinceData data = entry.getValue();

            String typeStr = data.type;
            if ("SEA".equalsIgnoreCase(typeStr)) {
                typeStr = "WATER";
            }

            ProvinceType type;
            try {   
                type = ProvinceType.valueOf(typeStr);
            } catch (IllegalArgumentException e) {
                throw new MapLoadException("Unknown province type: " + data.type + " for province " + provinceId);
            }

            ProvinceSignature signature = new ProvinceSignature(data.name, data.is_supply_center, type);
            Province province = new Province(provinceId, signature, new ArrayList<>());
            provincesById.put(provinceId, province);

            if (data.locations != null) {
                data.locations.forEach((locName, locData) -> {
                    Location location = new Location(province, locName);
                    province.addLocation(location);
                    String fullId = buildLocationFullId(provinceId, locName);
                    locationsByFullId.put(fullId, location);
                });
            }
        }
    } // ++

    private static void linkNeighboringLocations(RawMapData raw,
                                                 Map<String, Province> provincesById,
                                                 Map<String, Location> locationsByFullId) {
        for (var entry : raw.provinces.entrySet()) {
            String currentProvinceId = entry.getKey();
            ProvinceData data = entry.getValue();
            if (data.locations == null)
                continue;

            for (var locEntry : data.locations.entrySet()) {
                String currentLocName = locEntry.getKey();
                Location currentLocation = locationsByFullId.get(buildLocationFullId(currentProvinceId, currentLocName));
                if (currentLocation == null) 
                    continue;

                List<String> adjacency = locEntry.getValue().adjacency;
                if (adjacency == null) 
                    continue;

                for (String rawNeighbor : adjacency) {
                    Location neighbor = resolveNeighbor(rawNeighbor,
                                                        currentProvinceId, 
                                                        currentLocName, 
                                                        provincesById, 
                                                        locationsByFullId);
                    if (neighbor != null) {
                        currentLocation.addNeighbour(neighbor);
                    } else {
                        throw new MapLoadException(String.format(
                                "Cannot build a connection: %s -> %s. Check JSON data.",
                                buildLocationFullId(currentProvinceId, currentLocName),
                                rawNeighbor));
                    }
                }
            }
        }
    } // ++

    private static Location resolveNeighbor(String rawNeighbor,
                                            String currentProvinceId,
                                            String currentLocName,
                                            Map<String, Province> provincesById,
                                            Map<String, Location> locationsByFullId) {
        String internalKey = buildLocationFullId(currentProvinceId, rawNeighbor);
        if (locationsByFullId.containsKey(internalKey)) {
            return locationsByFullId.get(internalKey);
        } // Соседние берега?

        Province neighborProvince = provincesById.get(rawNeighbor);
        if (neighborProvince == null)
            return null;

        return findPrimaryLocation(neighborProvince, currentLocName);
    } //++

    private static Location findPrimaryLocation(Province province, String sourceLocName) {
        List<Location> locs = province.getLocations();
        if (locs.isEmpty()) {
            throw new MapLoadException("Province has no locations: " + province.getSignature().getName());
        }

        String sourceCategory = getLocCategory(sourceLocName);

        for (Location loc : locs) {
            if (getLocCategory(loc.getName()).equals(sourceCategory)) {
                return loc;
            }
        }

        for (Location loc : locs) {
            if (areCategoriesCompatible(sourceCategory, getLocCategory(loc.getName()))) {
                return loc;
            }
        }

        throw new MapLoadException(String.format(
                "Cannot find compatible location in province %s for source category '%s'",
                province.getSignature().getName(), sourceCategory));
    } // ++

    private static String getLocCategory(String locName) {
        if (locName == null) return "land";
        if (locName.equals("land")) return "land";
        if (locName.startsWith("coast")) return "coast";
        if (locName.equals("sea")) return "sea";
        return "land";
    } // ++

    private static boolean areCategoriesCompatible(String cat1, String cat2) {
        if (cat1.equals(cat2)) return true;
        if (cat1.equals("coast")) return cat2.equals("sea") || cat2.equals("land");
        if (cat2.equals("coast")) return cat1.equals("sea") || cat1.equals("land");
        return false;
    } // ++

    private static List<Country> createCountries(RawMapData raw,
                                                Map<String, Province> provincesById) {
        if (raw.countries == null) {
            throw new MapLoadException("Countries data is missing");
        }

        return raw.countries.entrySet().stream().map(entry -> {
            CountryData data = entry.getValue();
            List<Province> provinces = data.home_centers == null ? List.of() :
                    data.home_centers.stream()
                            .map(id -> provincesById.get(id))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

            Map<Province, String> startingUnits = new HashMap<>();
            if (data.starting_units != null) {
                data.starting_units.forEach((provinceId, unitType) -> {
                    Province province = provincesById.get(provinceId);
                    if (province != null) {
                        startingUnits.put(province, unitType);
                    } else {
                        System.out.println("Province " + provinceId + " not found for starting unit of " + data.name);
                    }
                });
            }

            return new Country(data.name, provinces, startingUnits);
        }).collect(Collectors.toList());
    } // ?? Надеюсь, что работает

    private static String buildLocationFullId(String provinceId, String locationName) {
        return provinceId + "_" + locationName;
    } // ++

    public static class MapLoadException extends RuntimeException {
        public MapLoadException(String message) {
            super(message);
        }
        public MapLoadException(String message, Throwable cause) {
            super(message, cause);
        }
    } // ++
}

// В uppercase переводить нельзя. Ведь Болгария - Bul, Чёрное море BLA