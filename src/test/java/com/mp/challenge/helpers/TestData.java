package com.mp.challenge.helpers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * TestData
 * <p>
 * TestData class.
 * <p>
 * This component was built following my personal development standards.
 * The code presented here is protected by intellectual property laws and copyrights.
 *
 * @author Jose Quiroga
 * @since 14/10/2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestData {
    private String name;
    private int value;
    private LocalDateTime timestamp;
}
