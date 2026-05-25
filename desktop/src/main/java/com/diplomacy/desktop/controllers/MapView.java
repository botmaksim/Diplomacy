package com.diplomacy.desktop.controllers;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.girod.javafx.svgimage.SVGImage;

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

    // Размеры контейнера (обновляются извне)
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

    // ================== Подгонка под контейнер ==================

    /**
     * Устанавливает размер контейнера и автоматически центрирует карту.
     */
    public void setContainerSize(double width, double height) {
        this.containerWidth = width;
        this.containerHeight = height;
        clampTranslation();
    }

    public void fitToContainer(double containerWidth, double containerHeight) {
    // Проверка на случай, если размеры еще не определены
    if (containerWidth <= 0 || containerHeight <= 0 || mapWidth <= 0 || mapHeight <= 0) {
        return;
    }

    // Рассчитываем коэффициенты масштабирования для заполнения по ширине и по высоте
    double scaleX = containerWidth / mapWidth;
    double scaleY = containerHeight / mapHeight;

    // Выбираем БОЛЬШИЙ из коэффициентов. Это гарантирует, что карта
    // заполнит контейнер по одной оси и выйдет за пределы по другой.
    double scale = Math.max(scaleX, scaleY);

    // Применяем вычисленный масштаб
    scaleTransform.setX(scale);
    scaleTransform.setY(scale);

    // Вычисляем реальные размеры карты после масштабирования
    double scaledMapWidth = mapWidth * scale;
    double scaledMapHeight = mapHeight * scale;

    // Вычисляем смещение, необходимое для центрирования увеличенной карты.
    // Смещение будет отрицательным или нулевым, сдвигая карту так,
    // чтобы ее центр совпал с центром контейнера.
    double transX = (containerWidth - scaledMapWidth) / 2;
    double transY = (containerHeight - scaledMapHeight) / 2;

    // Применяем вычисленное смещение
    translateTransform.setX(transX);
    translateTransform.setY(transY);
}
    /**
     * Ограничивает перемещение так, чтобы карта всегда полностью заполняла контейнер
     * (при статическом режиме просто центрирует).
     */
    public void clampTranslation() {
        if (containerWidth <= 0 || containerHeight <= 0) return;

        double scale = scaleTransform.getX();
        double scaledMapW = mapWidth * scale;
        double scaledMapH = mapHeight * scale;

        // Центрируем карту независимо от размера (статический режим)
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
        // 1. Линия
        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(Color.DARKRED);
        line.setStrokeWidth(3);
        
        // 2. Наконечник (треугольник)
        double arrowSize = 12.0;          // длина наконечника
        double arrowWidth = 8.0;          // ширина основания
        
        // Вычисляем угол линии
        double angle = Math.atan2(endY - startY, endX - startX);
        
        // Координаты основания треугольника (на конце линии)
        double baseX = endX;
        double baseY = endY;
        
        // Вершина треугольника (остриё) – можно немного отодвинуть от конца, но обычно остриё на самом конце
        double tipX = endX;
        double tipY = endY;
        
        // Левая и правая точки основания
        double leftX = baseX - arrowSize * Math.cos(angle - Math.toRadians(30));
        double leftY = baseY - arrowSize * Math.sin(angle - Math.toRadians(30));
        double rightX = baseX - arrowSize * Math.cos(angle + Math.toRadians(30));
        double rightY = baseY - arrowSize * Math.sin(angle + Math.toRadians(30));
        
        Polygon arrowHead = new Polygon(
            tipX, tipY,
            leftX, leftY,
            rightX, rightY
        );
        arrowHead.setFill(Color.DARKRED);
        
        // 3. Добавляем в группу
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
    //     clampTranslation();   // ← важно
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