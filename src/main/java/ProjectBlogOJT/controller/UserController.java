package ProjectBlogOJT.controller;

import ProjectBlogOJT.jwt.JwtTokenProvider;
import ProjectBlogOJT.model.entity.ERole;
import ProjectBlogOJT.model.entity.Roles;
import ProjectBlogOJT.model.entity.User;
import ProjectBlogOJT.model.service.RoleService;
import ProjectBlogOJT.model.service.UserSevice;
import ProjectBlogOJT.payload.request.ChangePass;
import ProjectBlogOJT.payload.request.LoginRequest;
import ProjectBlogOJT.payload.request.SignupRequest;
import ProjectBlogOJT.payload.request.UserUpdate;
import ProjectBlogOJT.payload.response.JwtResponse;
import ProjectBlogOJT.payload.response.MessageResponse;
import ProjectBlogOJT.security.CustomUserDetails;
import ProjectBlogOJT.sendEmail.ProvideSendEmail;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;

import java.security.Principal;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:8080")
@RestController
@RequestMapping("/api/v1/auth")
public class UserController {

    @Autowired
    private UserSevice userSevice;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenProvider tokenProvider;
    @Autowired
    private RoleService roleService;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private ProvideSendEmail provideSendEmail;

    @GetMapping("/getToken")
    public ResponseEntity<?> sendEmail(@RequestParam("email") String email) {
        try {
            String jwt = tokenProvider.generateTokenEmail(email);
            provideSendEmail.sendSimpleMessage(email, "Token", jwt);
            return ResponseEntity.ok("Send email successfully");
        } catch (Exception e) {
            return ResponseEntity.ok("Failed");
        }
    }

