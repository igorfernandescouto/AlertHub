package com.alerthub.rule.domain;

import com.fasterxml.jackson.databind.JsonNode;

public enum RuleOperator {
    ANY {
        @Override
        public boolean matches(JsonNode actualValue, String expectedValue) {
            return true;
        }
    },
    EQUALS {
        @Override
        public boolean matches(JsonNode actualValue, String expectedValue) {
            return actualValue != null && expectedValue != null && expectedValue.equals(actualValue.asText());
        }
    },
    CONTAINS {
        @Override
        public boolean matches(JsonNode actualValue, String expectedValue) {
            return actualValue != null && expectedValue != null && actualValue.asText().contains(expectedValue);
        }
    },
    GREATER_THAN {
        @Override
        public boolean matches(JsonNode actualValue, String expectedValue) {
            return compareNumbers(actualValue, expectedValue) > 0;
        }
    },
    LESS_THAN {
        @Override
        public boolean matches(JsonNode actualValue, String expectedValue) {
            return compareNumbers(actualValue, expectedValue) < 0;
        }
    };

    public abstract boolean matches(JsonNode actualValue, String expectedValue);

    protected static int compareNumbers(JsonNode actualValue, String expectedValue) {
        if (actualValue == null || expectedValue == null) {
            return Integer.MIN_VALUE;
        }
        double left = actualValue.asDouble(Double.NaN);
        if (Double.isNaN(left)) {
            return Integer.MIN_VALUE;
        }
        double right = Double.parseDouble(expectedValue);
        return Double.compare(left, right);
    }
}
