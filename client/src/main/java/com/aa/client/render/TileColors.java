package com.aa.client.render;

import javafx.scene.paint.Color;
import java.util.Map;

public final class TileColors {

    private static final Map<Integer, Color> COLORS = Map.ofEntries(
        Map.entry(0, Color.TRANSPARENT),
        Map.entry(1, Color.rgb(33, 38, 45)),
        Map.entry(2, Color.rgb(48, 54, 61)),
        Map.entry(3, Color.rgb(30, 35, 42)),
        Map.entry(4, Color.rgb(28, 33, 40)),
        Map.entry(5, Color.rgb(45, 51, 59)),
        Map.entry(6, Color.rgb(22, 27, 34)),
        Map.entry(7, Color.rgb(38, 44, 52)),
        Map.entry(8, Color.rgb(88, 166, 255, 0.3)),
        Map.entry(9, Color.rgb(46, 160, 67, 0.3)),
        Map.entry(10, Color.rgb(210, 153, 34, 0.3))
    );

    private TileColors() {}

    public static Color getColor(int tileId) {
        return COLORS.getOrDefault(tileId, Color.rgb(48, 54, 61));
    }
}
