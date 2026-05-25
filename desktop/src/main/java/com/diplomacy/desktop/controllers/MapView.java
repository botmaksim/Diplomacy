package com.diplomacy.desktop.controllers;

import com.diplomacy.logic.units.Army;
import com.diplomacy.logic.units.Unit;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import org.girod.javafx.svgimage.SVGImage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapView {

    private final Group mapGroup = new Group();
    private final Pane highlightPane = new Pane();
    private final Pane unitPane = new Pane();
    private final Group arrowGroup = new Group();

    private final Scale scaleTransform = new Scale(1, 1);
    private final Translate translateTransform = new Translate(0, 0);

    private final Map<String, Node> provinceNodes = new HashMap<>();
    private final Map<String, double[]> provinceCenters = new HashMap<>();
    private final Map<String, List<double[]>> unitCoordinates;
    private final Map<String, Node> supplyCenterNodes = new HashMap<>();
    private final Map<Unit, ImageView> unitViews = new HashMap<>();

    public MapView(SVGImage svgContent, Set<String> provinceIds,
                   Map<String, List<double[]>> unitCoordinates) {
        this.unitCoordinates = unitCoordinates;
        mapGroup.getChildren().addAll(svgContent, highlightPane, unitPane, arrowGroup);

        highlightPane.setMouseTransparent(true);
        arrowGroup.setMouseTransparent(true);
        unitPane.setPickOnBounds(false);

        mapGroup.getTransforms().addAll(translateTransform, scaleTransform);

        extractProvinceNodes(svgContent, provinceIds);
        computeProvinceCenters();
    }

    private void extractProvinceNodes(SVGImage svgRoot, Set<String> provinceIds) {
        for (String id : provinceIds) {
            // Используем встроенный метод библиотеки для поиска по ID
            Node provinceNode = svgRoot.getNode(id);
            if (provinceNode != null) {
                provinceNodes.put(id, provinceNode);
            }
            Node scNode = svgRoot.getNode("sc_" + id);
            if (scNode != null) {
                supplyCenterNodes.put(id, scNode);
            }
        }
    }

    private void computeProvinceCenters() {
        for (var entry : unitCoordinates.entrySet()) {
            String id = entry.getKey();
            List<double[]> locs = entry.getValue();
            if (locs != null && !locs.isEmpty()) {
                provinceCenters.put(id, locs.get(0));
            } else if (provinceNodes.containsKey(id)) {
                Node node = provinceNodes.get(id);
                var bounds = node.getBoundsInParent();
                provinceCenters.put(id, new double[]{
                    bounds.getCenterX(), bounds.getCenterY()
                });
            }
        }
    }

    public ImageView getUnitView(Unit unit) {
        return unitViews.get(unit);
    }

    public Group getView() {
        return mapGroup;
    }

    public double getTranslateX() {
        return translateTransform.getX();
    }

    public double getTranslateY() {
        return translateTransform.getY();
    }

    public void addUnit(Unit unit, String provinceId) {
        List<double[]> locs = unitCoordinates.get(provinceId);
        if (locs == null || locs.isEmpty()) return;
        double[] pos = locs.get(0);

        Image icon = getUnitIcon(unit);
        if (icon == null) return;  // иконка не найдена – не добавляем юнит

        ImageView iv = new ImageView(icon);
        iv.setUserData(unit);
        iv.setX(pos[0] - icon.getWidth() / 2);
        iv.setY(pos[1] - icon.getHeight() / 2);
        iv.setEffect(new DropShadow(10, Color.BLACK));
        iv.setOnMouseClicked(e -> {
            e.consume();  // дальнейшая обработка клика делегируется контроллеру
        });
        unitPane.getChildren().add(iv);
        unitViews.put(unit, iv);
    }

    public void removeUnit(Unit unit) {
        ImageView iv = unitViews.remove(unit);
        if (iv != null) unitPane.getChildren().remove(iv);
    }

    public void highlightSelectedUnit(Unit unit, boolean selected) {
        ImageView iv = unitViews.get(unit);
        if (iv != null) {
            iv.setEffect(selected ? new DropShadow(20, Color.GOLD) : new DropShadow(10, Color.BLACK));
        }
    }

    public void highlightProvince(String provinceId, Color color) {
        Node node = provinceNodes.get(provinceId);
        if (node != null) {
            node.setStyle(String.format("-fx-fill: %s; -fx-opacity: 0.4;", toHex(color)));
        }
    }

    public void clearHighlights() {
        provinceNodes.values().forEach(n -> n.setStyle(""));
    }

    public void drawArrow(String fromProvinceId, String toProvinceId) {
        double[] from = provinceCenters.get(fromProvinceId);
        double[] to = provinceCenters.get(toProvinceId);
        if (from == null || to == null) return;
        drawArrow(from[0], from[1], to[0], to[1]);
    }

    public void drawArrow(double startX, double startY, double endX, double endY) {
        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(Color.DARKRED);
        line.setStrokeWidth(3);
        arrowGroup.getChildren().add(line);
    }

    public void clearArrows() {
        arrowGroup.getChildren().clear();
    }

    public void setSupplyCenterColor(String provinceId, Color color) {
        Node sc = supplyCenterNodes.get(provinceId);
        if (sc != null) {
            sc.setStyle("-fx-fill: " + toHex(color) + ";");
        }
    }

    public void applyZoom(double factor, double pivotX, double pivotY) {
        double newScale = scaleTransform.getX() * factor;
        newScale = Math.min(3.0, Math.max(0.5, newScale));
        double realFactor = newScale / scaleTransform.getX();

        double dx = (pivotX - translateTransform.getX()) * (1 - realFactor);
        double dy = (pivotY - translateTransform.getY()) * (1 - realFactor);
        translateTransform.setX(translateTransform.getX() + dx);
        translateTransform.setY(translateTransform.getY() + dy);

        scaleTransform.setX(newScale);
        scaleTransform.setY(newScale);
    }

    public void applyPan(double dx, double dy) {
        translateTransform.setX(translateTransform.getX() + dx);
        translateTransform.setY(translateTransform.getY() + dy);
    }

    private Image getUnitIcon(Unit unit) {
        String type = unit instanceof Army ? "army" : "fleet";
        String path = "/images/icons/" + type + ".png";
        try {
            return new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            System.err.println("Missing icon: " + path);
            return null;
        }
    }

    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));
    }
}