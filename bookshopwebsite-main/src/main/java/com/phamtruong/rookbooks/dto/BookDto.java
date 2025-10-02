package com.phamtruong.rookbooks.dto;

import com.phamtruong.rookbooks.entity.Book;
import lombok.Data;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link Book}
 */
@Data
@Value
public class BookDto implements Serializable {
    String title;
    Double totalRevenue;

}