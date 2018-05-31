package com.cloud.uaa.web.rest;

import com.codahale.metrics.annotation.Timed;

import io.swagger.annotations.ApiOperation;
import com.cloud.uaa.domain.User;
import com.cloud.uaa.repository.UserRepository;
import com.cloud.uaa.security.DomainUserDetailsService;
import com.cloud.uaa.security.SecurityUtils;
import com.cloud.uaa.service.MailService;
import com.cloud.uaa.service.UserClient;
import com.cloud.uaa.service.UserService;
import com.cloud.uaa.service.VerifyService;
import com.cloud.uaa.service.WalletService;
import com.cloud.uaa.service.dto.UpdatePasswordDTO;
import com.cloud.uaa.service.dto.UserAnnexDTO;
import com.cloud.uaa.service.dto.UserDTO;
import com.cloud.uaa.web.rest.errors.*;
import com.cloud.uaa.web.rest.vm.KeyAndPasswordVM;
import com.cloud.uaa.web.rest.vm.ManagedUserVM;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import java.time.Instant;
import java.util.*;

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
public class AccountResource {

    private final Logger log = LoggerFactory.getLogger(AccountResource.class);

    private final UserRepository userRepository;

    private final UserService userService;

    private final MailService mailService;

    @Autowired
    private VerifyService verifyService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private UserClient userClient;

    public AccountResource(UserRepository userRepository, UserService userService, MailService mailService) {
		this.userRepository = userRepository;
		this.userService = userService;
		this.mailService = mailService;
	}

    /**
     * POST  /register : register the user.
     *
     * @param managedUserVM the managed user View Model
     * @throws InvalidPasswordException 400 (Bad Request) if the password is incorrect
     * @throws EmailAlreadyUsedException 400 (Bad Request) if the email is already used
     * @throws LoginAlreadyUsedException 400 (Bad Request) if the login is already used
     */
    @PostMapping("/register")
    @Timed
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAccount(@Valid @RequestBody ManagedUserVM managedUserVM) {
        if (!checkPasswordLength(managedUserVM.getPassword())) {
            throw new InvalidPasswordException();
        }
        userRepository.findOneByLogin(managedUserVM.getLogin().toLowerCase()).ifPresent(u -> {throw new LoginAlreadyUsedException();});
        userRepository.findOneByEmailIgnoreCase(managedUserVM.getEmail()).ifPresent(u -> {throw new EmailAlreadyUsedException();});
        User user = userService.registerUser(managedUserVM, managedUserVM.getPassword());
        mailService.sendActivationEmail(user);
    }

    /**
     * app用户修改密码
     * @author 逍遥子
     * @email 756898059@qq.com
     * @date 2018年4月13日
     * @version 1.0
     * @param managedUserVM
     */
    @ApiOperation("app用户修改密码")
    @PostMapping("/account/update-password")
    @Timed
    public void updatePasswordAppAccount(@Valid @RequestBody ManagedUserVM managedUserVM) {
    	ResponseEntity<String> resp = verifyService.getVerifyLogin(managedUserVM.getLogin());
    	String body = resp.getBody();
    	if (!managedUserVM.getVerifyCode().equals(body)) {
    		new BadRequestAlertException("验证码错误", "verifyCode", "verifyCodeError");
    	}
    	userService.updatePassword(managedUserVM.getPassword(), managedUserVM.getLogin());
    }

    @PostMapping("/register/app")
    @Timed
    @ResponseStatus(HttpStatus.CREATED)
    public void registerAppAccount(@Valid @RequestBody ManagedUserVM managedUserVM,HttpServletResponse httpServletResponse) {
    	if (!checkVerifyCode(managedUserVM.getVerifyCode())) {
    		throw new InvalidPasswordException();
    	}

    	if (!checkPasswordLength(managedUserVM.getPassword())) {
    		throw new InvalidPasswordException();
    	}
    	String code = verifyService.getVerifyCodeByPhone(managedUserVM.getLogin());
    	if (!managedUserVM.getVerifyCode().equals(code)) {
    		throw new BadRequestAlertException("验证码输入错误", "verifyService", "vcodeErr");
    	}
    	User inviterUser = null;
    	if (StringUtils.isNotBlank(managedUserVM.getInviterPhone())) {
			inviterUser = userRepository.findOneByLogin(managedUserVM.getInviterPhone()).orElseThrow(() -> new BadRequestAlertException("邀请人不存在", "inviterUser", "inviterUserNull"));
    	}
    	userRepository.findOneByLogin(managedUserVM.getLogin().toLowerCase()).ifPresent(u -> {throw new LoginAlreadyUsedException();});
    	//userRepository.findOneByEmailIgnoreCase(managedUserVM.getEmail()).ifPresent(u -> {throw new EmailAlreadyUsedException();});
    	User user = userService.registerAppUser(managedUserVM, managedUserVM.getPassword());
		//添加用户信息
    	UserAnnexDTO userAnnexDTO = new UserAnnexDTO();
    	userAnnexDTO.setId(user.getId());
    	Instant now = Instant.now();
		userAnnexDTO.setCreatedTime(now);
		userAnnexDTO.setUpdatedTime(now);
		userAnnexDTO.setPhone(user.getLogin());
		userAnnexDTO.setType(1);
		userAnnexDTO.setTypeString("普通会员");
    	userAnnexDTO.setInviterId(inviterUser == null ? null : inviterUser.getId());
    	userClient.createUserAnnex(userAnnexDTO);
    	httpServletResponse.addHeader("Access-Control-Allow-Origin", "*");
    }

