package com.example.demo.service;

import com.example.demo.builder.userbuilder.UserViewBuilder;
import com.example.demo.dto.userdto.UserDTO;
import com.example.demo.dto.userdto.UserViewDTO;
import com.example.demo.entity.Friendship;
import com.example.demo.entity.FriendshipStatus;
import com.example.demo.entity.PostStatus;
import com.example.demo.entity.User;
import com.example.demo.errorhandler.UserException;
import com.example.demo.repository.FriendshipRepository;

import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    public Friendship sendFriendRequest(User user, User friend) {


        Friendship friendship = new Friendship();
        friendship.setUser(user);
        friendship.setFriend(friend);
        friendship.setStatus(FriendshipStatus.PENDING);

        return friendshipRepository.save(friendship);
    }

    public ResponseEntity<?> sendFriends(Map<String, String> request) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);

        if (currentUser == null || currentUser.getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found!");
        }


        String name = request.get("email");
        if (name == null) {
            return ResponseEntity.badRequest().body("Friend's name is required!");
        }


        Optional<User> friend = userRepository.findUserByEmail(name);
        if (friend.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Friend email not found!");
        }

        if (friendshipRepository.existsByUserAndFriend(currentUser, friend.get())) {
            return ResponseEntity.badRequest().body("Friendship sent earlier!");
        }
        sendFriendRequest(currentUser, friend.get());

        return ResponseEntity.ok("Friend request sent!");
    }

    public boolean acceptFriendRequest(User recipient, User sender) {
        Friendship friendship = friendshipRepository.findByUserAndFriend(sender, recipient);

        if (friendship == null || friendship.getStatus() != FriendshipStatus.PENDING) {
            return false;
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);
        return true;
    }

    public ResponseEntity<?> acceptFriends(Map<String, String> request) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);

        if (currentUser == null || currentUser.getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found!");
        }


        String name = request.get("name");
        if (name == null) {
            return ResponseEntity.badRequest().body("Friend's name is required!");
        }

        User friend = userRepository.findUserByName(name);

        boolean accepted = acceptFriendRequest(currentUser, friend);
        if (!accepted) {
            return ResponseEntity.badRequest().body("Friend request not found or already accepted!");
        }

        return ResponseEntity.ok("Friend request accepted!");
    }

    public boolean rejectFriendRequest(User recipient, User sender) {
        Friendship friendship = friendshipRepository.findByUserAndFriend(sender, recipient);

        if (friendship == null || friendship.getStatus() != FriendshipStatus.PENDING) {
            return false;
        }

        friendship.setStatus(FriendshipStatus.REJECTED);
        friendshipRepository.save(friendship);
        return true;
    }

    public ResponseEntity<?> rejectFriends(Map<String, String> request) {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);

        if (currentUser == null || currentUser.getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found!");
        }


        String name = request.get("name");
        if (name == null) {
            return ResponseEntity.badRequest().body("Friend's name is required!");
        }

        User friend = userRepository.findUserByName(name);

        boolean rejected = rejectFriendRequest(currentUser, friend);
        if (!rejected) {
            return ResponseEntity.badRequest().body("Friend request not found or already rejected!");
        }

        return ResponseEntity.ok("Friend request rejected!");
    }

    public ResponseEntity<?> showFriends() {

        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User must be authenticated!");
        }

        String username = ((UserDetails) principal).getUsername();
        User currentUser = userRepository.findUserByName(username);

        if (currentUser == null || currentUser.getId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found!");
        }

        List<Friendship> friendships = friendshipRepository.findAcceptedFriendships(currentUser);

        List<String> friendNames = friendships.stream()
                .map(friendship -> {
                    User friend = (friendship.getUser().equals(currentUser)) ? friendship.getFriend() : friendship.getUser();
                    return friend.getName();
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Friends");
        response.put("friends", friendNames);

        return ResponseEntity.ok(response);
    }
}
