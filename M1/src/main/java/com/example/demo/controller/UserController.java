package com.example.demo.controller;



import com.example.demo.dto.chatdto.MessageDto;
import com.example.demo.dto.game.GameDTO;
import com.example.demo.dto.moderatoractionDTO.ModeratorActionDTO;


import com.example.demo.dto.userdto.UserDTO;
import com.example.demo.entity.Friendship;
import com.example.demo.entity.PostStatus;
import com.example.demo.entity.User;
import com.example.demo.errorhandler.UserException;
import com.example.demo.service.FriendshipService;
import com.example.demo.service.JWTService;
import com.example.demo.service.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @RequestMapping(value = "/getAll", method = RequestMethod.GET)
    public ResponseEntity<?> displayAllUserView(){
        return new ResponseEntity<>(userService.findAllUserView(), HttpStatus.OK);
    }

    @RequestMapping(value = "/getUserById/{id}", method = RequestMethod.GET)
    public ResponseEntity<?> displayUserViewById(@PathVariable("id") @NonNull  Long id) throws UserException {
        return new ResponseEntity<>(userService.findUserViewById(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/getUserByEmail/{email}", method = RequestMethod.GET)
    public ResponseEntity<?> displayUserViewByEmail(@PathVariable("email") String email) throws UserException {
        return new ResponseEntity<>(userService.findUserViewByEmail(email), HttpStatus.OK);
    }
    @RequestMapping(value = "/getUserByName/{name}", method = RequestMethod.GET)
    public ResponseEntity<?> displayUserViewByName(@PathVariable("name") String name) throws UserException {
        return new ResponseEntity<>(userService.findUserViewByName(name), HttpStatus.OK);
    }

    @RequestMapping(value = "/getUserByRoleName/{roleName}", method = RequestMethod.GET)
    public ResponseEntity<?> displayUserViewByRoleName(@PathVariable("roleName") String roleName) throws UserException {
        return new ResponseEntity<>(userService.findUserViewByRoleName(roleName), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/create")
    public ResponseEntity<?> processAddUserForm(@RequestBody(required = false) UserDTO userDTO) throws UserException {
        return new ResponseEntity<>(userService.registerUser(userDTO), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/update")
    public ResponseEntity<?> processUpdateUserForm(@RequestBody UserDTO userDTO) throws UserException {
        return new ResponseEntity<>(userService.updateUser(userDTO), HttpStatus.OK);
    }

    @RequestMapping(value="/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<?> deleteUserByIdForm(@PathVariable("id") Long id) throws UserException {
        userService.deleteUser(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/login")
    public ResponseEntity<?> processLoginUserForm(@RequestBody(required = false) UserDTO userDTO) throws UserException {
        return new ResponseEntity<>(userService.verifyUser(userDTO), HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/verifyRole")
    public ResponseEntity<?> processVerifyRoleForm(@RequestBody(required = false) UserDTO userDTO) throws UserException {
        return userService.showRole(userDTO);
    }

    @RequestMapping(method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/updateRole")
    public ResponseEntity<?> processUpdateRoleForm(@RequestBody(required = false) Map<String, String> request) throws UserException {
        return userService.updateRole(request);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/adminDeleteAction", consumes  = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> processAdminForm(@RequestBody ModeratorActionDTO moderatorActionDTO) {
        return userService.adminDeleteAction(moderatorActionDTO);
    }
    @RequestMapping(method = RequestMethod.PUT, value = "/adminBlockUser", consumes  = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> processAdminBlockForm(@RequestBody ModeratorActionDTO moderatorActionDTO) {
        return userService.adminBlockAction(moderatorActionDTO);
    }
    @RequestMapping(method = RequestMethod.PUT, value = "/adminUnblockUser", consumes  = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> processAdminUnblockForm(@RequestBody ModeratorActionDTO moderatorActionDTO) {
        return userService.adminUnblockAction(moderatorActionDTO);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/processAdmin")
    public ResponseEntity<?> processProcessAdminForm(@RequestBody(required = false) UserDTO userDTO){
        return userService.processRegisterAdmin(userDTO);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/createAdmin")
    public ResponseEntity<?> processCreateAdminForm(@RequestBody(required = false) UserDTO userDTO){
        return userService.registerAdmin(userDTO);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/chat")
    public ResponseEntity<?> processChatForm(@RequestBody(required = false) MessageDto messageDto){
        return userService.chat(messageDto);
    }
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/blackjack/start")
    public ResponseEntity<?> startBlackjack(@RequestBody(required = false) GameDTO gameDTO) {
        return userService.startBlackjackGame(gameDTO);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/blackjack/{gameId}/hit")
    public ResponseEntity<?> hitBlackjack(@PathVariable("gameId") Long gameId) {
        return userService.hitBlackjack(gameId);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "/blackjack/{gameId}/stand")
    public ResponseEntity<?> standBlackjack(@PathVariable("gameId") Long gameId) {
        return userService.standBlackjack(gameId);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/blackjack/{gameId}")
    public ResponseEntity<?> getBlackjackStatus(@PathVariable("gameId") Long gameId) {
        return userService.getBlackjackStatus(gameId);
    }


}
