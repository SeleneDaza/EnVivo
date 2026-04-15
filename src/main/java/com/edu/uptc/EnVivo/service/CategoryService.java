package com.edu.uptc.EnVivo.service;

import com.edu.uptc.EnVivo.dto.CreateCategoryDTO;
import com.edu.uptc.EnVivo.entity.Category;
import com.edu.uptc.EnVivo.repository.CategoryRepository;
import com.edu.uptc.EnVivo.repository.EventRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    public Category saveCategory(CreateCategoryDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        return categoryRepository.save(category);
    }

    public Category updateCategory(Long id, CreateCategoryDTO dto) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoria no encontrada."));
        category.setName(dto.getName());
        return categoryRepository.save(category);
    }

    public CreateCategoryDTO getCategoryDTO(Long id) {
        Category cat = getById(id);
        CreateCategoryDTO dto = new CreateCategoryDTO();
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
        if (eventRepository.existsByCategory_CategoryId(id)) {
            throw new IllegalStateException("La categoria tiene eventos asociados");
        }
        categoryRepository.deleteById(id);
    }

    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
}
