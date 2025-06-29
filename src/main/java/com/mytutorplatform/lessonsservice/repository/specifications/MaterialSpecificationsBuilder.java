package com.mytutorplatform.lessonsservice.repository.specifications;

import com.mytutorplatform.lessonsservice.model.Material;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MaterialSpecificationsBuilder {

    private final List<Specification<Material>> specifications = new ArrayList<>();

    public MaterialSpecificationsBuilder withFolderId(UUID folderId) {
        if (folderId != null) {
            specifications.add((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("folderId"), folderId));
        }
        return this;
    }

    public MaterialSpecificationsBuilder withSearch(String search) {
        if (search != null && !search.isEmpty()) {
            specifications.add((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + search.toLowerCase() + "%"));
        }
        return this;
    }

    public MaterialSpecificationsBuilder withType(String type) {
        if (type != null && !type.isEmpty()) {
            specifications.add((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("type"), type));
        }
        return this;
    }

    public MaterialSpecificationsBuilder withTags(List<String> tags) {
        if (tags != null && !tags.isEmpty()) {
            specifications.add((root, query, criteriaBuilder) -> {
                query.distinct(true);
                return root.join("tags").in(tags);
            });
        }
        return this;
    }

    public Specification<Material> build() {
        if (specifications.isEmpty()) {
            return null;
        }

        Specification<Material> result = specifications.get(0);
        for (int i = 1; i < specifications.size(); i++) {
            result = Specification.where(result).and(specifications.get(i));
        }
        return result;
    }
}