    /**
     * GET  /activate : activate the registered user.
     *
     * @param key the activation key
     * @throws RuntimeException 500 (Internal Server Error) if the user couldn't be activated
     */
    @GetMapping("/activate")
    @Timed
    public void activateAccount(@RequestParam(value = "key") String key) {
        Optional<User> user = userService.activateRegistration(key);
        if (!user.isPresent()) {
            throw new InternalServerErrorException("No user was found for this reset key");
        }
    }

    /**
     * GET  /authenticate : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request
     * @return the login if the user is authenticated
     */
    @GetMapping("/authenticate")
    @Timed
    public String isAuthenticated(HttpServletRequest request) {
        log.debug("REST request to check if the current user is authenticated");
        return request.getRemoteUser();
    }

    /**
     * GET  /account : get the current user.
     *
     * @return the current user
     * @throws RuntimeException 500 (Internal Server Error) if the user couldn't be returned
     */
    @GetMapping("/account")
    @Timed
    public UserDTO getAccount() {
        return userService.getUserWithAuthorities()
            .map(UserDTO::new)
            .orElseThrow(() -> new InternalServerErrorException("User could not be found"));
    }

    /**
     * POST  /account : update the current user information.
     *
     * @param userDTO the current user information
     * @throws EmailAlreadyUsedException 400 (Bad Request) if the email is already used
     * @throws RuntimeException 500 (Internal Server Error) if the user login wasn't found
     */
    @PostMapping("/account")
    @Timed
    public void saveAccount(@Valid @RequestBody UserDTO userDTO) {
        final String userLogin = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new InternalServerErrorException("Current user login not found"));
        Optional<User> existingUser = userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
        if (existingUser.isPresent() && (!existingUser.get().getLogin().equalsIgnoreCase(userLogin))) {
            throw new EmailAlreadyUsedException();
        }
        Optional<User> user = userRepository.findOneByLogin(userLogin);
        if (!user.isPresent()) {
            throw new InternalServerErrorException("User could not be found");
        }
        userService.updateUser(userDTO.getFirstName(), userDTO.getLastName(), userDTO.getEmail(),
            userDTO.getLangKey(), userDTO.getImageUrl());
   }

    /**
     * POST  /account/change-password : changes the current user's password
     *
     * @param password the new password
     * @throws InvalidPasswordException 400 (Bad Request) if the new password is incorrect
     */
    @PostMapping(path = "/account/change-password")
    @Timed
    public void changePassword(@RequestBody String password) {
        if (!checkPasswordLength(password)) {
            throw new InvalidPasswordException();
        }
        userService.changePassword(password);
   }

    @GetMapping(path = "/account/userlogin")
    @Timed
    public void updatePassword(@RequestBody UpdatePasswordDTO updatePasswordDTO) {
    	if (!checkPasswordLength(updatePasswordDTO.getPassword())) {
    		throw new InvalidPasswordException();
    	}
    	 SecurityUtils.getCurrentUserLogin()
         .flatMap(userRepository::findOneByLogin)
         .ifPresent(user -> {
        	 System.out.println(user.getLogin());
        	 //verifyService.getVerifyCodeByPhone(phone);
         });
    	//currentUserLogin.flatMap(mapper);
    	//userService.changePassword(password);
    }

    /**
     * POST   /account/reset-password/init : Send an email to reset the password of the user
     *
     * @param mail the mail of the user
     * @throws EmailNotFoundException 400 (Bad Request) if the email address is not registered
     */
    @PostMapping(path = "/account/reset-password/init")
    @Timed
    public void requestPasswordReset(@RequestBody String mail) {
       mailService.sendPasswordResetMail(
           userService.requestPasswordReset(mail)
               .orElseThrow(EmailNotFoundException::new)
       );
    }

    /**
     * POST   /account/reset-password/finish : Finish to reset the password of the user
     *
     * @param keyAndPassword the generated key and the new password
     * @throws InvalidPasswordException 400 (Bad Request) if the password is incorrect
     * @throws RuntimeException 500 (Internal Server Error) if the password could not be reset
     */
    @PostMapping(path = "/account/reset-password/finish")
    @Timed
    public void finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) {
        if (!checkPasswordLength(keyAndPassword.getNewPassword())) {
            throw new InvalidPasswordException();
        }
        Optional<User> user =
            userService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey());

        if (!user.isPresent()) {
            throw new InternalServerErrorException("No user was found for this reset key");
        }
    }

    private static boolean checkPasswordLength(String password) {
        return !StringUtils.isEmpty(password) &&
            password.length() >= ManagedUserVM.PASSWORD_MIN_LENGTH &&
            password.length() <= ManagedUserVM.PASSWORD_MAX_LENGTH;
    }

    private static boolean checkVerifyCode(String verifyCode) {

        if (StringUtils.isEmpty(verifyCode)) return false;
//        return api.smsValidateUsingPOST();
        return true;
    }

}
