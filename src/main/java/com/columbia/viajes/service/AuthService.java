package com.columbia.viajes.service;

import com.columbia.viajes.dto.request.LoginRequest;
import com.columbia.viajes.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}