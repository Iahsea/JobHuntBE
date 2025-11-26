package vn.hoidanit.jobhunter.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.scheduling.annotation.Scheduled;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.service.EmailService;
import vn.hoidanit.jobhunter.service.SubscriberService;
import vn.hoidanit.jobhunter.service.OtpService;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class EmailController {

    private final EmailService emailService;
    private final SubscriberService subscriberService;
    private final OtpService otpService;
    private final UserService userService;

    public EmailController(EmailService emailService,
            SubscriberService subscriberService,
            OtpService otpService,
            UserService userService) {
        this.emailService = emailService;
        this.subscriberService = subscriberService;
        this.otpService = otpService;
        this.userService = userService;
    }

    @GetMapping("/email")
    @ApiMessage("Send simple email")
    // @Scheduled(cron = "*/30 * * * * *")
    // @Transactional
    public String sendSimpleEmail() {
        // this.emailService.sendSimpleEmail();
        // this.emailService.sendEmailSync("ads.hoidanit@gmail.com", "test send email",
        // "<h1> <b> hello </b> </h1>", false,
        // true);
        // this.emailService.sendEmailFromTemplateSync("ads.hoidanit@gmail.com", "test
        // send email", "job");
        this.subscriberService.sendSubscribersEmailJobs();
        return "ok";
    }

    @PostMapping("/email/resend-otp")
    @ApiMessage("Send OTP to email for registration")
    public ResponseEntity<String> sendOtpForRegister(
            @RequestParam("email") String email,
            @RequestParam("name") String name) {
        if (email == null || email.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is required");
        }

        // User user = this.userService.handleGetUserByUsername(email);
        // if (user != null && user.isVerified()) {
        // return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already
        // registered");
        // }

        // create otp valid for 5 minutes
        var otp = this.otpService.createOtpForEmail(email, 5);

        String subject = "Mã OTP đăng ký - JobHunter";
        String content = otp.getCode();
        this.emailService.sendEmailFromTemplateSync(email, subject, "otp", name, content);

        return ResponseEntity.ok("OTP sent");
    }

    @PostMapping("/email/verify-otp")
    @ApiMessage("Verify OTP for registration")
    public ResponseEntity<String> verifyOtp(@RequestParam("email") String email, @RequestParam("code") String code)
            throws IdInvalidException {

        log.info(">>>>>>>>>Verifying OTP for email: {}", email);
        log.info("OTP code provided: {}", code);
        if (email == null || email.isBlank() || code == null || code.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email and code are required");
        }

        boolean valid = this.otpService.validateOtp(email, code);
        if (!valid) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP");
        }

        log.info("valid {}", valid);

        // mark user as verified
        this.userService.verifyUserByEmail(email);

        return ResponseEntity.ok("OTP verified");
    }
}
