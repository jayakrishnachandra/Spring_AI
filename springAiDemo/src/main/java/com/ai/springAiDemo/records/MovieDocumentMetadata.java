package com.ai.springAiDemo.records;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public  class MovieDocumentMetadata
{
    @NotBlank(message = "Title is required and cannot be blank")
    String title;

    List<@NotBlank(message = "Cast information is required")String> cast;

    @NotBlank(message = "Movie name must be provided")
    String movie;

    @NotNull(message = "Genre must be specified")
    Genre genre;

    @NotNull(message = "Category must be specified")
    Category category;

    @NotBlank(message = "Language is required")
    String language;

    @NotBlank(message = "Director name must be provided")
    String director;

    @Min(value = 1900, message = "Year must be no earlier than 1900")
    @Max(value = 2100, message = "Year must be no later than 2100")
    int year;

    @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
    @DecimalMax(value = "10.0", message = "Rating must be at most 10.0")
    double rating;

    @Positive(message = "Duration must be a positive number of minutes")
    int durationMinutes;

    List<@NotBlank(message = "Tags cannot contain empty values") String> tags;
}
