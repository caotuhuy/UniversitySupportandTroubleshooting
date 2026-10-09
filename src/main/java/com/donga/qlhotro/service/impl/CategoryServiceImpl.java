package com.donga.qlhotro.service.impl;

import com.donga.qlhotro.dto.CategoryRequestDTO;
import com.donga.qlhotro.dto.CategoryResponseDTO;
import com.donga.qlhotro.entity.Category;
import com.donga.qlhotro.enums.CategoryStatus;
import com.donga.qlhotro.exception.ResourceNotFoundException;
import com.donga.qlhotro.repository.CategoryRepository;
import com.donga.qlhotro.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", id));
        return mapToResponseDTO(category);
    }

    @Override
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setStatus(request.getStatus() != null ? request.getStatus() : CategoryStatus.ACTIVE);

        Category saved = categoryRepository.save(category);
        return mapToResponseDTO(saved);
    }

    @Override
    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục", "id", id));

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            category.setStatus(request.getStatus());
        }

        Category updated = categoryRepository.save(category);
        return mapToResponseDTO(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Danh mục", "id", id);
        }
        categoryRepository.deleteById(id);
    }

    private CategoryResponseDTO mapToResponseDTO(Category category) {
        CategoryResponseDTO dto = new CategoryResponseDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setStatus(category.getStatus());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        return dto;
    }
}
