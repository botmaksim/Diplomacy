package com.diplomacy.desktop.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.girod.javafx.svgimage.SVGImage;
import org.girod.javafx.svgimage.SVGLoader;

import com.diplomacy.logic.gameControllingUnits.GameMaster;
import com.diplomacy.logic.gameControllingUnits.utils.GameInitializer;
import com.diplomacy.logic.gameControllingUnits.utils.MapLoader;
import com.diplomacy.logic.geography.advanced.GameMap;
import com.diplomacy.logic.geography.basic.Province;
import com.diplomacy.logic.orders.utils.OrderType;
import com.diplomacy.logic.player.Password;
import com.diplomacy.logic.player.Player;
import com.diplomacy.logic.turnClassificator.PhaseType;
import com.diplomacy.logic.turnClassificator.Season;
import com.diplomacy.logic.turnClassificator.TurnClassificator;
import com.diplomacy.logic.units.Unit;

import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public class GameViewController {

    @FXML private StackPane mapContainer;
    @FXML private ListView<String> ordersListView;
    @FXML private ToggleGroup orderTypeGroup;
    @FXML private TextArea logTextArea;
    @FXML private Label turnLabel;
    @FXML private Label phaseLabel;
    @FXML private Label currentPlayerLabel;
    @FXML private Label playerInfoLabel;
    @FXML private Button loginButton;
    @FXML private Button spectatorButton;
    @FXML private Button confirmOrdersButton;
    @FXML private Button nextPhaseButton;
    @FXML private VBox orderButtonsContainer;

    private GameMaster gameMaster;
    private MapView mapView;
    private MapInputHandler inputHandler;
    private OrderController orderController;
    private OrderLedger orderLedger;
    private PlayerSession playerSession;

    private final String mapSvgName = "map_europe_1900.svg";
    private final String mapMaskName = "map_mask_europe_1900.png";

    @FXML
    public void initialize() {
        GameMap gameMap = MapLoader.load();
        System.out.println("Map loaded.");

        List<Player> players = new ArrayList<>();
        players.add(new Player(gameMap.getCountry("France"), new Password("12345")));
        players.add(new Player(gameMap.getCountry("Germany"), new Password("12345")));
        players.add(new Player(gameMap.getCountry("England"), new Password("12345")));
        players.add(new Player(gameMap.getCountry("Italy"), new Password("12345")));
        players.add(new Player(gameMap.getCountry("Austria-Hungary"), new Password("12345")));
        players.add(new Player(gameMap.getCountry("Russia"), new Password("12345")));
        players.add(new Player(gameMap.getCountry("Turkey"), new Password("12345")));
        System.out.println("Players created.");

        gameMaster = new GameMaster(players, gameMap, null);
        GameInitializer.initializeStartPositions(gameMaster);
        System.out.println("GameMaster initialized.");

        Map<String, Map<String, Object>> rawUi = MapLoader.loadUiData();
        Image mask = new Image(getClass().getResourceAsStream("/maps/" + mapMaskName));
        Map<Integer, Province> colorToProvince = buildColorMapping(rawUi, gameMap);
        Map<String, List<double[]>> unitCoordinates = extractUnitCoords(rawUi);
        Set<String> provinceIds = rawUi.keySet();

        Node svgRoot = loadSvg();
        mapView = new MapView(svgRoot, provinceIds, unitCoordinates);
        mapContainer.getChildren().add(mapView.getView());

        Rectangle clipRect = new Rectangle();
        clipRect.widthProperty().bind(mapContainer.widthProperty());
        clipRect.heightProperty().bind(mapContainer.heightProperty());
        mapContainer.setClip(clipRect);

        mapContainer.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.getWidth() > 0 && newVal.getHeight() > 0) {
                mapView.fitToContainer(newVal.getWidth(), newVal.getHeight());
            }
        });

        orderLedger = new OrderLedger(gameMaster, mapView, ordersListView);
        playerSession = new PlayerSession(gameMaster, orderLedger, currentPlayerLabel,
            playerInfoLabel, loginButton, confirmOrdersButton, orderTypeGroup);
        orderController = new OrderController(gameMaster, mapView,
            logTextArea, orderLedger, playerSession);
        inputHandler = new MapInputHandler(mapView, mask, colorToProvince, orderController,
            mapView.getMapWidth(), mapView.getMapHeight());

        renderInitialUnits();

        setupOrderListDelete();

        playerSession.enterSpectatorMode();
        refreshOrderTypeButtonStates();
        updatePhaseDisplay();

        orderTypeGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            Player cp = playerSession.getCurrentPlayer();
            if (cp != null && orderLedger.isConfirmed(cp)) {
                for (Toggle t : orderTypeGroup.getToggles()) {
                    if ("None".equals(((RadioButton) t).getText())) {
                        orderTypeGroup.selectToggle(t);
                        return;
                    }
                }
            }
            if (newToggle == null) {
                orderController.setOrderType(null);
                return;
            }
            String text = ((RadioButton) newToggle).getText();
            orderController.setOrderType(parseOrderType(text));
        });

        System.out.println("initialize END");
    }

    private void setupOrderListDelete() {
        ordersListView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) return;
            if (event.getClickCount() == 2) {
                int index = ordersListView.getSelectionModel().getSelectedIndex();
                if (index >= 0) {
                    orderLedger.removeOrder(playerSession.getCurrentPlayer(), index);
                }
            }
        });
    }

    @FXML
    private void onSpectatorClick() {
        playerSession.enterSpectatorMode();
        refreshOrderTypeButtonStates();
    }

    @FXML
    private void onLoginClick() {
        playerSession.showLoginDialog(mapContainer.getScene().getWindow());
        refreshOrderTypeButtonStates();
    }

    private OrderType parseOrderType(String text) {
        return switch (text) {
            case "None" -> null;
            case "BeConvoyed" -> OrderType.BECONVOYED;
            default -> OrderType.valueOf(text.toUpperCase());
        };
    }

    private Node loadSvg() {
        try {
            java.net.URL svgUrl = getClass().getResource("/maps/" + mapSvgName);
            if (svgUrl == null) {
                throw new java.io.FileNotFoundException("Map file not found in resources.");
            }
            SVGImage image = SVGLoader.load(svgUrl);
            if (image == null) throw new Exception("SVG is null");
            return image;
        } catch (Exception e) {
            System.err.println("Failed to load SVG: " + e.getMessage());
            e.printStackTrace();
            Image img = new Image(getClass().getResourceAsStream("/maps/" + mapMaskName));
            ImageView iv = new ImageView(img);
            iv.setPreserveRatio(true);
            return new Group(iv);
        }
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
    private void onNextPhaseClick() {
        advancePhase();
        orderLedger.clearAll();
        orderController.clearSelection();
        playerSession.updateConfirmButton();
        orderLedger.rebuild(playerSession.getCurrentPlayer());
        selectNoneRadioButton();
        updatePhaseDisplay();
        refreshOrderTypeButtonStates();
    }

    private void advancePhase() {
        gameMaster.getTurn().nextTurn();
        gameMaster.updatePhaseObject();
    }

    private void updatePhaseDisplay() {
        TurnClassificator turn = gameMaster.getTurn();
        String seasonStr = turn.getSeason() == Season.SPRING ? "Spring" : "Fall";
        turnLabel.setText(seasonStr + " " + turn.getYear());

        String phaseText = switch (turn.getPhase()) {
            case MOVEMENT -> "Order Phase";
            case RETREAT -> "Retreat Phase";
            case SPAWN -> "Spawn Phase";
        };
        phaseLabel.setText(phaseText);
    }

    private void refreshOrderTypeButtonStates() {
        orderButtonsContainer.getChildren().clear();

        Player player = playerSession.getCurrentPlayer();
        if (player == null) {
            return;
        }

        PhaseType phase = gameMaster.getTurn().getPhase();
        List<List<String>> rows = switch (phase) {
            case MOVEMENT -> List.of(
                List.of("None", "Hold", "Move"),
                List.of("Support", "Convoy", "BeConvoyed")
            );
            case RETREAT -> List.of(
                List.of("None", "Retreat", "Die")
            );
            case SPAWN -> List.of(
                List.of("None", "Spawn", "Dismiss")
            );
        };

        for (List<String> row : rows) {
            HBox hbox = new HBox(5);
            for (String text : row) {
                RadioButton rb = new RadioButton(text);
                rb.setToggleGroup(orderTypeGroup);
                if ("None".equals(text)) {
                    rb.setSelected(true);
                }
                hbox.getChildren().add(rb);
            }
            orderButtonsContainer.getChildren().add(hbox);
        }
    }

    private void selectNoneRadioButton() {
        for (Toggle t : orderTypeGroup.getToggles()) {
            if ("None".equals(((RadioButton) t).getText())) {
                orderTypeGroup.selectToggle(t);
                break;
            }
        }
    }

    @FXML private void onConfirmOrdersClick() {
        Player currentPlayer = playerSession.getCurrentPlayer();
        if (currentPlayer == null) return;
        if (orderLedger.isConfirmed(currentPlayer)) {
            orderLedger.cancelConfirmation(currentPlayer);
        } else {
            orderLedger.confirm(currentPlayer);
        }
        orderController.clearSelection();
        playerSession.updateConfirmButton();
        for (Toggle t : orderTypeGroup.getToggles()) {
            if ("None".equals(((RadioButton) t).getText())) {
                orderTypeGroup.selectToggle(t);
                break;
            }
        }
    }
}
