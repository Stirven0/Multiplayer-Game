package com.aa.shared.model;

/**
 * Vector 2D inmutable para posiciones, direcciones y velocidades.
 * Usar para cálculos geométricos simples.
 */
public record Vector2(double x, double y) {
    
    public Vector2 add(Vector2 other) {
        return new Vector2(this.x + other.x, this.y + other.y);
    }
    
    public Vector2 multiply(double scalar) {
        return new Vector2(this.x * scalar, this.y * scalar);
    }
    
    public double magnitude() {
        return Math.sqrt(x * x + y * y);
    }
    
    public Vector2 normalize() {
        double mag = magnitude();
        if (mag == 0) return new Vector2(0, 0);
        return new Vector2(x / mag, y / mag);
    }
    
    public double distanceTo(Vector2 other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
