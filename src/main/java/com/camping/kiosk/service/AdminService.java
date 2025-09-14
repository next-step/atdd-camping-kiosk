package com.camping.kiosk.service;

import com.camping.kiosk.external.admin.AdminClient;
import com.camping.kiosk.external.admin.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AdminClient adminClient;

    public List<Product> loadProducts() {
        return adminClient.getProducts();
    }
}


