package com.rudraksha.shopsphere.checkout.service;

import com.rudraksha.shopsphere.checkout.dto.request.CheckoutRequest;
import com.rudraksha.shopsphere.checkout.dto.response.CheckoutResponse;

public interface CheckoutService {
    CheckoutResponse processCheckout(CheckoutRequest request);
}
