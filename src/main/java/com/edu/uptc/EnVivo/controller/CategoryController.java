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

    @GetMapping
    public String listarCategorias(Model model) {
        model.addAttribute("categorias", categoryService.getCategories());
        model.addAttribute("categoria", new CreateCategoryDTO()); 
        return "categories"; 
    }

    @GetMapping("/edit/{id}")
    public String prepararEditar(@PathVariable Long id, Model model) {
        Category cat = categoryService.getById(id);
        CreateCategoryDTO dto = new CreateCategoryDTO();
        dto.setCategoryId(cat.getCategoryId());
        dto.setName(cat.getName());

        model.addAttribute("categorias", categoryService.getCategories());
        model.addAttribute("categoria", dto); 
        return "categories"; 
    }

    @PostMapping("/create")
    public String guardarCategoria(@ModelAttribute("categoria") CreateCategoryDTO dto) {
        categoryService.saveCategory(dto);
        return "redirect:/categories";
    }

    @GetMapping("/delete/{id}")
    public String eliminarCategoria(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/categories";
    }
}