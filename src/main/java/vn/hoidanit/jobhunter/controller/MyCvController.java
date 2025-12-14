package vn.hoidanit.jobhunter.controller;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.request.MyCvRequest;
import vn.hoidanit.jobhunter.service.FileService;
import vn.hoidanit.jobhunter.service.MyCvService;
import vn.hoidanit.jobhunter.service.UserService;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class MyCvController {

    private final MyCvService myCvService;
    private final UserService userService;
    private final FileService fileService;

    @PostMapping("/my-cv")
    public ResponseEntity<String> createMyCv(
            @RequestPart("myCv") MyCvRequest myCv,
            @RequestPart("cv") MultipartFile file) {

        String filename = null;
        try {
            if (file != null && !file.isEmpty()) {
                if (file.getSize() > 10 * 1024 * 1024) { // >10MB
                    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                            .body("File size exceeds the maximum limit of 10MB");
                }

                String fileName = file.getOriginalFilename();
                List<String> allowedExtensions = Arrays.asList("pdf", "jpg", "jpeg", "png", "doc", "docx");
                boolean isValidExtension = allowedExtensions.stream()
                        .anyMatch(ext -> fileName.toLowerCase().endsWith("." + ext));

                if (!isValidExtension) {
                    throw new Exception("Invalid file extension. Only allow " + allowedExtensions.toString());
                }

                filename = this.fileService.store(file, "resume");
            }

            myCv.setUrl(filename);
            User user = userService.fetchUserById(myCv.getUserId());
            myCvService.saveMyCv(myCv, user);

            return ResponseEntity.ok("ok");
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/my-cv/{id}")
    public ResponseEntity<String> deleteMyCv(@PathVariable("id") Long id) {
        myCvService.deleteMyCv(id);
        return ResponseEntity.ok("ok");
    }

}
