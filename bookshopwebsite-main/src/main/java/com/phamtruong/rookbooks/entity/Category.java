package com.phamtruong.rookbooks.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "categories")
@NoArgsConstructor
@AllArgsConstructor
public class Category extends AbstractBase {

    @Column(name = "name")
    private String name;

    @Lob
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "category",
            cascade = {CascadeType.ALL})
    private List<Book> bookList;

    // Getters and setters, validation annotations, and other fields as needed
}