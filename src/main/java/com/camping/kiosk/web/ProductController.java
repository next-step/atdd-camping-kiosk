package com.camping.kiosk.web;

import com.camping.kiosk.external.admin.Product;
import com.camping.kiosk.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<List<Product>> list() {
        return ResponseEntity.ok(adminService.loadProducts());
    }
}


