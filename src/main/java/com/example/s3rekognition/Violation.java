package com.example.s3rekognition;

public class Violation {
    private int violationCount;
    private String bodyPart; 
    
    public int getViolationCount(){
        return violationCount;
    }
    public String getBodyPart(){
        return bodyPart;
    }
    
    public void setViolationCount(int violationCount){
        this.violationCount = violationCount;
    }
    public void setBodyPart(String bodyPart){
        this.bodyPart = bodyPart;
    }
}
