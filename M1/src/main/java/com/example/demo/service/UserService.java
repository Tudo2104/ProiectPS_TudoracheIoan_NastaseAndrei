package com.example.demo.service;

import com.example.demo.builder.userbuilder.SimpleGameResponseBuilder;
import com.example.demo.builder.userbuilder.UserBuilder;
import com.example.demo.builder.userbuilder.UserViewBuilder;
import com.example.demo.dto.card.CardDTO;
import com.example.demo.dto.chatdto.MessageDto;
import com.example.demo.dto.game.GameDTO;
import com.example.demo.dto.gameSummarySTO.GameSummaryDTO;
import com.example.demo.dto.moderatorDTO.ModeratorDTO;
import com.example.demo.dto.moderatoractionDTO.ModeratorActionDTO;
import com.example.demo.dto.userdto.UserDTO;
import com.example.demo.dto.userdto.UserViewDTO;
import com.example.demo.entity.*;
import com.example.demo.errorhandler.UserException;
import com.example.demo.repository.FriendshipRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.validator.UserFieldValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService   {

    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    private final FriendshipRepository friendshipRepository;

    private final RoleRepository roleRepository;

    private final UserRepository userRepository;
    @Autowired
    private JWTService jwtService;
    @Autowired
    AuthenticationManager authManager;
    @Autowired
    private WebClient.Builder webClientBuilder;

    public List<UserViewDTO> findAllUserView() {

        return userRepository.findAll().stream()
                .map(UserViewBuilder::generateDTOFromEntity)
                .collect(Collectors.toList());
    }

    public UserViewDTO findUserViewById(Long id) throws UserException {

        Optional<User> user  = userRepository.findById(id);

        if (user.isEmpty()) {
            throw new UserException("User not found with id field: " + id);
        }
        return UserViewBuilder.generateDTOFromEntity(user.get());
    }

    public UserViewDTO findUserViewByEmail(String email) throws UserException {
        Optional<User> user  = userRepository.findUserByEmail(email);

        if (user.isEmpty()) {
            throw new UserException("User not found with email field: " + email);
        }
        return UserViewBuilder.generateDTOFromEntity(user.get());
    }
    public UserViewDTO findUserViewByName(String name) throws UserException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            throw new UserException("User must be authenticated!");
        }
        User user  = userRepository.findUserByName(name);

        if (user == null) {
            throw new UserException("User not found with name field: " + name);
        }
        return UserViewBuilder.generateDTOFromEntity(user);
    }

    public Long updateUser(UserDTO userDTO) throws UserException {
        List<String> errors = UserFieldValidator.validateInsertOrUpdate(userDTO);

        if(!errors.isEmpty())
        {
            throw new UserException(StringUtils.collectionToDelimitedString(errors, "\n"));
        }

        Optional<Role> role = roleRepository.findRoleByName(userDTO.getRoleName().toUpperCase());

        if (role.isEmpty()) {
            throw new UserException("Role not found with name field: " + userDTO.getRoleName().toUpperCase());
        }

        Optional<User> user = userRepository.findById(userDTO.getId());
        if(user.isEmpty()){
            throw new UserException("User not found with id field: " + userDTO.getId());
        }


        if(!user.get().getEmail().equals(userDTO.getEmail()))
        {
            Optional<User> verifyDuplicated = userRepository.findUserByEmail(userDTO.getEmail());
            if(verifyDuplicated.isPresent() ){
                throw new UserException("User record does not permit duplicates for email field: " + userDTO.getEmail());
            }
        }

        user.get().setName(userDTO.getName());
        user.get().setEmail(userDTO.getEmail());
        user.get().setPassword(userDTO.getPassword());
        user.get().setRole(role.get());

        return userRepository.save(user.get()).getId();
    }

    public void deleteUser(Long id) throws UserException {

        Optional<User> user = userRepository.findById(id);

        if (user.isEmpty()) {
            throw new UserException("User not found with id field: " + id);
        }

        this.userRepository.deleteById(id);
    }

    public List<UserViewDTO> findUserViewByRoleName(String roleName) throws UserException {


        List<User> userList  = userRepository.findUserByRoleName(roleName);

        if (userList.isEmpty()) {
            throw new UserException("User not found with role name field: " + roleName);
        }
        return  userList.stream()
                .map(UserViewBuilder::generateDTOFromEntity)
                .collect(Collectors.toList());
    }

    public ResponseEntity<?> registerUser(UserDTO userDTO) throws UserException {
        List<String> errors = UserFieldValidator.validateInsertOrUpdate(userDTO);

        if(!errors.isEmpty())
        {
            throw new UserException(StringUtils.collectionToDelimitedString(errors, "\n"));
        }

        Optional<Role> role = roleRepository.findRoleByName("User");

        if (role.isEmpty()) {
            throw new UserException("Role not found with name field ");
        }

        Optional<User> user = userRepository.findUserByEmail(userDTO.getEmail());
        if(user.isPresent() ){
            throw new UserException("Email Already Registered");
        }
        User username = userRepository.findUserByName(userDTO.getName());
        if(username != null){
            throw new UserException("Username already in use");
        }

        User userSave = UserBuilder.generateEntityFromDTO(userDTO, role.get());
        userSave.setPassword(encoder.encode(userSave.getPassword()));
        userSave.setBalance(0.0);
        userRepository.save(userSave);
        return verifyUser(userDTO);
    }

    public ResponseEntity<?> registerAdmin(UserDTO userDTO) {

        if (userDTO.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Username must be written");
        }

        if (userDTO.getEmail() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email must be written");
        }

        if (userDTO.getPassword() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Password must be written");
        }

        Optional<Role> role = roleRepository.findRoleByName("Admin");

        if (role.isEmpty()) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Role not found with name field: Admin");
        }

        Optional<User> user = userRepository.findUserByEmail(userDTO.getEmail());
        if(user.isPresent() ){

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User record does not permit duplicates for email field: " + userDTO.getEmail());
        }

        User userSave = UserBuilder.generateEntityFromDTO(userDTO, role.get());
        userSave.setPassword(encoder.encode(userSave.getPassword()));
        userRepository.save(userSave);

        return ResponseEntity.status(HttpStatus.OK).body(UserBuilder.generateDTOFromEntity(userSave));

    }

    public ResponseEntity<?> verifyUser(UserDTO userDTO) throws UserException {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(userDTO.getName(), userDTO.getPassword())
            );

            User user = userRepository.findUserByName(userDTO.getName());
            if (user == null) {
                throw new UserException("User not found.");
            }

            userDTO.setId(user.getId());


            return ResponseEntity.ok(jwtService.generateToken(userDTO.getName())) ;

        } catch (BadCredentialsException ex) {
            throw new UserException("Invalid username or password.");
        } catch (Exception ex) {
            throw new UserException("An unexpected error occurred.");
        }

    }

    public ResponseEntity<String> verifyLogin(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Bad Token");
        }
        return ResponseEntity.status(HttpStatus.OK).body("Good Token");
    }

    public ResponseEntity<?> showRole(UserDTO userDTO) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }

        return ResponseEntity.ok("Role Status for "+currentUser.getName()+" is "+currentUser.getRole().getName());
    }

    public ResponseEntity<?> updateRole(Map<String, String> request) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found!");
        }

        if (!currentUser.getRole().getName().equals("Admin")) {
            return ResponseEntity.ok("Cannot modify role as a user!!!!!!");
        }

        String user = request.get("name");
        if (user == null) {
            return ResponseEntity.badRequest().body("User's name is required!");
        }

        User userChange = userRepository.findUserByName(user);
        if (userChange == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User to be updated not found!");
        }

        String roleName = request.get("role");
        if (roleName == null) {
            return ResponseEntity.badRequest().body("Role's name is required!");
        }

        if (!roleName.equals("Admin") && !roleName.equals("User")) {
            return ResponseEntity.badRequest().body("Role's name doesn't exist!");
        }

        Optional<Role> role = roleRepository.findRoleByName(roleName.toUpperCase());
        if (role.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Role not found in database!");
        }

        userChange.setRole(role.get());
        userRepository.save(userChange);

        return ResponseEntity.ok("The role has been modified!!!!!!");
    }

    public ResponseEntity<String> adminDeleteAction(ModeratorActionDTO moderatorActionDTO) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);
        if (currentUser.getRole().getId() != 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Must be an admin to delete posts with this action.");
        }

        moderatorActionDTO.setModeratorId(currentUser.getId());
        if(moderatorActionDTO.getTargetUserId() != null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Method for deleting posts or comments, not blocking users.");
        }

        if(moderatorActionDTO.getTargetCommentId() != null &&moderatorActionDTO.getTargetPostId() != null ){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Cannot perform both deleting in the same time introduce them one by one.");
        }

        try {
            ResponseEntity<String> response = webClientBuilder.build()
                    .method(HttpMethod.DELETE)
                    .uri("http://localhost:8083/api/mod/deleteAdmin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(moderatorActionDTO)
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            return response;

        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    public ResponseEntity<String> adminBlockAction(ModeratorActionDTO moderatorActionDTO) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);
        if (currentUser.getRole().getId() != 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Must be an admin to block.");
        }

        moderatorActionDTO.setModeratorId(currentUser.getId());

        if(moderatorActionDTO.getTargetCommentId() != null || moderatorActionDTO.getTargetPostId()!= null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Method for blocking users,not deleting posts or comments.");
        }
        if(moderatorActionDTO.getTargetUserId() == null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Add an user Id to block/unblock.");
        }
        Optional<User> targetUser = userRepository.findById(moderatorActionDTO.getTargetUserId());
        if(targetUser.isEmpty()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("The user with this Id doesn't exist.");
        }

        try {
            ResponseEntity<String> response = webClientBuilder.build()
                    .method(HttpMethod.PUT)
                    .uri("http://localhost:8083/api/mod/blockUser")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(moderatorActionDTO)
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            return response;

        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    public ResponseEntity<String> adminUnblockAction(ModeratorActionDTO moderatorActionDTO) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);
        if (currentUser.getRole().getId() != 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Must be an admin to unblock.");
        }

        moderatorActionDTO.setModeratorId(currentUser.getId());

        if(moderatorActionDTO.getTargetCommentId() != null || moderatorActionDTO.getTargetPostId()!= null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Method for unblocking users,not deleting posts or comments.");
        }
        if(moderatorActionDTO.getTargetUserId() == null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Add an user Id to unblock.");
        }
        Optional<User> targetUser = userRepository.findById(moderatorActionDTO.getTargetUserId());
        if(targetUser.isEmpty()){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("The user with this Id doesn't exist.");
        }

        try {
            ResponseEntity<String> response = webClientBuilder.build()
                    .method(HttpMethod.PUT)
                    .uri("http://localhost:8083/api/mod/unblockUser")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(moderatorActionDTO)
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            return response;

        } catch (WebClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    public ResponseEntity<?> processRegisterAdmin(UserDTO userDTO) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);
        if (currentUser.getRole().getId() != 1) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Must be an admin to create an admin account.");
        }
        ModeratorDTO moderatorDTO = new ModeratorDTO();
        moderatorDTO.setModeratorId(currentUser.getId());
        moderatorDTO.setUserDTO(userDTO);

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = request.getHeader("Authorization");

        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization token is missing");
        }


        try {
            UserDTO response = webClientBuilder.build()
                    .method(HttpMethod.POST)
                    .uri("http://localhost:8083/api/mod/createAdmin")
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(moderatorDTO)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block();

            return ResponseEntity.ok(response);

        }catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            return ResponseEntity.status(e.getStatusCode()).body(errorBody);
        }

    }
    public ResponseEntity<?> chat(MessageDto messageDto) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }
        GameDTO lastGame = new GameDTO();
        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);
        messageDto.setSender(currentUser.getName());
        try {
            GameDTO[] responseArray = webClientBuilder.build()
                    .method(HttpMethod.GET)
                    .uri("http://localhost:8083/api/games/" + currentUser.getId()+"/history")
                    .retrieve()
                    .bodyToMono(GameDTO[].class)
                    .block();

            if (responseArray != null && responseArray.length != 0) {
                lastGame = responseArray[responseArray.length - 1];
            }


        } catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            return ResponseEntity.status(e.getStatusCode()).body(errorBody);
        }

        Long gameId = null;
        if ("IN_PROGRESS".equals(lastGame.getStatus())) {
            gameId = lastGame.getId();
            System.out.println(gameId);
        }

        try {
            webClientBuilder.build()
                    .method(HttpMethod.POST)
                    .uri("http://localhost:8082/api/message/chat/"+ gameId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(messageDto)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();


        }catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            return ResponseEntity.status(e.getStatusCode()).body(errorBody);
        }
        return ResponseEntity.ok("Message was sent.");
    }

    public ResponseEntity<?> startBlackjackGame(GameDTO gameDTO) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }
        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);
        gameDTO.setUserId(currentUser.getId());
        try {
            GameDTO response = webClientBuilder.build()
                    .method(HttpMethod.POST)
                    .uri("http://localhost:8083/api/games/start")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(gameDTO)
                    .retrieve()
                    .bodyToMono(GameDTO.class)
                    .block();

            if (response == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No response from Blackjack service.");
            }

            return ResponseEntity.ok(SimpleGameResponseBuilder.generateFromGameDTO(response));
        } catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            return ResponseEntity.status(e.getStatusCode()).body(errorBody);
        }
    }

    public ResponseEntity<?> hitBlackjack(Long gameId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }
        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);
        try {
            GameDTO response = webClientBuilder.build()
                    .method(HttpMethod.PUT)
                    .uri("http://localhost:8083/api/games/" + gameId +"/"+username+ "/hit")
                    .retrieve()
                    .bodyToMono(GameDTO.class)
                    .block();

            if (response == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No response from Blackjack service.");
            }
            if(response.getStatus().equals("PLAYER_LOST")){
                currentUser.setBalance(currentUser.getBalance()-50);
                userRepository.save(currentUser);
            }

            return ResponseEntity.ok(SimpleGameResponseBuilder.generateFromGameDTO(response));
        } catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            return ResponseEntity.status(e.getStatusCode()).body(errorBody);
        }
    }
    public ResponseEntity<?> standBlackjack(Long gameId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }
        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);
        try {
            GameDTO response = webClientBuilder.build()
                    .method(HttpMethod.PUT)
                    .uri("http://localhost:8083/api/games/" + gameId +"/"+username+  "/stand")
                    .retrieve()
                    .bodyToMono(GameDTO.class)
                    .block();

            if (response == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No response from Blackjack service.");
            }
            if(response.getStatus().equals("PLAYER_WON")){
                currentUser.setBalance(currentUser.getBalance()+50);
                userRepository.save(currentUser);
            }
            else if(response.getStatus().equals("PLAYER_LOST")){
                currentUser.setBalance(currentUser.getBalance()-50);
                userRepository.save(currentUser);
            }

            return ResponseEntity.ok(SimpleGameResponseBuilder.generateFromGameDTO(response));
        } catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            return ResponseEntity.status(e.getStatusCode()).body(errorBody);
        }
    }

    public ResponseEntity<?> getBlackjackStatus(Long gameId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        try {
            GameDTO response = webClientBuilder.build()
                    .method(HttpMethod.GET)
                    .uri("http://localhost:8083/api/games/" + gameId)
                    .retrieve()
                    .bodyToMono(GameDTO.class)
                    .block();

            if (response == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No response from Blackjack service.");
            }

            return ResponseEntity.ok(SimpleGameResponseBuilder.generateFromGameDTO(response));
        } catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            return ResponseEntity.status(e.getStatusCode()).body(errorBody);
        }
    }

    public ResponseEntity<?> updateBalance(Float addMoney) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }
        if(addMoney <=0 ){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Value must be higher than 0");
        }
        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);
        currentUser.setBalance(currentUser.getBalance()+addMoney);
        userRepository.save(currentUser);
        return  ResponseEntity.ok("Money has been deposited");

    }

    public ResponseEntity<?> getBalance() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }
        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);
        return  ResponseEntity.ok(currentUser);

    }

    public ResponseEntity<?> getBlackjackHistoryByUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }
        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);

        try {
            GameDTO[] responseArray = webClientBuilder.build()
                    .method(HttpMethod.GET)
                    .uri("http://localhost:8083/api/games/" + currentUser.getId()+"/history")
                    .retrieve()
                    .bodyToMono(GameDTO[].class)
                    .block();

            if (responseArray == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("No response from Blackjack service.");
            }

            AtomicInteger counter = new AtomicInteger(1);

            List<GameSummaryDTO> summaryList = Arrays.stream(responseArray)
                    .filter(game -> !"IN_PROGRESS".equalsIgnoreCase(game.getStatus()))
                    .map(game -> {
                        Double betValue = switch (game.getStatus()) {
                            case "PLAYER_WON" -> 50.0;
                            case "PLAYER_LOST" -> -50.0;
                            case "DRAW" -> 0.0;
                            default -> null;
                        };
                        return GameSummaryDTO.builder()
                                .id((long) counter.getAndIncrement())
                                .betValue(betValue)
                                .status(game.getStatus())
                                .build();
                    })
                    .toList();


            return ResponseEntity.ok(summaryList);
        } catch (WebClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            return ResponseEntity.status(e.getStatusCode()).body(errorBody);
        }
    }





}
