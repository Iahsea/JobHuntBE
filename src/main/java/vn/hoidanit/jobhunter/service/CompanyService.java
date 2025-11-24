package vn.hoidanit.jobhunter.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import vn.hoidanit.jobhunter.domain.Company;
import vn.hoidanit.jobhunter.domain.User;
import vn.hoidanit.jobhunter.domain.Job;
import vn.hoidanit.jobhunter.domain.response.ResultPaginationDTO;
import vn.hoidanit.jobhunter.domain.response.company.CompanyDetailResponse;
import vn.hoidanit.jobhunter.domain.response.job.JobSummaryResponse;
import vn.hoidanit.jobhunter.repository.CompanyRepository;
import vn.hoidanit.jobhunter.repository.UserRepository;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public CompanyService(
            CompanyRepository companyRepository,
            UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    public Company handleCreateCompany(Company c) {
        return this.companyRepository.save(c);
    }

    public ResultPaginationDTO handleGetCompany(Specification<Company> spec, Pageable pageable) {
        Page<Company> pCompany = this.companyRepository.findAll(spec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pCompany.getTotalPages());
        mt.setTotal(pCompany.getTotalElements());

        rs.setMeta(mt);
        rs.setResult(pCompany.getContent());
        return rs;
    }

    public Company handleUpdateCompany(Company c) {
        Optional<Company> companyOptional = this.companyRepository.findById(c.getId());
        if (companyOptional.isPresent()) {
            Company currentCompany = companyOptional.get();
            currentCompany.setLogo(c.getLogo());
            currentCompany.setName(c.getName());
            currentCompany.setDescription(c.getDescription());
            currentCompany.setAddress(c.getAddress());
            return this.companyRepository.save(currentCompany);
        }
        return null;
    }

    public void handleDeleteCompany(long id) {
        Optional<Company> comOptional = this.companyRepository.findById(id);
        if (comOptional.isPresent()) {
            Company com = comOptional.get();
            // fetch all user belong to this company
            List<User> users = this.userRepository.findByCompany(com);
            this.userRepository.deleteAll(users);
        }

        this.companyRepository.deleteById(id);
    }

    public Optional<Company> findById(long id) {
        return this.companyRepository.findById(id);
    }

    public CompanyDetailResponse getCompanyDetail(long id) {
        Company company = companyRepository.findByIdWithJobs(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        return convertToCompanyDetailResponse(company);
    }

    private CompanyDetailResponse convertToCompanyDetailResponse(Company company) {
        CompanyDetailResponse dto = new CompanyDetailResponse();

        dto.setId(company.getId());
        dto.setName(company.getName());
        dto.setDescription(company.getDescription());
        dto.setAddress(company.getAddress());
        dto.setLogo(company.getLogo());
        dto.setCoverImage(company.getCoverImage());
        dto.setWebsite(company.getWebsite());
        dto.setCompanySize(company.getCompanySize());
        dto.setFoundedDate(company.getFoundedDate());
        dto.setEmployeeCount(company.getEmployeeCount());
        dto.setBenefits(company.getBenefits());
        dto.setSocialLinks(company.getSocialLinks());

        // chỉ lấy job active
        List<JobSummaryResponse> jobDTOs = company.getJobs()
                .stream()
                .filter(Job::isActive)
                .map(job -> {
                    JobSummaryResponse j = new JobSummaryResponse();
                    j.setId(job.getId());
                    j.setName(job.getName());
                    j.setLocation(job.getLocation());
                    j.setSalary(job.getSalary());
                    j.setQuantity(job.getQuantity());
                    j.setLevel(job.getLevel());
                    j.setActive(job.isActive());
                    return j;
                })
                .toList();

        dto.setJobs(jobDTOs);

        return dto;
    }
}
