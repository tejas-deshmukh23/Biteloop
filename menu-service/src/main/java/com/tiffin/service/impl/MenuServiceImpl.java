package com.tiffin.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tiffin.common.enums.MenuCategory;
import com.tiffin.dto.MenuItemRequest;
import com.tiffin.dto.MenuItemResponse;
import com.tiffin.entity.MenuItem;
import com.tiffin.exception.MenuItemNotFoundException;
import com.tiffin.repository.MenuItemRepository;
import com.tiffin.service.MenuCacheService;
import com.tiffin.service.MenuService;

@Service
@Transactional
public class MenuServiceImpl implements MenuService {
	
	private final MenuItemRepository menuItemRepository;
	private final MenuCacheService menuCacheService;
	
	public MenuServiceImpl(MenuItemRepository menuItemRepository, MenuCacheService menuCacheService) {
		this.menuItemRepository = menuItemRepository;
		this.menuCacheService = menuCacheService;
	}

	@Override
	public MenuItemResponse createItem(String providerId, MenuItemRequest request) {
		MenuItem item = new MenuItem(
                providerId,
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getCategory(),
                request.getIsVeg()
        );
		
		MenuItem saved = menuItemRepository.save(item);
        menuCacheService.evict(providerId);
        return toResponse(saved);
		
	}

	@Override
	@Transactional(readOnly = true)
	public List<MenuItemResponse> getAvailableItems(String providerId) {
		List<MenuItemResponse> cached = menuCacheService.get(providerId);
		if(cached != null) {
			return cached;
		}
		
		//without stream
//		List<MenuItem> menuItems = menuItemRepository.findByProviderIdAndIsAvailableTrue(providerId);
//		
//		List<MenuItemResponse> items = new ArrayList<>();
//		for(MenuItem menuItem : menuItems) {
//			MenuItemResponse menuItemResponse = toResponse(menuItem);
//			items.add(menuItemResponse);
//		}
//		
//		menuCacheService.put(providerId, items);
//		
//		return items;
		
		//with stream
		List<MenuItemResponse> items = menuItemRepository
				.findByProviderIdAndIsAvailableTrue(providerId)
				.stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
		
		menuCacheService.put(providerId, items);
		
		return items;
	}

	@Override
	@Transactional(readOnly = true)
	public List<MenuItemResponse> getAllItemsForProvider(String providerId) {
		return menuItemRepository
				.findByProviderId(providerId)
				.stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly=true)
	public List<MenuItemResponse> getByVeg(String providerId, boolean isVeg) {
		return menuItemRepository
				.findByProviderIdAndIsAvailableTrueAndIsVeg(providerId, isVeg)
				.stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly=true)
	public List<MenuItemResponse> getByCategory(String providerId, MenuCategory category) {
		return menuItemRepository
				.findByProviderIdAndIsAvailableTrueAndCategory(providerId, category)
				.stream()
				.map(this::toResponse)
				.collect(Collectors.toList());
	}

	@Override
	public MenuItemResponse updateItem(String itemId, String providerId, MenuItemRequest request) {
		 MenuItem item = menuItemRepository.findByIdAndProviderId(itemId, providerId)
	                .orElseThrow(() -> new MenuItemNotFoundException(
	                        "Menu item not found or does not belong to this provider"));

	        item.setName(request.getName());
	        item.setDescription(request.getDescription());
	        item.setPrice(request.getPrice());
	        item.setCategory(request.getCategory());
	        item.setVeg(request.getIsVeg());

	        MenuItem updated = menuItemRepository.save(item);
	        menuCacheService.evict(providerId);
	        return toResponse(updated);
	}

	@Override
	public MenuItemResponse toggleAvailability(String itemId, String providerId) {
		MenuItem item = menuItemRepository.findByIdAndProviderId(itemId, providerId)
                .orElseThrow(() -> new MenuItemNotFoundException(
                        "Menu item not found or does not belong to this provider"));

        item.setAvailable(!item.isAvailable());

        MenuItem updated = menuItemRepository.save(item);
        menuCacheService.evict(providerId);
        return toResponse(updated);
	}

	@Override
	public void deleteItem(String itemId, String providerId) {
		MenuItem item = menuItemRepository.findByIdAndProviderId(itemId, providerId)
                .orElseThrow(() -> new MenuItemNotFoundException(
                        "Menu item not found or does not belong to this provider"));

        item.setAvailable(false);
        menuItemRepository.save(item);
        menuCacheService.evict(providerId);
	}
	
	// ── Mapper ─────────────────────────────────────────────────

    private MenuItemResponse toResponse(MenuItem item) {
        MenuItemResponse response = new MenuItemResponse();
        response.setId(item.getId());
        response.setProviderId(item.getProviderId());
        response.setName(item.getName());
        response.setDescription(item.getDescription());
        response.setPrice(item.getPrice());
        response.setCategory(item.getCategory());
        response.setVeg(item.isVeg());
        response.setAvailable(item.isAvailable());
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());
        return response;
    }

}
