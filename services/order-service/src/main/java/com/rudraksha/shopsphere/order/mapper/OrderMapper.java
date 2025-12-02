package com.rudraksha.shopsphere.order.mapper;

import com.rudraksha.shopsphere.order.dto.request.CreateOrderRequest;
import com.rudraksha.shopsphere.order.dto.response.OrderItemResponse;
import com.rudraksha.shopsphere.order.dto.response.OrderResponse;
import com.rudraksha.shopsphere.order.entity.Order;
import com.rudraksha.shopsphere.order.entity.OrderAddress;
import com.rudraksha.shopsphere.order.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "shippingAddress", source = "shippingAddress", qualifiedByName = "toAddressResponse")
    @Mapping(target = "billingAddress", source = "billingAddress", qualifiedByName = "toAddressResponse")
    @Mapping(target = "items", source = "items")
    OrderResponse toOrderResponse(Order order);

    List<OrderResponse> toOrderResponseList(List<Order> orders);

    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    List<OrderItemResponse> toOrderItemResponseList(List<OrderItem> orderItems);

    @Named("toAddressResponse")
    default OrderResponse.AddressResponse toAddressResponse(OrderAddress address) {
        if (address == null) {
            return null;
        }
        return OrderResponse.AddressResponse.builder()
                .fullName(address.getFullName())
                .phone(address.getPhone())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .build();
    }

    default OrderAddress toOrderAddress(CreateOrderRequest.AddressRequest addressRequest) {
        if (addressRequest == null) {
            return null;
        }
        return OrderAddress.builder()
                .fullName(addressRequest.getFullName())
                .phone(addressRequest.getPhone())
                .addressLine1(addressRequest.getAddressLine1())
                .addressLine2(addressRequest.getAddressLine2())
                .city(addressRequest.getCity())
                .state(addressRequest.getState())
                .postalCode(addressRequest.getPostalCode())
                .country(addressRequest.getCountry())
                .build();
    }

    default OrderItem toOrderItem(CreateOrderRequest.OrderItemRequest itemRequest) {
        if (itemRequest == null) {
            return null;
        }
        return OrderItem.builder()
                .productId(itemRequest.getProductId())
                .productName(itemRequest.getProductName())
                .sku(itemRequest.getSku())
                .quantity(itemRequest.getQuantity())
                .unitPrice(itemRequest.getPrice())
                .build();
    }
}
