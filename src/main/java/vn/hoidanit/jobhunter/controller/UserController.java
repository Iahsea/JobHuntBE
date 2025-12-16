package vn.hoidanit.jobhunter.controller;

import java.util.Arrays;
import java.util.List;

import org.hibernate.query.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;


//import com.mysql.cj.x.protobuf.MysqlxCrud.Update;
import com.turkraft.springfilter.boot.Filter;

import feign.Response;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import vn.hoidanit.jobhunter.domain.MyCv;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.request.AdminUpdateUserRequest;
import vn.hoidanit.jobhunter.domain.request.ChangePasswordRequest;
import vn.hoidanit.jobhunter.domain.response.ResCreateUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResUpdateUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResUserDTO;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.repository.JobRepository;
import vn.hoidanit.jobhunter.service.FileService;
import vn.hoidanit.jobhunter.service.JobService;
import vn.hoidanit.jobhunter.service.UserService;
import vn.hoidanit.jobhunter.specification.UserSpecification;
import vn.hoidanit.jobhunter.util.annotation.ApiMessage;
import vn.hoidanit.jobhunter.util.error.IdInvalidException;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class UserController {

    private final JobService jobService;

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    private final FileService fileService;

    private final vn.hoidanit.jobhunter.service.MyCvService myCvService;

    public UserController(UserService userService, PasswordEncoder passwordEncoder, FileService fileService,
            vn.hoidanit.jobhunter.service.MyCvService myCvService, JobService jobService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.fileService = fileService;
        this.myCvService = myCvService;
        this.jobService = jobService;
    }

    @PostMapping("/users")
    @ApiMessage("Create a new user")
    public ResponseEntity<ResCreateUserDTO> createNewUser(@Valid @RequestBody User postManUser)
            throws IdInvalidException {
        boolean isEmailExist = this.userService.isEmailExist(postManUser.getEmail());
        if (isEmailExist) {
            throw new IdInvalidException(
                    "Email " + postManUser.getEmail() + "đã tồn tại, vui lòng sử dụng email khác.");
        }

        String hashPassword = this.passwordEncoder.encode(postManUser.getPassword());
        postManUser.setPassword(hashPassword);
        User ericUser = this.userService.handleCreateUser(postManUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.convertToResCreateUserDTO(ericUser));
    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Delete a user")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") long id)
            throws IdInvalidException {
        User currentUser = this.userService.fetchUserById(id);
        if (currentUser == null) {
            throw new IdInvalidException("User với id = " + id + " không tồn tại");
        }

        this.userService.handleDeleteUser(id);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/users/{id}")
    @ApiMessage("fetch user by id")
    public ResponseEntity<ResUserDTO> getUserById(@PathVariable("id") long id) throws IdInvalidException {
        User fetchUser = this.userService.fetchUserById(id);
        if (fetchUser == null) {
            throw new IdInvalidException("User với id = " + id + " không tồn tại");
        }

        return ResponseEntity.status(HttpStatus.OK)
                .body(this.userService.convertToResUserDTO(fetchUser));
    }

    // fetch all users
    @GetMapping("/users")
    @ApiMessage("fetch all users")
    public ResponseEntity<ResultPaginationDTO> getAllUser(
            @Filter Specification<User> spec,
            @RequestParam(required = false) Long roleId,
            Pageable pageable) {

        // nếu spec từ @Filter null thì tạo where(null)
        Specification<User> finalSpec = (spec == null)
                ? Specification.where(null)
                : spec;

        // ghép thêm điều kiện theo roleId (nếu có truyền)
        finalSpec = finalSpec.and(UserSpecification.hasRoleId(roleId));


        return ResponseEntity.status(HttpStatus.OK).body(
                this.userService.fetchAllUser(finalSpec, pageable));
    }

    @PutMapping(value = "/users/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiMessage("Update a user")
    public ResponseEntity<?> updateUser(
            @PathVariable("id") Long id,
            @RequestPart(value = "user", required = false) User updatedUserDTO,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) throws Exception {

        // lưu file
        String filename = null;
        try {
            if (avatar != null && !avatar.isEmpty()) {
                if (avatar.getSize() > 10 * 1024 * 1024) { // >10MB
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                            .body("File size exceeds the maximum limit of 10MB");
                }

                String fileName = avatar.getOriginalFilename();
                List<String> allowedExtensions = Arrays.asList("pdf", "jpg", "jpeg", "png", "doc", "docx");
                boolean isValidExtension = allowedExtensions.stream()
                        .anyMatch(ext -> fileName.toLowerCase().endsWith("." + ext));

                if (!isValidExtension) {
                    throw new Exception("Invalid file extension. Only allow " + allowedExtensions.toString());
                }

                filename = this.fileService.storeFile(avatar, "avatar");
            }

            log.info("FileName: {}", filename);
            updatedUserDTO.setAvatar(filename);
            User updatedUser = userService.handleUpdateUser(id, updatedUserDTO);

            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/users/{id}/change-password")
    @ApiMessage("Change user password")
    public ResponseEntity<String> changeUserPassword(
            @PathVariable("id") Long id,
            @RequestBody ChangePasswordRequest changePasswordRequest) throws IdInvalidException {

        log.info("Change password request: {}", changePasswordRequest.getCurrentPassword());
        log.info("Change password request: {}", changePasswordRequest.getNewPassword());

        User currentUser = this.userService.fetchUserById(id);
        if (currentUser == null) {
            throw new IdInvalidException("User với id = " + id + " không tồn tại");
        }

        // kiểm tra mật khẩu cũ
        boolean isMatch = this.passwordEncoder.matches(changePasswordRequest.getCurrentPassword(),
                currentUser.getPassword());
        if (!isMatch) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Current password is incorrect");
        }

        // cập nhật mật khẩu mới
        String hashedNewPassword = this.passwordEncoder.encode(changePasswordRequest.getNewPassword());
        currentUser.setPassword(hashedNewPassword);
        this.userService.handleUpdateUser(id, currentUser);

        return ResponseEntity.ok("Đổi mật khẩu thành công");
    }

    @GetMapping("/users/{id}/my-cv")
    public ResponseEntity<List<MyCv>> getAllMyCv(@PathVariable("id") Long id) {
        User user = this.userService.fetchUserById(id);
        List<MyCv> myCvs = this.myCvService.getMyCvsByUser(user);
        return ResponseEntity.ok(myCvs);
    }

    // post favorite job ids
    @PatchMapping("/users/favorite-job/{id}")
    public ResponseEntity<String> postFavoriteJobIds(
            @PathVariable("id") Long id) throws IdInvalidException {

        this.userService.handleAddFavoriteJobIds(id);

        return ResponseEntity.ok("Cập nhật thành công");
    }

    @GetMapping("/users/favorite-job")
    public ResponseEntity<ResultPaginationDTO> getMethodName(Pageable pageable) {

        return ResponseEntity.ok(
                this.jobService.getAllFavoriteJobUser(pageable));
    }


    @PutMapping("/admin/users/{id}")
    public ResponseEntity<?> updateUserByAdmin(
            @PathVariable Long id,
            @RequestBody AdminUpdateUserRequest req
    ) {
        User updated = userService.handleUpdateUserByAdmin(id, req);
        return ResponseEntity.ok(updated); // tốt hơn: trả UserResponseDTO
    }


}
