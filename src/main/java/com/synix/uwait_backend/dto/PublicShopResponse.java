package com.synix.uwait_backend.dto;

import com.synix.uwait_backend.model.Shop;
import com.synix.uwait_backend.model.ShopStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class PublicShopResponse {

    private String publicCode;

    private String shopName;
    private String category;
    private String shopLogoUrl;

    private String whatsappNumber;
    private String shortBio;
    private String instagramId;

    private ShopStatus status;
    private boolean active;

    private String area;
    private String fullAddress;
    private String city;
    private String pinCode;
    private String OwnerName;
    private String ShopPlanStatus;
    private String location;

    public static PublicShopResponse from(Shop shop) {
        PublicShopResponse r = new PublicShopResponse();
        r.setOwnerName(shop.getOwner() != null ? shop.getOwner().getFullName() : null);
        r.setPublicCode(shop.getPublicCode());

        r.setShopName(shop.getShopName());
        r.setShopLogoUrl(shop.getLogoPath());
        r.setCategory(shop.getCategory());
        r.setWhatsappNumber(shop.getWhatsappNumber());
        r.setShortBio(shop.getShortBio());
        r.setInstagramId(shop.getInstagramId());

        r.setStatus(shop.getStatus());
        r.setActive(shop.isActive());
        r.setFullAddress(shop.getFullAddress());
        r.setLocation(shop.getGpsLocation());

        r.setArea(shop.getArea());
        r.setCity(shop.getCity());
        r.setPinCode(shop.getPincode());
        r.setShopPlanStatus(String.valueOf(shop.getSubscription().getPlan()));
        return r;
    }


}
