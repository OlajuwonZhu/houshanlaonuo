package com.senol.dto;

public class EventSignupRequest {
    private String answers; // JSON string
    private String note;

    public String getAnswers() { return answers; }
    public void setAnswers(String answers) { this.answers = answers; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
