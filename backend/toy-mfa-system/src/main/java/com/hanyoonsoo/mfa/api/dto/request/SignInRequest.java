package com.hanyoonsoo.mfa.api.dto.request;

public record SignInRequest(
  String username,
  String password
) {}
