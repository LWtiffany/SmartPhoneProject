package hku.cs.hkutopia.model;

import java.util.List;

public class ProductItem {
    private int id;
    private String name;
    private double price;
    private List<Integer> images;

    public ProductItem(int id, String name, double price, List<Integer> images) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.images = images;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public List<Integer> getImages() {
        return images;
    }
}