package com.diplomacy.desktop.controllers;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.girod.javafx.svgimage.SVGImage;

import com.diplomacy.logic.orders.utils.OrderType;
import com.diplomacy.logic.units.Army;
import com.diplomacy.logic.units.Unit;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

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

    private final double mapWidth = 610;
    private final double mapHeight = 560;

    private double containerWidth;
    private double containerHeight;

    public MapView(Node svgContent, Set<String> provinceIds,
                   Map<String, List<double[]>> unitCoordinates) {
        this.unitCoordinates = unitCoordinates;
        Node background = (svgContent != null) ? svgContent : new Group();
        mapGroup.getChildren().addAll(background, highlightPane, unitPane, arrowGroup);

        highlightPane.setMouseTransparent(true);
        arrowGroup.setMouseTransparent(true);
        unitPane.setPickOnBounds(false);

        mapGroup.getTransforms().addAll(translateTransform, scaleTransform);

        extractProvinceNodes(svgContent, provinceIds);
        computeProvinceCenters();
    }

    public void setContainerSize(double width, double height) {
        this.containerWidth = width;
        this.containerHeight = height;
        clampTranslation();
    }

    public void fitToContainer(double containerWidth, double containerHeight) {
        if (containerWidth <= 0 || containerHeight <= 0 || mapWidth <= 0 || mapHeight <= 0) {
            return;
        }

        double scaleX = containerWidth / mapWidth;
        double scaleY = containerHeight / mapHeight;
        double scale = Math.min(scaleX, scaleY);

        scaleTransform.setX(scale);
        scaleTransform.setY(scale);

        double scaledMapWidth = mapWidth * scale;
        double scaledMapHeight = mapHeight * scale;

        double transX = (containerWidth - scaledMapWidth) / 2;
        double transY = (containerHeight - scaledMapHeight) / 2;

        translateTransform.setX(transX);
        translateTransform.setY(transY);
    }
    public void clampTranslation() {
        if (containerWidth <= 0 || containerHeight <= 0) return;

        double scale = scaleTransform.getX();
        double scaledMapW = mapWidth * scale;
        double scaledMapH = mapHeight * scale;

        double currentX = (containerWidth - scaledMapW) / 2;
        double currentY = (containerHeight - scaledMapH) / 2;

        translateTransform.setX(currentX);
        translateTransform.setY(currentY);
    }

    private void extractProvinceNodes(Node svgRoot, Set<String> provinceIds) {
        if (svgRoot == null) return;

        if (svgRoot instanceof SVGImage) {
            SVGImage svgImage = (SVGImage) svgRoot;
            for (String id : provinceIds) {
                Node provinceNode = svgImage.getNode(id);
                if (provinceNode != null) provinceNodes.put(id, provinceNode);
                Node scNode = svgImage.getNode("sc_" + id);
                if (scNode != null) supplyCenterNodes.put(id, scNode);
            }
        } else {
            for (String id : provinceIds) {
                Node provinceNode = svgRoot.lookup("#" + id);
                if (provinceNode != null) provinceNodes.put(id, provinceNode);
                Node scNode = svgRoot.lookup("#sc_" + id);
                if (scNode != null) supplyCenterNodes.put(id, scNode);
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
        if (icon == null) return;

        ImageView iv = new ImageView(icon);
        iv.setUserData(unit);
        iv.setX(pos[0] - icon.getWidth() / 2);
        iv.setY(pos[1] - icon.getHeight() / 2);
        iv.setEffect(new DropShadow(10, Color.BLACK));
        iv.setOnMouseClicked(e -> {
            e.consume();
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

    public void drawOrderArrow(String fromProvinceId, String toProvinceId, OrderType type) {
        double[] from = provinceCenters.get(fromProvinceId);
        double[] to = provinceCenters.get(toProvinceId);
        if (from == null || to == null) return;

        Color color = switch (type) {
            case MOVE       -> Color.DARKRED;
            case SUPPORT    -> Color.DARKGREEN;
            case CONVOY     -> Color.DARKBLUE;
            case BECONVOYED -> Color.DODGERBLUE;
            case RETREAT    -> Color.ORANGE;
            default         -> Color.GRAY;
        };

        boolean dashed = (type == OrderType.SUPPORT || type == OrderType.BECONVOYED);

        drawArrow(from[0], from[1], to[0], to[1], color, dashed);
    }

    public void drawHoldMarker(String provinceId) {
        double[] center = provinceCenters.get(provinceId);
        if (center == null) return;

        Circle marker = new Circle(center[0], center[1], 10);
        marker.setFill(Color.TRANSPARENT);
        marker.setStroke(Color.GOLD);
        marker.setStrokeWidth(2.5);
        marker.getStrokeDashArray().addAll(6.0, 3.0);
        arrowGroup.getChildren().add(marker);
    }

    public void drawConvoyMarker(String provinceId) {
        double[] center = provinceCenters.get(provinceId);
        if (center == null) return;

        Circle marker = new Circle(center[0], center[1], 14);
        marker.setFill(Color.TRANSPARENT);
        marker.setStroke(Color.DARKBLUE);
        marker.setStrokeWidth(2.5);
        arrowGroup.getChildren().add(marker);
    }

    public void drawConvoyLink(String fleetProvinceId, String armyProvinceId) {
        double[] fleetPos = provinceCenters.get(fleetProvinceId);
        double[] armyPos = provinceCenters.get(armyProvinceId);
        if (fleetPos == null || armyPos == null) return;

        Line link = new Line(fleetPos[0], fleetPos[1], armyPos[0], armyPos[1]);
        link.setStroke(Color.DARKBLUE);
        link.setStrokeWidth(2);
        link.getStrokeDashArray().addAll(4.0, 4.0);
        arrowGroup.getChildren().add(link);
    }

    public void drawDieMarker(String provinceId) {
        double[] center = provinceCenters.get(provinceId);
        if (center == null) return;

        double x = center[0];
        double y = center[1];
        double size = 8;
        Line l1 = new Line(x - size, y - size, x + size, y + size);
        l1.setStroke(Color.RED);
        l1.setStrokeWidth(3);
        Line l2 = new Line(x + size, y - size, x - size, y + size);
        l2.setStroke(Color.RED);
        l2.setStrokeWidth(3);
        arrowGroup.getChildren().addAll(l1, l2);
    }

    public void drawDismissMarker(String provinceId) {
        double[] center = provinceCenters.get(provinceId);
        if (center == null) return;

        Circle marker = new Circle(center[0], center[1], 10);
        marker.setFill(Color.TRANSPARENT);
        marker.setStroke(Color.RED);
        marker.setStrokeWidth(2.5);
        arrowGroup.getChildren().add(marker);
    }

    public void drawSpawnMarker(String provinceId) {
        double[] center = provinceCenters.get(provinceId);
        if (center == null) return;

        double x = center[0];
        double y = center[1];
        double size = 8;
        Line h = new Line(x - size, y, x + size, y);
        h.setStroke(Color.GREEN);
        h.setStrokeWidth(3);
        Line v = new Line(x, y - size, x, y + size);
        v.setStroke(Color.GREEN);
        v.setStrokeWidth(3);
        arrowGroup.getChildren().addAll(h, v);
    }

    private void drawArrow(double startX, double startY,
                            double endX, double endY,
                            Color color, boolean dashed) {
        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(color);
        line.setStrokeWidth(3);
        if (dashed) {
            line.getStrokeDashArray().addAll(8.0, 4.0);
        }

        double arrowSize = 12.0;
        double angle = Math.atan2(endY - startY, endX - startX);
        double tipX = endX;
        double tipY = endY;
        double leftX = tipX - arrowSize * Math.cos(angle - Math.toRadians(30));
        double leftY = tipY - arrowSize * Math.sin(angle - Math.toRadians(30));
        double rightX = tipX - arrowSize * Math.cos(angle + Math.toRadians(30));
        double rightY = tipY - arrowSize * Math.sin(angle + Math.toRadians(30));

        Polygon arrowHead = new Polygon(tipX, tipY, leftX, leftY, rightX, rightY);
        arrowHead.setFill(color);

        arrowGroup.getChildren().addAll(line, arrowHead);
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

    // public void applyZoom(double factor, double pivotX, double pivotY) {
    //     double newScale = scaleTransform.getX() * factor;
    //     newScale = Math.min(3.0, Math.max(0.5, newScale));
    //     double realFactor = newScale / scaleTransform.getX();

    //     double dx = (pivotX - translateTransform.getX()) * (1 - realFactor);
    //     double dy = (pivotY - translateTransform.getY()) * (1 - realFactor);
    //     translateTransform.setX(translateTransform.getX() + dx);
    //     translateTransform.setY(translateTransform.getY() + dy);

    //     scaleTransform.setX(newScale);
    //     scaleTransform.setY(newScale);
    //     clampTranslation();
    // }

    // public void applyPan(double dx, double dy) {
    //     translateTransform.setX(translateTransform.getX() + dx);
    //     translateTransform.setY(translateTransform.getY() + dy);
    // }

    private Image getUnitIcon(Unit unit) {
        String type = unit instanceof Army ? "army" : "fleet";
        String path = "/images/icons/" + type + ".png";
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is == null) {
                System.err.println("Icon not found: " + path);
                return null;
            }

            return new Image(is, 24, 24, false, true);
        } catch (Exception e) {
            System.err.println("Failed to load icon: " + path);
            return null;
        }
    }

    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));
    }

    public double getMapWidth() {
        return mapWidth;
    }

    public double getMapHeight() {
        return mapHeight;
    }
}