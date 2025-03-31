package com.example.restaurantsearch;

import java.io.Serializable;

//IDを持って詳細データを検索しようと思ったら、最初からAPIが全てのデータを書き込んで、レストランの一覧を提供してくれます。
//2回APIを使うより、一覧を出力する時点で全てのデータを保存しといて、オブジェクトそのもの詳細画面に渡します。そのため、本クラスが implements Serializable　の形にしました。
//参考: https://docs.oracle.com/javase/8/docs/api/java/io/Serializable.html
//参考２：https://www.repeato.app/how-to-send-an-object-from-one-android-activity-to-another-using-intents/
public class Shop implements Serializable {

    private String id, name, genre, address, stationName, latitude, longitude, openingHours, estimatedPrice, access, imageUrl, websiteUrl, distanceFromSearchCoordinates;
    //次の項目がbooleanのはずですが、APIから「あり、なし」の形だけで来るのではなく、特別なメッセージで送信される可能性もあるため、全部String型にします。詳細画面で、もらったまま表示することにします。
    private String hasParking, cardAvailability, smokingSeats, isBarrierFree, hasLunch, hasTatami, acceptsPets, hasKaraoke;

    //distanceFromSearchCoordinatesだけが含まれていません。別の時点でsetメソッドを呼びます。
    public Shop(String id, String name, String genre, String address, String stationName, String latitude, String longitude, String openingHours, String estimatedPrice, String access, String imageUrl, String websiteUrl, String hasParking, String cardAvailability, String smokingSeats, String isBarrierFree, String hasLunch, String hasTatami, String acceptsPets, String hasKaraoke) {
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
        this.hasParking = hasParking;
        this.cardAvailability = cardAvailability;
        this.smokingSeats = smokingSeats;
        this.isBarrierFree = isBarrierFree;
        this.hasLunch = hasLunch;
        this.hasTatami = hasTatami;
        this.acceptsPets = acceptsPets;
        this.hasKaraoke = hasKaraoke;
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
                ", estimatedPrice='" + estimatedPrice + '\'' +
                ", access='" + access + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", websiteUrl='" + websiteUrl + '\'' +
                ", hasParking='" + hasParking + '\'' +
                ", cardAvailability='" + cardAvailability + '\'' +
                ", smokingSeats='" + smokingSeats + '\'' +
                ", isBarrierFree='" + isBarrierFree + '\'' +
                ", hasLunch='" + hasLunch + '\'' +
                ", hasTatami='" + hasTatami + '\'' +
                ", acceptsPets='" + acceptsPets + '\'' +
                ", hasKaraoke='" + hasKaraoke + '\'' +
                ", distanceFromSearchCoordinates='" + distanceFromSearchCoordinates + '\'' +
                '}';
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

    public String getHasParking() {
        return hasParking;
    }

    public void setHasParking(String hasParking) {
        this.hasParking = hasParking;
    }

    public String getCardAvailability() {
        return cardAvailability;
    }

    public void setCardAvailability(String cardAvailability) {
        this.cardAvailability = cardAvailability;
    }

    public String getSmokingSeats() {
        return smokingSeats;
    }

    public void setSmokingSeats(String smokingSeats) {
        this.smokingSeats = smokingSeats;
    }

    public String getIsBarrierFree() {
        return isBarrierFree;
    }

    public void setIsBarrierFree(String isBarrierFree) {
        this.isBarrierFree = isBarrierFree;
    }

    public String getHasLunch() {
        return hasLunch;
    }

    public void setHasLunch(String hasLunch) {
        this.hasLunch = hasLunch;
    }

    public String getHasTatami() {
        return hasTatami;
    }

    public void setHasTatami(String hasTatami) {
        this.hasTatami = hasTatami;
    }

    public String getAcceptsPets() {
        return acceptsPets;
    }

    public void setAcceptsPets(String acceptsPets) {
        this.acceptsPets = acceptsPets;
    }

    public String getHasKaraoke() {
        return hasKaraoke;
    }

    public void setHasKaraoke(String hasKaraoke) {
        this.hasKaraoke = hasKaraoke;
    }
}