    @PostMapping("/resetPass")
    public User resetPass(@RequestParam("token") String token, @RequestBody String newPass) {
        String userName = tokenProvider.getUserNameFromJwt(token);
        User user = userSevice.findByUserName(userName);
        user.setUserPassword(encoder.encode(newPass));

        return userSevice.saveOrUpdate(user);
    }
    @PutMapping("/changePass")
    public ResponseEntity<?> changePassword(@RequestBody ChangePass changePass) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User users = userSevice.findByID(userDetails.getUserId());
        BCryptPasswordEncoder bc = new BCryptPasswordEncoder();
        boolean passChecker = bc.matches(changePass.getOldPassword(), users.getUserPassword());
        if (passChecker) {
            boolean checkDuplicate = bc.matches(changePass.getPassword(), users.getUserPassword());
            if (checkDuplicate) {
                return ResponseEntity.ok(new MessageResponse("The new password must be different from the old password !"));
            } else {
                users.setUserPassword(encoder.encode(changePass.getPassword()));
                userSevice.saveOrUpdate(users);
                return ResponseEntity.ok(new MessageResponse("Change password successfully !"));
            }
        } else {
            return ResponseEntity.ok(new MessageResponse("Password does not match ! Change password fail"));
        }
    }
    @GetMapping("/logOut")
    public ResponseEntity<?> logOut(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        // Clear the authentication from server-side (in this case, Spring Security)
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("You have been logged out.");
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signupRequest) {
        if (userSevice.existsByUserName(signupRequest.getUserName())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Usermame is already"));
        }
        if (userSevice.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already"));

        }
        User users = new User();
        users.setUserName(signupRequest.getUserName());
        users.setUserPassword(encoder.encode(signupRequest.getPassword()));
        users.setUserEmail(signupRequest.getEmail());
        users.setUserStatus(true);
        Set<String> strRoles = signupRequest.getListRoles();
        Set<Roles> listRoles = new HashSet<>();
        if (strRoles == null) {
            Roles userRole = roleService.findByRoleName(ERole.ROLE_USER).orElseThrow(() -> new RuntimeException("Error: Role is not found"));
            listRoles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Roles adminRole = roleService.findByRoleName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found"));
                        listRoles.add(adminRole);
                    case "moderator":
                        Roles modRole = roleService.findByRoleName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found"));
                        listRoles.add(modRole);
                    case "user":
                        Roles userRole = roleService.findByRoleName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found"));
                        listRoles.add(userRole);
                }
            });
        }
        users.setListRoles(listRoles);
        userSevice.saveOrUpdate(users);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUserName(), loginRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        User users = userSevice.findByUserName(customUserDetails.getUsername());
        if(!customUserDetails.isUserStatus()){
            return ResponseEntity.ok("Your account have been block !");
        } else {
            String jwt = tokenProvider.generateToken(customUserDetails);
            List<String> listRoles = customUserDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority()).collect(Collectors.toList());
            return ResponseEntity.ok(new JwtResponse(users.getUserID(), jwt,"Bearer",users.getUserName(),users.getUserAvatar(),users.getUserEmail(),listRoles));
        }
    }
    @PostMapping("/block/{userID}")
    public ResponseEntity<?> blockUser(@PathVariable("userID") int userID) {
        User userBlock = userSevice.findByID(userID);
        userBlock.setUserStatus(false);
        userSevice.saveOrUpdate(userBlock);
        return ResponseEntity.ok("Block Successfully !");
    }
    @GetMapping()
    public List<User> readUser(){
        List<User> userList = userSevice.findAll();
        return userList;
    }

    @GetMapping("/searchUser/{userName}")
    public List<User> listSearch(@PathVariable("userName") String userName){
        List<User> listSearch = userSevice.searchByName(userName);
        return listSearch ;
    }


    @GetMapping("/filter/{option}")
    public List<User> listFilter(@PathVariable("option") Integer option){
       return userSevice.listFilter(option);
    }

    @GetMapping("/sort")
    public List<User> sortUser(@RequestParam("userName") String userName){
        List<User> listSort = userSevice.sortByName(userName);
        return  listSort;
    }
    @GetMapping("/getPagging")
    public ResponseEntity<Map<String, Object>> getPagging(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> pageBook = userSevice.getPagging(pageable);
        Map<String, Object> data = new HashMap<>();
        data.put("user", pageBook.getContent());
        data.put("total", pageBook.getSize());
        data.put("totalItems", pageBook.getTotalElements());
        data.put("totalPages", pageBook.getTotalPages());
        return new ResponseEntity<>(data, HttpStatus.OK);
    }

    @GetMapping("/login-Google")
    public RedirectView loginWithGoogle(){
        return new RedirectView("/oauth2/authorization/google");
    }

    @RequestMapping("/oauth2/success")

    public ResponseEntity<?> getEmailLoginGoogle(@AuthenticationPrincipal OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String avatar = oAuth2User.getAttribute("picture");
        String userName = oAuth2User.getAttribute("name");
        if (userSevice.existsByEmail(email)) {
            User user = userSevice.findByEmail(email);
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(user.getUserName(),user.getUserPassword())
//            );
//            SecurityContextHolder.getContext().setAuthentication(authentication);
//            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
//            if(!customUserDetails.isUserStatus()){
//                return ResponseEntity.ok("Your account have been block !");
//            } else {
//                String jwt = tokenProvider.generateToken(customUserDetails);
//                List<String> listRoles = customUserDetails.getAuthorities().stream()
//                        .map(item -> item.getAuthority()).collect(Collectors.toList());
//                return ResponseEntity.ok(new JwtResponse(user.getUserID(), jwt,"Bearer",user.getUserName(),user.getUserAvatar(),user.getUserEmail(),listRoles));
//            }
            return ResponseEntity.ok(user);
        } else {
            User user = new User();
            user.setUserName(userName);
            user.setUserPassword(encoder.encode(randomPassword()));
            user.setUserEmail(email);
            user.setUserAvatar(avatar);
            user.setUserStatus(true);
            Set<Roles> listRoles = new HashSet<>();
            Roles userRole = roleService.findByRoleName(ERole.ROLE_USER).orElseThrow(() -> new RuntimeException("Error: Role is not found"));
            listRoles.add(userRole);
            user.setListRoles(listRoles);
            userSevice.saveOrUpdate(user);
            return ResponseEntity.ok(user);
        }
    }

    public String randomPassword(){
        int length = 10;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            char randomChar = chars.charAt(index);
            sb.append(randomChar);
        }

    public OAuth2User getEmailLoginGoogle(@AuthenticationPrincipal OAuth2User principal){


        return principal;
    }

    @PostMapping("/updateUser/{userID}")
    public User updateUser(@PathVariable("userID") int userID, @RequestBody UserUpdate userUpdate){
        User user = userSevice.findByID(userID);
        Set<String> strRoles = userUpdate.getListRoles();
        Set<Roles> listRoles = new HashSet<>();
        if(strRoles == null){
            Roles userRole = roleService.findByRoleName(ERole.ROLE_USER).orElseThrow(() -> new RuntimeException("Error: Role is not found"));
            listRoles.add(userRole);
        }else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Roles adminRole = roleService.findByRoleName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found"));
                        listRoles.add(adminRole);
                    case "moderator":
                        Roles modRole = roleService.findByRoleName(ERole.ROLE_MODERATOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found"));
                        listRoles.add(modRole);
                    case "user":
                        Roles userRole = roleService.findByRoleName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found"));
                        listRoles.add(userRole);
                }
            });
        }
        user.setListRoles(listRoles);
        return userSevice.saveOrUpdate(user);
    }

        return sb.toString();
    }
}
