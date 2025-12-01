package com.rudraksha.shopsphere.user.service.impl;

import com.rudraksha.shopsphere.user.dto.request.CreateAddressRequest;
import com.rudraksha.shopsphere.user.dto.request.UpdateUserRequest;
import com.rudraksha.shopsphere.user.dto.response.AddressResponse;
import com.rudraksha.shopsphere.user.dto.response.UserResponse;
import com.rudraksha.shopsphere.user.entity.Address;
import com.rudraksha.shopsphere.user.entity.UserProfile;
import com.rudraksha.shopsphere.user.mapper.UserMapper;
import com.rudraksha.shopsphere.user.repository.AddressRepository;
import com.rudraksha.shopsphere.user.repository.UserProfileRepository;
import com.rudraksha.shopsphere.user.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserProfileRepository userProfileRepository;
    private final AddressRepository addressRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse getCurrentUser(UUID authUserId) {
        UserProfile profile = getOrCreateProfile(authUserId);
        return userMapper.toUserResponse(profile);
    }

    @Override
    @Transactional
    public UserResponse updateCurrentUser(UUID authUserId, UpdateUserRequest request) {
        UserProfile profile = getOrCreateProfile(authUserId);
        userMapper.updateUserProfile(profile, request);
        UserProfile saved = userProfileRepository.save(profile);
        return userMapper.toUserResponse(saved);
    }

    @Override
    public UserResponse getUserById(UUID userId) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return userMapper.toUserResponse(profile);
    }

    @Override
    public List<AddressResponse> getAddresses(UUID authUserId) {
        UserProfile profile = getProfileByAuthUserId(authUserId);
        return userMapper.toAddressResponseList(profile.getAddresses());
    }

    @Override
    @Transactional
    public AddressResponse createAddress(UUID authUserId, CreateAddressRequest request) {
        UserProfile profile = getOrCreateProfile(authUserId);

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.clearDefaultForType(profile.getId(), request.getType());
        }

        Address address = userMapper.toAddress(request);
        profile.addAddress(address);
        Address saved = addressRepository.save(address);
        return userMapper.toAddressResponse(saved);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(UUID authUserId, UUID addressId, CreateAddressRequest request) {
        UserProfile profile = getProfileByAuthUserId(authUserId);

        Address address = addressRepository.findByIdAndUserProfileId(addressId, profile.getId())
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + addressId));

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            addressRepository.clearDefaultForType(profile.getId(), request.getType());
        }

        userMapper.updateAddress(address, request);
        Address saved = addressRepository.save(address);
        return userMapper.toAddressResponse(saved);
    }

    @Override
    @Transactional
    public void deleteAddress(UUID authUserId, UUID addressId) {
        UserProfile profile = getProfileByAuthUserId(authUserId);

        Address address = addressRepository.findByIdAndUserProfileId(addressId, profile.getId())
                .orElseThrow(() -> new EntityNotFoundException("Address not found with id: " + addressId));

        profile.removeAddress(address);
        addressRepository.delete(address);
    }

    private UserProfile getProfileByAuthUserId(UUID authUserId) {
        return userProfileRepository.findByAuthUserId(authUserId)
                .orElseThrow(() -> new EntityNotFoundException("User profile not found for auth user: " + authUserId));
    }

    private UserProfile getOrCreateProfile(UUID authUserId) {
        return userProfileRepository.findByAuthUserId(authUserId)
                .orElseGet(() -> {
                    UserProfile newProfile = UserProfile.builder()
                            .authUserId(authUserId)
                            .build();
                    return userProfileRepository.save(newProfile);
                });
    }
}
