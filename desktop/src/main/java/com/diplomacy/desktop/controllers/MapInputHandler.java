package com.diplomacy.desktop.controllers;

import com.diplomacy.logic.geography.basic.Province;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

import java.util.Map;

public class MapInputHandler {
    private final MapView mapView;
    private final Image maskImage;
    private final Map<Integer, Province> colorToProvince;
    private final OrderController orderController;

    private double dragStartX, dragStartY;

    public MapInputHandler(MapView mapView, Image mask, Map<Integer, Province> mapping, OrderController orderCtrl) {
        this.mapView = mapView;
        this.maskImage = mask;
        this.colorToProvince = mapping;
        this.orderController = orderCtrl;
        setupHandlers();
    }

    private void setupHandlers() {
        Group mapGroup = mapView.getView();
        mapGroup.setOnMouseClicked(this::onMapClick);
        mapGroup.setOnScroll(this::onScroll);
        mapGroup.setOnMousePressed(this::onMousePressed);
        mapGroup.setOnMouseDragged(this::onMouseDragged);
    }

    private void onMousePressed(MouseEvent e) {
        if (e.getButton() == MouseButton.MIDDLE || e.isSecondaryButtonDown()) {
            dragStartX = e.getSceneX();
            dragStartY = e.getSceneY();
            e.consume();
        }
    }

    private void onMouseDragged(MouseEvent e) {
        if (e.getButton() == MouseButton.MIDDLE || e.isSecondaryButtonDown()) {
            double dx = e.getSceneX() - dragStartX;
            double dy = e.getSceneY() - dragStartY;
            mapView.applyPan(dx, dy);
            dragStartX = e.getSceneX();
            dragStartY = e.getSceneY();
            e.consume();
        }
    }

    private void onMapClick(MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY) return;
        Point2D mapCoords = mapView.getView().sceneToLocal(e.getSceneX(), e.getSceneY());
        int argb = maskImage.getPixelReader().getArgb((int)mapCoords.getX(), (int)mapCoords.getY());
        Province province = colorToProvince.get(argb);
        orderController.processClick(province);
        e.consume();
    }

    private void onScroll(ScrollEvent e) {
        double factor = (e.getDeltaY() > 0) ? 1.1 : 0.9;
        Point2D mouseInMap = mapView.getView().sceneToLocal(e.getSceneX(), e.getSceneY());
        mapView.applyZoom(factor, mouseInMap.getX(), mouseInMap.getY());
        e.consume();
    }
} // I HOPE THIS WORKS WELL