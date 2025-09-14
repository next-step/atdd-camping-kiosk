package com.camping.kiosk.web;

import com.camping.kiosk.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final PaymentService paymentService;

    @GetMapping("/health")
    @ResponseBody
    public String health() {
        return "OK";
    }

    @GetMapping("/")
    public String index(Model model) {
        try {
            model.addAttribute("products", paymentService.loadProducts());
        } catch (Exception e) {
            log.warn("상품 목록 로딩 실패 - 빈 리스트로 대체. adminBaseUrl 설정 및 관리자 서버 가동 여부, 인증 쿠키 유효성 확인 필요", e);
            model.addAttribute("products", List.of());
        }
        return "index";
    }
}


