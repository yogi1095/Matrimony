package com.matrimony.cassini.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.matrimony.cassini.constants.Constant;
import com.matrimony.cassini.controller.UserInterestController;
import com.matrimony.cassini.dto.FilterRequestDto;
import com.matrimony.cassini.dto.InterestRequestDto;
import com.matrimony.cassini.dto.InterestResponseDto;
import com.matrimony.cassini.dto.UserAcceptanceRequestDto;
import com.matrimony.cassini.entity.User;
import com.matrimony.cassini.entity.UserInterest;
import com.matrimony.cassini.exception.RequestNotRaisedException;
import com.matrimony.cassini.exception.UserNotFoundException;
import com.matrimony.cassini.repository.UserInterestRepository;
import com.matrimony.cassini.repository.UserRepository;

@Service
public class UserInterestServiceImpl implements UserInterestService {
	private static final Logger logger = LoggerFactory.getLogger(UserInterestController.class);
	/**
	 * This will inject all the implementations in the userRepository
	 */

	@Autowired
	UserRepository userRepository;
	/**
	 * This will inject all the implementations in the userInterestRepository
	 */

	@Autowired
	UserInterestRepository userInterestRepository;

	/**
	 * This API is used to get the users while searching the opposite sex users
	 * filterRequestdto includes userID,occupation,religion,dateOfBirth This returns
	 * the users as per the requirements after filtering occupation,dateOfBirth and
	 * religion
	 */

	@Override
	public List<User> getAllFilteredUsers(FilterRequestDto filterRequestDto) {
		logger.info("to get filtered user");
		Optional<User> user = userRepository.findById(filterRequestDto.getUserId());
		if (user.isPresent()) {
			List<User> users = userRepository.findByGenderNot(user.get().getGender());
			List<UserInterest> userInterests = userInterestRepository.findByToUser(user.get());
			for (UserInterest userInterest : userInterests) {
				users = users.stream()
						.filter(user1 -> !(user1.getUserId().equals(userInterest.getFromUser().getUserId())))
						.collect(Collectors.toList());
			}
			List<UserInterest> userInterests1 = userInterestRepository.findByFromUserAndStatus(user.get(), "Requested");
			for (UserInterest userInterest : userInterests1) {
				users = users.stream()
						.filter(user1 -> !(user1.getUserId().equals(userInterest.getToUser().getUserId())))
						.collect(Collectors.toList());
			}

			if (filterRequestDto.getOccupation() != null) {
				users = users.stream().filter(user1 -> user1.getOccupation().equals(filterRequestDto.getOccupation()))
						.collect(Collectors.toList());
			}
			if (filterRequestDto.getReligion() != null) {
				users = users.stream().filter(user1 -> user1.getReligion().equals(filterRequestDto.getReligion()))
						.collect(Collectors.toList());
			}
			if (filterRequestDto.getDateOfBirth() != null) {
				users = users.stream()
						.filter(user1 -> user1.getDateOfBirth().isAfter(filterRequestDto.getDateOfBirth()))
						.collect(Collectors.toList());
			}
			return users;
		} else {
			return new ArrayList<>();
		}
	}

	/**
	 * Here by passing the userId the users who accepted the request their details
	 * could be listed by finding the sender and their status
	 * 
	 */

	@Override
	public List<User> acceptedDetails(Integer userId) {
		logger.info("to get accepted user details");
		List<User> users = new ArrayList<>();
		Optional<User> user = userRepository.findById(userId);
		if (user.isPresent()) {
			List<UserInterest> userMappings = userInterestRepository.findByFromUserAndStatus(user.get(),
					Constant.ACCEPTED);
			userMappings.forEach(intrested -> {
				users.add(intrested.getToUser());
			});
		}
		return users;
	}

	/**
	 * This method is used to send the request or show interest to opposite user
	 * with the message request sent successfully
	 * 
	 * interestRequestDto contains fromUserId and toUserId in which the request sent
	 * from one user to other user
	 * 
	 * This method returns the interestResponseDto which contains statuscode and
	 * message UserNotFoundException occurs when toUserId is not found while sending
	 * the request or showing interest
	 */

