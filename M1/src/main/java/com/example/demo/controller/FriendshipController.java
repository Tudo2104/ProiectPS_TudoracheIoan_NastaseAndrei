package com.example.demo.controller;

import com.example.demo.dto.userdto.UserDTO;
import com.example.demo.dto.userdto.UserViewDTO;
import com.example.demo.entity.Friendship;
import com.example.demo.entity.User;
import com.example.demo.errorhandler.UserException;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.FriendshipService;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping(value = "/api/user")
@RequiredArgsConstructor
public class FriendshipController {


    private final FriendshipService friendshipService;

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/sendFriendRequest")
    public ResponseEntity<?> processsendFriendsForm(   @RequestHeader("Authorization") String token,
                                                         @RequestBody Map<String, String> request) throws UserException {
        return friendshipService.sendFriends(request);
    }

    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/acceptFriendRequest")
    public ResponseEntity<?> processAcceptFriendsForm(   @RequestHeader("Authorization") String token,
                                                         @RequestBody Map<String, String> request) throws UserException {
        return friendshipService.acceptFriends(request);
    }
    @RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/rejectFriendRequest")
    public ResponseEntity<?> processRejectFriendsForm(   @RequestHeader("Authorization") String token,
                                                         @RequestBody Map<String, String> request) throws UserException {
        return friendshipService.rejectFriends(request);
    }

    @RequestMapping(method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE, value = "/showFriends")
    public ResponseEntity<?> processShowFriendsForm(@RequestHeader("Authorization") String token) throws UserException {
        return friendshipService.showFriends();
    }

}
