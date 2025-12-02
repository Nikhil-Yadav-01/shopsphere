package com.rudraksha.shopsphere.returns.mapper;

import com.rudraksha.shopsphere.returns.dto.response.ReturnResponse;
import com.rudraksha.shopsphere.returns.dto.response.RMAResponse;
import com.rudraksha.shopsphere.returns.entity.ReturnRequest;
import com.rudraksha.shopsphere.returns.entity.RMA;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReturnMapper {

    ReturnResponse toReturnResponse(ReturnRequest returnRequest);

    RMAResponse toRMAResponse(RMA rma);
}
