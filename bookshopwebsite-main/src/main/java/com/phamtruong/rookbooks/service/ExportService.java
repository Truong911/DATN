package com.phamtruong.rookbooks.service;

import com.phamtruong.rookbooks.dto.BookDto;
import com.phamtruong.rookbooks.dto.CategoryDto;
import com.phamtruong.rookbooks.dto.OrderDTO;
import com.phamtruong.rookbooks.entity.User;

import java.util.List;

public interface ExportService {

    String exportOrderReport(User user, List<OrderDTO> orderDTOList, String keyword);

    String exportCategoryReport(User user, List<CategoryDto> categoryDTOList, String keyword);

    String exportBookReport(User user, List<BookDto> bookDtoList, String keyword);

}
