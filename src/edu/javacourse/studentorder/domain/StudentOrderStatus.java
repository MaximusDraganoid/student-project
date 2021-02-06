package edu.javacourse.studentorder.domain;

public enum StudentOrderStatus {
    START, CHECKED;

    public static StudentOrderStatus fromValue(int value) {
        for (StudentOrderStatus status : StudentOrderStatus.values()) {
            if (status.ordinal() == value)
                return status;
        }
        throw new RuntimeException("Unknown value: " + value);
    }
}
