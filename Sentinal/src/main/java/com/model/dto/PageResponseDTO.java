package com.model.dto;

import java.util.List;

import lombok.Data;

@Data
public class PageResponseDTO<T> {
    private List<T> content;
    private int totalPages;
    private long totalElements;
    private int number;
    private int size;
}
