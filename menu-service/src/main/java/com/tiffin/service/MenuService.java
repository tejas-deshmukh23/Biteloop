package com.tiffin.service;

import com.tiffin.common.enums.MenuCategory;
import com.tiffin.dto.MenuItemRequest;
import com.tiffin.dto.MenuItemResponse;

import java.util.List;

public interface MenuService {

    MenuItemResponse createItem(String providerId, MenuItemRequest request);

    List<MenuItemResponse> getAvailableItems(String providerId);

    List<MenuItemResponse> getAllItemsForProvider(String providerId);

    List<MenuItemResponse> getByVeg(String providerId, boolean isVeg);

    List<MenuItemResponse> getByCategory(String providerId, MenuCategory category);

    MenuItemResponse updateItem(String itemId, String providerId, MenuItemRequest request);

    MenuItemResponse toggleAvailability(String itemId, String providerId);

    void deleteItem(String itemId, String providerId);
}