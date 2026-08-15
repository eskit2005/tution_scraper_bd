package com.tuition.app.service;

import com.tuition.app.dto.TuitionMapper;
import com.tuition.app.dto.TuitionPostDto;
import com.tuition.app.entity.TuitionPost;
import com.tuition.app.repository.TuitionPostRepository;
import com.tuition.app.repository.TuitionPostSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TuitionService {

    private final TuitionPostRepository tuitionPostRepository;
    private final TuitionMapper tuitionMapper;

    public TuitionService(TuitionPostRepository tuitionPostRepository, TuitionMapper tuitionMapper) {
        this.tuitionPostRepository = tuitionPostRepository;
        this.tuitionMapper = tuitionMapper;
    }

    public List<TuitionPostDto> getAllTuitions(String location, String salary, String classLevel, String subject) {
        Specification<TuitionPost> spec = Specification.where(null);

        if (location != null && !location.isEmpty()) {
            spec = spec.and(TuitionPostSpecification.hasLocation(location));
        }
        if (salary != null && !salary.isEmpty()) {
            spec = spec.and(TuitionPostSpecification.hasSalary(salary));
        }
        if (classLevel != null && !classLevel.isEmpty()) {
            spec = spec.and(TuitionPostSpecification.hasClassLevel(classLevel));
        }
        if (subject != null && !subject.isEmpty()) {
            spec = spec.and(TuitionPostSpecification.hasSubject(subject));
        }

        List<TuitionPost> posts = tuitionPostRepository.findAll(spec);
        return tuitionMapper.toDtoList(posts);
    }

    public String ingestTuitions(List<TuitionPostDto> postDtos) {
        int newlySaved = 0;
        int duplicates = 0;

        for (TuitionPostDto dto : postDtos) {
            if (tuitionPostRepository.existsById(dto.getCompositeKey())) {
                duplicates++;
            } else {
                TuitionPost post = tuitionMapper.toEntity(dto);
                tuitionPostRepository.save(post);
                newlySaved++;
            }
        }
        return "Received: " + postDtos.size() + ", Saved: " + newlySaved + ", Duplicates Ignored: " + duplicates;
    }

    public List<String> getRecentFacebookPostIds(int days) {
        if (days <= 0 || days > 30) {
            return tuitionPostRepository.findAllDistinctFacebookPostIds();
        }
        java.time.LocalDateTime since = java.time.LocalDateTime.now().minusDays(days);
        return tuitionPostRepository.findFacebookPostIdsScrapedSince(since);
    }
}
