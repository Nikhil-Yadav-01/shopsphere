package com.rudraksha.shopsphere.user.repository;

import com.rudraksha.shopsphere.user.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findByUserProfileId(UUID userProfileId);

    Optional<Address> findByIdAndUserProfileId(UUID id, UUID userProfileId);

    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.userProfile.id = :userProfileId AND a.type = :type")
    void clearDefaultForType(@Param("userProfileId") UUID userProfileId, @Param("type") Address.AddressType type);
}
