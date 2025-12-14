package vn.hoidanit.jobhunter.service;

import java.security.Security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.request.outbound.ExchangeTokenRequest;
import vn.hoidanit.jobhunter.domain.response.ResLoginDTO;
import vn.hoidanit.jobhunter.domain.response.outbound.ExchangeTokenResponse;
import vn.hoidanit.jobhunter.repository.UserRepository;
import vn.hoidanit.jobhunter.repository.httpclient.OutboundIdentityClient;
import vn.hoidanit.jobhunter.repository.httpclient.OutboundUserClient;
import vn.hoidanit.jobhunter.util.SecurityUtil;

@Service
@Slf4j
public class AuthService {

    private final OutboundIdentityClient outboundIdentityClient;
    private final OutboundUserClient outboundUserClient;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final UserProfileService userProfileService;

    public AuthService(OutboundIdentityClient outboundIdentityClient, OutboundUserClient outboundUserClient,
            UserRepository userRepository, SecurityUtil securityUtil, UserProfileService userProfileService) {
        this.outboundIdentityClient = outboundIdentityClient;
        this.outboundUserClient = outboundUserClient;
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
        this.userProfileService = userProfileService;
    }

    @NonFinal
    @Value("${outbound.identity.client-id}")
    protected String CLIENT_ID;

    @NonFinal
    @Value("${outbound.identity.client-secret}")
    protected String CLIENT_SECRET;

    @NonFinal
    @Value("${outbound.identity.redirect-uri}")
    protected String REDIRECT_URI;

    @NonFinal
    protected final String GRANT_TYPE = "authorization_code";

    public ResLoginDTO outboundAuthenticate(String code) {
        var response = outboundIdentityClient.exchangeToken(
                ExchangeTokenRequest.builder()
                        .code(code)
                        .clientId(CLIENT_ID)
                        .clientSecret(CLIENT_SECRET)
                        .redirectUri(REDIRECT_URI)
                        .grantType(GRANT_TYPE)
                        .build());

        log.info("TOKEN RESPONSE {}", response);

        var userInfo = outboundUserClient.getUserInfo("json", response.getAccessToken());

        log.info("USER INFO RESPONSE {}", userInfo);

        // Kiểm tra user tồn tại
        var existingUser = userRepository.findByEmail(userInfo.getEmail());

        User user;

        if (existingUser == null) {
            // User chưa tồn tại → tạo mới
            user = userRepository.save(
                    User.builder()
                            .name(userInfo.getName())
                            .email(userInfo.getEmail())
                            .avatar(userInfo.getPicture())
                            .isGoogleAccount(true)
                            .password("") // có thể bỏ
                            .build());

            userProfileService.createUserProfileForUser(user);
        } else {
            // User đã tồn tại → dùng lại
            user = existingUser;
        }

        // Build response
        ResLoginDTO res = ResLoginDTO.builder()
                .user(
                        ResLoginDTO.UserLogin.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .name(user.getName())
                                .avatar(user.getAvatar())
                                .phoneNumber(user.getPhoneNumber())
                                .dateOfBirth(user.getDateOfBirth())
                                .gender(user.getGender())
                                .isUserGoogleAccount(user.isGoogleAccount())
                                .favoriteJobIds(user.getFavoriteJobIds())
                                .build())
                .build();

        // Tạo access token
        String accessToken = this.securityUtil.createAccessToken(user.getEmail(), res);
        res.setAccessToken(accessToken);

        log.info("RES LOGIN DTO: {}", res);

        return res;
    }
}
