package com.rudraksha.shopsphere.user.service;

import com.rudraksha.shopsphere.user.dto.request.CreateAddressRequest;
import com.rudraksha.shopsphere.user.dto.request.UpdateUserRequest;
import com.rudraksha.shopsphere.user.dto.response.AddressResponse;
import com.rudraksha.shopsphere.user.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse getCurrentUser(UUID authUserId);

    UserResponse updateCurrentUser(UUID authUserId, UpdateUserRequest request);

    UserResponse getUserById(UUID userId);

    List<AddressResponse> getAddresses(UUID authUserId);

    AddressResponse createAddress(UUID authUserId, CreateAddressRequest request);

    AddressResponse updateAddress(UUID authUserId, UUID addressId, CreateAddressRequest request);

    void deleteAddress(UUID authUserId, UUID addressId);
}
