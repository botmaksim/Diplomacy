package com.diplomacy.desktop.controllers;

import java.util.Map;

import com.diplomacy.logic.geography.basic.Province;

import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class MapInputHandler {
    private final MapView mapView;
    private final Image maskImage;
    private final Map<Integer, Province> colorToProvince;
    private final OrderController orderController;

    // private double dragStartX, dragStartY;

    private final double svgWidth;
    private final double svgHeight;

    // Обновленный конструктор
    public MapInputHandler(MapView mapView, Image mask, Map<Integer, Province> mapping, OrderController orderCtrl, double svgWidth, double svgHeight) {
        this.mapView = mapView;
        this.maskImage = mask;
        this.colorToProvince = mapping;
        this.orderController = orderCtrl;
        this.svgWidth = svgWidth;    // Сохраняем размеры
        this.svgHeight = svgHeight;  // Сохраняем размеры
        setupHandlers();
    }

    private void setupHandlers() {
        Group mapGroup = mapView.getView();
        mapGroup.setOnMouseClicked(this::onMapClick);
        // mapGroup.setOnScroll(this::onScroll);
        // mapGroup.setOnMousePressed(this::onMousePressed);
        // mapGroup.setOnMouseDragged(this::onMouseDragged);
    }

    // private void onMousePressed(MouseEvent e) {
    //     if (e.getButton() == MouseButton.MIDDLE || e.isSecondaryButtonDown()) {
    //         dragStartX = e.getSceneX();
    //         dragStartY = e.getSceneY();
    //         e.consume();
    //     }
    // }

    // private void onMouseDragged(MouseEvent e) {
    //     if (e.getButton() == MouseButton.MIDDLE || e.isSecondaryButtonDown()) {
    //         double dx = e.getSceneX() - dragStartX;
    //         double dy = e.getSceneY() - dragStartY;
    //         mapView.applyPan(dx, dy);
    //         mapView.clampTranslation();   // ← ограничиваем смещение
    //         dragStartX = e.getSceneX();
    //         dragStartY = e.getSceneY();
    //         e.consume();
    //     }
    // }

    // private void onMapClick(MouseEvent e) {
    //     if (e.getButton() != MouseButton.PRIMARY) return;
    //     Point2D mapCoords = mapView.getView().sceneToLocal(e.getSceneX(), e.getSceneY());
    //     int argb = maskImage.getPixelReader().getArgb((int)mapCoords.getX(), (int)mapCoords.getY());
    //     Province province = colorToProvince.get(argb);
    //     if (province == null) 
    //         System.out.println("loh");
    //     else {
    //         System.out.println(province.getName());
    //         System.out.println((int)mapCoords.getX());
    //         System.out.println((int)mapCoords.getY());
    //     }
            
    //     orderController.processClick(province);
    //     e.consume();
    // }

    private void onMapClick(MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY) return;

        // 1. Получаем координаты клика в локальной системе SVG (как и раньше)
        Point2D svgCoords = mapView.getView().sceneToLocal(e.getSceneX(), e.getSceneY());
        double svgX = svgCoords.getX();
        double svgY = svgCoords.getY();

        // 2. Преобразуем координаты из системы SVG в систему PNG-маски
        double pngWidth = maskImage.getWidth();
        double pngHeight = maskImage.getHeight();

        int pngX = (int) ((svgX / this.svgWidth) * pngWidth);
        int pngY = (int) ((svgY / this.svgHeight) * pngHeight);

        // 3. Проверяем, что мы не вышли за границы маски
        if (pngX < 0 || pngY < 0 || pngX >= pngWidth || pngY >= pngHeight) {
            orderController.processClick(null); // Кликнули мимо
            e.consume();
            return;
        }

        // 4. Получаем цвет и провинцию из PNG-маски по НОВЫМ, правильным координатам
        int argb = maskImage.getPixelReader().getArgb(pngX, pngY);
        Province province = colorToProvince.get(argb);

        // Отладочная печать (можно оставить или удалить)
        System.out.println(String.format("SVG Coords: (%.1f, %.1f) -> PNG Coords: (%d, %d) -> Color: %s -> Province: %s",
                svgX, svgY, pngX, pngY, Integer.toHexString(argb), province != null ? province.getName() : "null"));

        orderController.processClick(province);
        e.consume();
    }

    // private void onScroll(ScrollEvent e) {
    //     double factor = (e.getDeltaY() > 0) ? 1.1 : 0.9;
    //     Point2D mouseInMap = mapView.getView().sceneToLocal(e.getSceneX(), e.getSceneY());
    //     mapView.applyZoom(factor, mouseInMap.getX(), mouseInMap.getY());
    //     e.consume();
    // }
} // // I HOPE THIS WORKS WELL