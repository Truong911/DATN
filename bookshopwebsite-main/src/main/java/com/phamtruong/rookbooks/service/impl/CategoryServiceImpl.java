package com.phamtruong.rookbooks.service.impl;

import com.phamtruong.rookbooks.dto.CategoryDto;
import com.phamtruong.rookbooks.entity.Category;
import com.phamtruong.rookbooks.repository.BookRepository;
import com.phamtruong.rookbooks.service.CategoryService;
import com.phamtruong.rookbooks.repository.CategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    @Override
    public Page<Category> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public List<Category> getAllCategoriesForShop() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(c -> new Category(
                        c.getName(),
                        c.getDescription(),
                        bookRepository.findAllByCategoryAndActiveFlag(c, true)
                ))
                .collect(Collectors.toList());
    }


    @Override
    public Category getCategoryById(Long categoryId) {
        Optional<Category> categoryOptional = categoryRepository.findById(categoryId);
        return categoryOptional.orElse(null);
    }

    @Override
    public void addCategory(Category category) {
        categoryRepository.save(category);
    }

    @Override
    public void updateCategory(Long categoryId, Category updatedCategory) {
        Category existingCategory = getCategoryById(categoryId);
        if (existingCategory != null) {
            existingCategory.setName(updatedCategory.getName());
            existingCategory.setDescription(updatedCategory.getDescription());
            categoryRepository.save(existingCategory);
        }
    }

    @Override
    public void deleteCategory(Long categoryId) {
        Category category = getCategoryById(categoryId);
        if (category != null) {
            categoryRepository.delete(category);
        }
    }

    @Override
    public List<CategoryDto> getTop10BestSellerByMonth(int month) {
        List<Object[]> result = categoryRepository.findTop10BestSellerByMonth(month);
        List<CategoryDto> resultConvertedToDto = new ArrayList<>();
        for (Object[] item : result) {
            resultConvertedToDto.add(new CategoryDto(item[0].toString(), Double.parseDouble(item[1].toString())));
        }
        return resultConvertedToDto;
    }
}
