package edu.javacourse.studentorder.domain;

public class University {
    private Long universityId;
    private String universityName;

    public University(Long universityId, String universityName) {
        this.universityId = universityId;
        this.universityName = universityName;
    }

    public University() {
    }

    public Long getUniversityId() {
        return universityId;
    }

    public void setUniversityId(Long universityId) {
        this.universityId = universityId;
    }

    public String getUniversityName() {
        return universityName;
    }

    public void setUniversityName(String universityName) {
        this.universityName = universityName;
    }
}
