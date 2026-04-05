package com.edu.uptc.EnVivo.controller;

import com.edu.uptc.EnVivo.dto.CreateCategoryDTO;
import com.edu.uptc.EnVivo.entity.Category;
import com.edu.uptc.EnVivo.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    private void cargarListaCategorias(Model model) {
        model.addAttribute("categorias", categoryService.getCategories());
    }

    @GetMapping
    public String listCategories(Model model) {
        cargarListaCategorias(model);
        model.addAttribute("categoria", new CreateCategoryDTO()); 
        return "categories"; 
    }

    @GetMapping("/edit/{id}")
    public String editCategory(@PathVariable Long id, Model model) {
        CreateCategoryDTO dto = categoryService.getCategoryDTO(id);
        cargarListaCategorias(model);
        model.addAttribute("categoria", dto); 
        return "categories"; 
    }

    @PostMapping("/create")
    public String saveCategory(@ModelAttribute("categoria") CreateCategoryDTO dto) {
        try {
            categoryService.saveCategory(dto);
            return "redirect:/categories?exito";
        } catch (Exception e) {
            return "redirect:/categories?error_general";
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