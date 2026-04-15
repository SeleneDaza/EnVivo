package com.edu.uptc.EnVivo.controller;

import com.edu.uptc.EnVivo.dto.CreateCategoryDTO;
import com.edu.uptc.EnVivo.entity.Category;
import com.edu.uptc.EnVivo.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryController.class);
    private final CategoryService categoryService;

    private void getCategoriesList(Model model) {
        model.addAttribute("categorias", categoryService.getCategories());
    }

    @GetMapping
    public String listCategories(Model model) {
        getCategoriesList(model);
        model.addAttribute("categoria", new CreateCategoryDTO());
        model.addAttribute("isEditMode", false);
        model.addAttribute("editingCategoryId", null);
        return "categories";
    }

    @GetMapping("/edit/{id}")
    public String editCategory(@PathVariable Long id, Model model) {
        CreateCategoryDTO dto = categoryService.getCategoryDTO(id);
        getCategoriesList(model);
        model.addAttribute("categoria", dto);
        model.addAttribute("isEditMode", true);
        model.addAttribute("editingCategoryId", id);
        return "categories";
    }

    @PostMapping("/create")
    public String saveCategory(@ModelAttribute("categoria") CreateCategoryDTO dto) {
        try {
            if (categoryService.existsByName(dto.getName())) {
                return "redirect:/categories?error_duplicado=true";
            }
            categoryService.saveCategory(dto);
            return "redirect:/categories?exito=true";
        } catch (Exception e) {
            LOGGER.error("Error saving category", e);
            return "redirect:/categories?error_general=true";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateCategory(@PathVariable Long id,
                                 @ModelAttribute("categoria") CreateCategoryDTO dto) {
        try {
            if (categoryService.existsByName(dto.getName())) {
                Category existingCategory = categoryService.getById(id);
                if (existingCategory.getCategoryId() == null
                        || !dto.getName().equals(existingCategory.getName())) {
                    return "redirect:/categories?error_duplicado=true";
                }
            }
            categoryService.updateCategory(id, dto);
            return "redirect:/categories?exito=true";
        } catch (Exception e) {
            LOGGER.error("Error updating category", e);
            return "redirect:/categories?error_general=true";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return "redirect:/categories?exito";

        } catch (IllegalStateException e) {
            return "redirect:/categories?error_en_uso";

        } catch (Exception e) {
            return "redirect:/categories?error_general";
        }
    }
}