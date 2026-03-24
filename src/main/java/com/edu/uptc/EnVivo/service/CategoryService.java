package com.edu.uptc.EnVivo.service;

import com.edu.uptc.EnVivo.dto.CreateCategoryDTO;
import com.edu.uptc.EnVivo.entity.Category;
import com.edu.uptc.EnVivo.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public Category saveCategory(CreateCategoryDTO dto) {
        Category category = new Category();
        category.setCategoryId(dto.getCategoryId()); 
        category.setName(dto.getName());
        return categoryRepository.save(category);
    }

    public CreateCategoryDTO getCategoryDTO(Long id) {
        Category cat = getById(id);
        CreateCategoryDTO dto = new CreateCategoryDTO();
        dto.setCategoryId(cat.getCategoryId());
        dto.setName(cat.getName());
        return dto;
    }

    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    public Category getById(Long id) {
        return categoryRepository.findById(id).orElse(new Category());
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
