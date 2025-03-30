package com.example.restaurantsearch;

public class Shop {

    private String id, name, genre, address, stationName, latitude, longitude, openingHours, estimatedPrice, access, imageUrl, websiteUrl, distanceFromSearchCoordinates;

    public Shop(String id, String name, String genre, String address, String stationName, String latitude, String longitude, String openingHours, String estimatedPrice, String access, String imageUrl, String websiteUrl) {
        this.id = id;
        this.name = name;
        this.genre = genre;
        this.address = address;
        this.stationName = stationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.openingHours = openingHours;
        this.estimatedPrice = estimatedPrice;
        this.access = access;
        this.imageUrl = imageUrl;
        this.websiteUrl = websiteUrl;
    }

    @Override
    public String toString() {
        return "Shop{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", genre='" + genre + '\'' +
                ", address='" + address + '\'' +
                ", stationName='" + stationName + '\'' +
                ", latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", openingHours='" + openingHours + '\'' +
                ", estimatedCost='" + estimatedPrice + '\'' +
                ", access='" + access + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", websiteUrl='" + websiteUrl + '\'' +
                "}\n";
    }

    public String getDistanceFromSearchCoordinates() {
        return distanceFromSearchCoordinates;
    }

    //Shopオブジェクトの作成と距離の計算が別々で行いますのでコンストラクタには入れません。setメソッドで設定します。
    public void setDistanceFromSearchCoordinates(String distanceFromSearchCoordinates) {
        this.distanceFromSearchCoordinates = distanceFromSearchCoordinates;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public String getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(String estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getWebsiteUrl() {
        return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
        this.websiteUrl = websiteUrl;
    }
}
