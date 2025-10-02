package com.phamtruong.rookbooks.controller.common;

import com.phamtruong.rookbooks.entity.Category;
import com.phamtruong.rookbooks.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class HeaderController extends BaseController {

    @Autowired
    private CategoryService categoryService;

    @ModelAttribute("headerCategory")
    public List<Category> getCategories() {
        return categoryService.getAllCategories();
    }

}