	@Override
	public InterestResponseDto showInterest(InterestRequestDto interestRequestDto) throws UserNotFoundException {
		logger.info("to show interest on user");
		Optional<User> sender = userRepository.findById(interestRequestDto.getFromUserId());
		Optional<User> receiver = userRepository.findById(interestRequestDto.getToUserId());
		if (!sender.isPresent()) {
			throw new UserNotFoundException(Constant.SENDER_PROFILE_NOT_FOUND);
		}
		if (!receiver.isPresent()) {
			throw new UserNotFoundException(Constant.RECEIVER_PROFILE_NOT_FOUND);
		}
		UserInterest interest = new UserInterest();
		interest.setStatus("interested");
		interest.setFromUser(sender.get());
		interest.setToUser(receiver.get());
		userInterestRepository.save(interest);
		InterestResponseDto interestResponseDto = new InterestResponseDto();
		interestResponseDto.setMessage(Constant.REQUESTED);
		interestResponseDto.setStatusCode(Constant.OK);
		return interestResponseDto;

	}

	/**
	 * This method is used to get the interested users along with their details
	 * 
	 * Here By passing the userId the interested users who are interested to accept
	 * the request are listed This method returns the list of interested users
	 * RequestNotRaisedException occurs when request is not raised by the user
	 */

	@Override
	public List<Optional<User>> requestList(Integer userId) throws RequestNotRaisedException {
		logger.info("to get the requested user");
		Optional<User> currentuser = userRepository.findById(userId);
		List<UserInterest> userInterests = userInterestRepository.findAllUserMappingsByToUserAndStatus(currentuser,
				Constant.REQUESTED);
		List<Optional<User>> users = new ArrayList<>();
		if (userInterests.isEmpty()) {
			return new ArrayList<>();
		} else {
			for (UserInterest userInterest : userInterests) {
				Optional<User> interresteduser = userRepository.findById(userInterest.getFromUser().getUserId());
				users.add(interresteduser);
			}
			return users;
		}

	}

	/**
	 * This method is used to accept or deny the request userAcceptanceRequestDto
	 * includes fromUserId,toUserId,statuscode
	 * 
	 * This method returns the message as rejected or accepted
	 * 
	 * UserMappingNotFound exception occurs when user mapping not found
	 * RequestNotRaisedException occurs when request is not raised by the user
	 * @throws UserNotFoundException 
	 */

	@Override
	public String userResponse(UserAcceptanceRequestDto userAcceptanceRequestDto)
			throws RequestNotRaisedException, UserNotFoundException {
		logger.info("to get accepted user details");
		Optional<User> fromUser = userRepository.findById(userAcceptanceRequestDto.getFromUserId());
		Optional<User> toUser = userRepository.findById(userAcceptanceRequestDto.getToUserId());

		if (fromUser.isPresent() && toUser.isPresent()) {
			Optional<UserInterest> userInterests = userInterestRepository.findByFromUserAndToUser(toUser.get(),
					fromUser.get());
			if (!userInterests.isPresent()) {
				throw new RequestNotRaisedException(Constant.REQUEST_NOT_RAISED);
			} else {
				if (userAcceptanceRequestDto.getStatusCode().equals(Constant.ACCEPTED_CODE)) {

					userInterests.get().setStatus(Constant.ACCEPTED);
					userInterestRepository.save(userInterests.get());
					UserInterest acceptedUserMapping = new UserInterest();
					acceptedUserMapping
							.setFromUser(fromUser.get());
					acceptedUserMapping
							.setToUser(toUser.get());
					acceptedUserMapping.setStatus(Constant.ACCEPTED);
					userInterestRepository.save(acceptedUserMapping);
					return Constant.ACCEPTED;
				} else {
					userInterests.get().setStatus(Constant.REJECTED);
					userInterestRepository.save(userInterests.get());
					return Constant.REJECTED;
				}

			}
		}else {
			throw new UserNotFoundException(Constant.USER_NOT_FOUND);
		}

	}

}
