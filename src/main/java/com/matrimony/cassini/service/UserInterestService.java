package com.matrimony.cassini.service;

import java.util.List;
import java.util.Optional;

import com.matrimony.cassini.dto.FilterRequestDto;
import com.matrimony.cassini.dto.InterestRequestDto;
import com.matrimony.cassini.dto.InterestResponseDto;
import com.matrimony.cassini.dto.UserAcceptanceRequestDto;
import com.matrimony.cassini.entity.User;
import com.matrimony.cassini.exception.RequestNotRaisedException;
import com.matrimony.cassini.exception.UserMappingNotFound;
import com.matrimony.cassini.exception.UserNotFoundException;

public interface UserInterestService {
	
	List<User> getAllFilteredUsers(FilterRequestDto filterRequestDto);

	List<User> acceptedDetails(Integer userId);


	List<Optional<User>> requestList(Integer userId) throws RequestNotRaisedException;

	String userResponse(UserAcceptanceRequestDto userAcceptanceRequestDto) throws UserMappingNotFound;

	InterestResponseDto showInterest(InterestRequestDto interestRequestDto) throws UserNotFoundException;

}
