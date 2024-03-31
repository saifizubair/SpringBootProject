package com.mini.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mini.WebClientConfig;
import com.mini.entity.User;
import com.mini.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final WebClient api1WebClient;
    private final WebClient api2WebClient;
    private final WebClient api3WebClient;
    private final UserRepository userRepository;

    @Autowired
    public UserService(WebClientConfig webClientConfig, UserRepository userRepository) {
        this.api1WebClient = webClientConfig.api1WebClient();
        this.api2WebClient = webClientConfig.api2WebClient();
        this.api3WebClient = webClientConfig.api3WebClient();
        this.userRepository = userRepository;
    }

    @Transactional
    public void processUserData(int size) {
        if (size < 1 || size > 5) {
            throw new IllegalArgumentException("Size should be between 1 and 5");
        }

        String randomUserUrl = "https://randomuser.me/api/";
        String nationalityUrl = "https://api.nationalize.io/";
        String genderUrl = "https://api.genderize.io/";

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            for (int i = 0; i < size; i++) {
                String randomUserData = fetchDataFromAPI(randomUserUrl, api1WebClient);
                JsonNode userData = objectMapper.readTree(randomUserData);

                String firstName = userData.get("results").get(0).get("name").get("first").asText();

                String nationalityData = fetchDataFromAPI(nationalityUrl + "?name=" + firstName, api2WebClient);
                JsonNode nationality = objectMapper.readTree(nationalityData);
                List<String> countryIds = nationality.findValuesAsText("country_id");

                String genderData = fetchDataFromAPI(genderUrl + "?name=" + firstName, api3WebClient);
                JsonNode gender = objectMapper.readTree(genderData);
                String apiGender = gender.get("gender").asText();

                String verificationStatus = determineVerificationStatus(userData, countryIds, apiGender);

                User newUser = createNewUser(userData, verificationStatus);
                userRepository.save(newUser);
            }
        } catch (Exception e) {
            // Handle exceptions appropriately based on your application's needs
            e.printStackTrace();
        }
    }

    private String fetchDataFromAPI(String url, WebClient webClient) {
        return webClient.get().uri(url).retrieve().bodyToMono(String.class).block();
    }

    private String determineVerificationStatus(JsonNode userData, List<String> countryIds, String apiGender) {
        String userGender = userData.get("results").get(0).get("gender").asText();
        String userNat = userData.get("results").get(0).get("nat").asText();

        String verificationStatus = "TO_BE_VERIFIED"; // Default status

        if (userNat.equals("IN") && countryIds.contains("IN") && apiGender.equals(userGender)) {
            verificationStatus = "VERIFIED";
        }

        return verificationStatus;
    }

    private User createNewUser(JsonNode userData, String verificationStatus) {
        String firstName = userData.get("results").get(0).get("name").get("first").asText();
        String lastName = userData.get("results").get(0).get("name").get("last").asText();
        String fullname = firstName + " " + lastName;
        String userGender = userData.get("results").get(0).get("gender").asText();
        int userAge = userData.get("results").get(0).get("dob").get("age").asInt();
        String userNat = userData.get("results").get(0).get("nat").asText();

        User newUser = new User();
        newUser.setName(fullname);
        newUser.setGender(userGender);
        newUser.setAge(userAge);
        newUser.setVerificationStatus(verificationStatus);
        newUser.setNationality(userNat);
        newUser.setDateCreated(LocalDateTime.now());
        newUser.setDateModified(LocalDateTime.now());

        return newUser;
    }

    public List<User> getUsersWithSortingAndPagination(String sortType, String sortOrder, Integer limit, Integer offset) {
        List<User> users = userRepository.findAll();

        if (sortType == null || sortOrder == null || limit == null || offset == null) {
            throw new IllegalArgumentException("sortType, sortOrder, limit, and offset are required");
        }

        if (limit < 1 || limit > 5) {
            throw new IllegalArgumentException("Limit should be between 1 and 5");
        }

        if (offset < 0) {
            throw new IllegalArgumentException("Offset should be a non-negative value");
        }

        if (sortType.equalsIgnoreCase("Name")) {
            users.sort(Comparator.comparing(User::getName));
        } else if (sortType.equalsIgnoreCase("Age")) {
            users.sort(Comparator.comparingInt(User::getAge));
        } else {
            throw new IllegalArgumentException("Invalid sortType");
        }

        if (sortOrder.equalsIgnoreCase("EVEN")) {
            users = users.stream().filter(user -> user.getAge() % 2 == 0).collect(Collectors.toList());
        } else if (sortOrder.equalsIgnoreCase("ODD")) {
            users = users.stream().filter(user -> user.getName().length() % 2 != 0).collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("Invalid sortOrder");
        }

        // Pagination logic using limit and offset
        int fromIndex = Math.min(offset, users.size());
        int toIndex = Math.min(offset + limit, users.size());

        return users.subList(fromIndex, toIndex);
    }

}
