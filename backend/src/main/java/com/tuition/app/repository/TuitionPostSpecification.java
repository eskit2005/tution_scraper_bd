package com.tuition.app.repository;

import com.tuition.app.entity.TuitionPost;
import org.springframework.data.jpa.domain.Specification;

public class TuitionPostSpecification {

    public static Specification<TuitionPost> hasLocation(String location) {
        return (root, query, builder) -> 
            location == null ? null : builder.like(builder.lower(root.get("location")), "%" + location.toLowerCase() + "%");
    }

    public static Specification<TuitionPost> hasSalary(String salary) {
        return (root, query, builder) -> 
            salary == null ? null : builder.like(builder.lower(root.get("salary")), "%" + salary.toLowerCase() + "%");
    }

    public static Specification<TuitionPost> hasClassLevel(String classLevel) {
        return (root, query, builder) -> 
            classLevel == null ? null : builder.like(builder.lower(root.get("classLevel")), "%" + classLevel.toLowerCase() + "%");
    }

    public static Specification<TuitionPost> hasSubject(String subject) {
        return (root, query, builder) -> 
            subject == null ? null : builder.like(builder.lower(root.get("subject")), "%" + subject.toLowerCase() + "%");
    }
}
