package vn.hoidanit.jobhunter.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import vn.hoidanit.jobhunter.domain.MyCv;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.request.MyCvRequest;
import vn.hoidanit.jobhunter.repository.MyCvRepository;

@Service
@RequiredArgsConstructor
public class MyCvService {

    private final MyCvRepository myCvRepository;

    public void saveMyCv(MyCvRequest myCv, User user) {
        MyCv cv = new MyCv();
        cv.setName(myCv.getName());
        cv.setUrl(myCv.getUrl());
        cv.setSize(myCv.getSize());
        cv.setUser(user);
        myCvRepository.save(cv);
    }

    public void deleteMyCv(Long id) {
        myCvRepository.deleteById(id);
    }

    public List<MyCv> getMyCvsByUser(User user) {
        return this.myCvRepository.findByUser(user);
    }

}
