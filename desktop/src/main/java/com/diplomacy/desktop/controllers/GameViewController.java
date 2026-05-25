package com.diplomacy.desktop.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.girod.javafx.svgimage.SVGImage;
import org.girod.javafx.svgimage.SVGLoader;

import com.diplomacy.logic.gameControllingUnits.GameMaster;
import com.diplomacy.logic.gameControllingUnits.utils.MapLoader;
import com.diplomacy.logic.geography.advanced.GameMap;
import com.diplomacy.logic.geography.basic.Province;
import com.diplomacy.logic.orders.utils.OrderType;
import com.diplomacy.logic.player.Password;
import com.diplomacy.logic.player.Player;
import com.diplomacy.logic.units.Unit;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;

public class GameViewController {

    @FXML private StackPane mapContainer;
    @FXML private ListView<String> ordersListView;

    private GameMaster gameMaster;
    private MapView mapView;
    private MapInputHandler inputHandler;
    private OrderController orderController;

    // имена файлов ресурсов (лежат в desktop/src/main/resources/maps/)
    private final String mapSvgName = "map_europe_1900.svg";
    private final String mapMaskName = "map_mask_europe_1900.png";

    @FXML
    public void initialize() {
        // 1. Загружаем карту (модель)
        GameMap gameMap = MapLoader.load();

        // 2. Создаём игроков (пока заглушка — только две страны)
        List<Player> players = new ArrayList<>();
        players.add(new Player(gameMap.getCountry("France"), new Password("12345")));
        //players.add(new Player("player 1", gameMap.getCountry("France")));
        //players.add(new Player("player 2", gameMap.getCountry("England")));
        // TODO: добавить остальные страны, либо загружать из конфига

        // 3. Создаём GameMaster и расставляем юниты
        gameMaster = new GameMaster(players, gameMap, null);
        gameMaster.initializeStartPositions();

        // 4. Загружаем UI-данные (координаты, hex_ID)
        Map<String, Map<String, Object>> rawUi = MapLoader.loadUiData();
        Image mask = new Image(getClass().getResourceAsStream("/maps/" + mapMaskName));
        Map<Integer, Province> colorToProvince = buildColorMapping(rawUi, gameMap);
        Map<String, List<double[]>> unitCoordinates = extractUnitCoords(rawUi);
        Set<String> provinceIds = rawUi.keySet();

        // 5. Загружаем SVG карты – теперь через новую библиотеку
        SVGImage svgRoot = loadSvg();
        mapView = new MapView(svgRoot, provinceIds, unitCoordinates);
        mapContainer.getChildren().add(mapView.getView());

        // 6. Создаём контроллер приказов и обработчик ввода
        orderController = new OrderController(gameMaster, mapView, ordersListView);
        inputHandler = new MapInputHandler(mapView, mask, colorToProvince, orderController);

        // 7. Отрисовываем начальное положение юнитов
        renderInitialUnits();
    }

    private void renderInitialUnits() {
        for (Unit unit : gameMaster.getUnits()) {
            String provinceId = unit.getLocation().getParentProvince().getId();
            mapView.addUnit(unit, provinceId);
        }
    }

    private Map<Integer, Province> buildColorMapping(Map<String, Map<String, Object>> rawUi, GameMap gameMap) {
        Map<Integer, Province> map = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : rawUi.entrySet()) {
            String provId = entry.getKey();
            Map<String, Object> data = entry.getValue();
            String hexStr = (String) data.get("hex_ID");
            if (hexStr != null) {
                int color = 0xFF000000 | Integer.parseInt(hexStr, 16);
                Province province = gameMap.getProvince(provId);
                if (province != null) {
                    map.put(color, province);
                }
            }
        }
        return map;
    }

    private Map<String, List<double[]>> extractUnitCoords(Map<String, Map<String, Object>> rawUi) {
        Map<String, List<double[]>> result = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : rawUi.entrySet()) {
            String provId = entry.getKey();
            Map<String, Object> data = entry.getValue();
            Object locationsObj = data.get("locations");
            if (locationsObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, List<Double>> locsMap = (Map<String, List<Double>>) locationsObj;
                List<double[]> coordsList = new ArrayList<>();
                for (Map.Entry<String, List<Double>> locEntry : locsMap.entrySet()) {
                    List<Double> list = locEntry.getValue();
                    if (list != null && list.size() >= 2) {
                        coordsList.add(new double[]{list.get(0), list.get(1)});
                    }
                }
                result.put(provId, coordsList);
            }
        }
        return result;
    }

    @FXML
    private void onMoveButtonClick() {
        orderController.setOrderType(OrderType.MOVE);
    }

    @FXML
    private void onSupportButtonClick() {
        orderController.setOrderType(OrderType.SUPPORT);
    }

    @FXML
    private void onHoldButtonClick() {
        orderController.setOrderType(OrderType.HOLD);
        orderController.processClick(null);  // немедленно создать приказ Hold
    }

    private SVGImage loadSvg() {
        try {
            String svgUrl = getClass().getResource("/maps/" + mapSvgName).toExternalForm();
            return SVGLoader.load(svgUrl);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось загрузить SVG карты: " + mapSvgName, e);
        }
    }
}