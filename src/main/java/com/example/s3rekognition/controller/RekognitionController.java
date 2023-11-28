package com.example.s3rekognition.controller;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.MeterRegistry;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.s3rekognition.PPEClassificationResponse;
import com.example.s3rekognition.PPEResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


@RestController
public class RekognitionController implements ApplicationListener<ApplicationReadyEvent> {

    private final AmazonS3 s3Client;
    private final AmazonRekognition rekognitionClient;
    private MeterRegistry meterRegistry;

    private static final Logger logger = Logger.getLogger(RekognitionController.class.getName());

    @Autowired
    public RekognitionController(MeterRegistry meterRegistry) {
        this.s3Client = AmazonS3ClientBuilder.standard().build();
        this.rekognitionClient = AmazonRekognitionClientBuilder.standard().build();
        this.meterRegistry = meterRegistry;
    }
    int violationCount;
    boolean violationBool;
    /**
     * This endpoint takes an S3 bucket name in as an argument, scans all the
     * Files in the bucket for Protective Gear Violations.
     * <p>
     *
     * @param bucketName
     * @return
     */
    @GetMapping(value = "/scan-ppe", consumes = "*/*", produces = "application/json")
    @ResponseBody
    public ResponseEntity<PPEResponse> scanForPPE(@RequestParam String bucketName) {
        // List all objects in the S3 bucket
        ListObjectsV2Result imageList = s3Client.listObjectsV2(bucketName);

        // This will hold all of our classifications
        List<PPEClassificationResponse> classificationResponses = new ArrayList<>();

        // This is all the images in the bucket
        List<S3ObjectSummary> images = imageList.getObjectSummaries();

        // Iterate over each object and scan for PPE
        for (S3ObjectSummary image : images) {
            logger.info("scanning " + image.getKey());

            // This is where the magic happens, use AWS rekognition to detect PPE
            DetectProtectiveEquipmentRequest request = new DetectProtectiveEquipmentRequest()
                    .withImage(new Image()
                            .withS3Object(new S3Object()
                                    .withBucket(bucketName)
                                    .withName(image.getKey())))
                    .withSummarizationAttributes(new ProtectiveEquipmentSummarizationAttributes()
                            .withMinConfidence(80f)
                            .withRequiredEquipmentTypes("FACE_COVER"));

            DetectProtectiveEquipmentResult result = rekognitionClient.detectProtectiveEquipment(request);

            // If any person on an image lacks PPE on the face, it's a violation of regulations
            boolean violation = isViolation(result);

            logger.info("scanning " + image.getKey() + ", violation result " + violation);
            // Categorize the current image as a violation or not.
            int personCount = result.getPersons().size();
            PPEClassificationResponse classification = new PPEClassificationResponse(image.getKey(), personCount, violation);
            classificationResponses.add(classification);
        }
        PPEResponse ppeResponse = new PPEResponse(bucketName, classificationResponses);
        return ResponseEntity.ok(ppeResponse);
    }
//###################################################################################################################################
    public double facePPEViolation;
    public double headPPEViolation;
    public double handPPEViolation;
    public double imageCount;
    
    public boolean violation;
    
    @GetMapping(value = "/scan-all", consumes = "*/*", produces = "application/json")
    @ResponseBody
    public ResponseEntity<PPEResponse> scanForGeneralPPE(@RequestParam String bucketName) {
        // List all objects in the S3 bucket
        ListObjectsV2Result imageList = s3Client.listObjectsV2(bucketName);

        // This will hold all of our classifications
        List<PPEClassificationResponse> classificationResponses = new ArrayList<>();

        // This is all the images in the bucket
        List<S3ObjectSummary> images = imageList.getObjectSummaries();

        // Iterate over each object and scan for PPE
        for (S3ObjectSummary image : images) {
            Timer.Sample timer = Timer.start(meterRegistry);
            logger.info("scanning " + image.getKey());
            violationCount = 0;
            // This is where the magic happens, use AWS rekognition to detect PPE
            DetectProtectiveEquipmentRequest request = new DetectProtectiveEquipmentRequest()
                    .withImage(new Image()
                            .withS3Object(new S3Object()
                                    .withBucket(bucketName)
                                    .withName(image.getKey())))
                    .withSummarizationAttributes(new ProtectiveEquipmentSummarizationAttributes()
                            .withMinConfidence(80f)
                            .withRequiredEquipmentTypes("HAND_COVER", "FACE_COVER", "HEAD_COVER"));

            DetectProtectiveEquipmentResult result = rekognitionClient.detectProtectiveEquipment(request);

            // If any person on an image lacks PPE, it's a violation of regulations
            
            
            List <ProtectiveEquipmentPerson> persons = result.getPersons();
            
            for(ProtectiveEquipmentPerson person: persons){
                
                int facePPE = isViolationFace(result);
                int headPPE = isViolationHead(result);
                int handPPE = isViolationHands(result);
                if (0 < facePPE || 0 < headPPE || 0 < handPPE){
                    facePPEViolation += facePPE;
                    headPPEViolation += headPPE;
                    handPPEViolation += handPPE;
                    violation = true;
                }
            }
            logger.info("scanning " + image.getKey() + ", violation result " + violation + ", violation count " + violationCount);
            // Categorize the current image as a violation or not.
            int personCount = result.getPersons().size();
            PPEClassificationResponse classification = new PPEClassificationResponse(image.getKey(), personCount, violation, violationCount);
            classificationResponses.add(classification);
        
            meterRegistry.counter("people_scanned").increment(personCount);
            timer.stop(meterRegistry.timer("image_scan_duration"));
            
            meterRegistry.counter("violation_face").increment(facePPEViolation);
            meterRegistry.counter("violation_head").increment(headPPEViolation);
            meterRegistry.counter("violation_hands").increment(handPPEViolation);
            meterRegistry.counter("total_PPE_violations").increment(facePPEViolation+headPPEViolation+handPPEViolation);
            meterRegistry.counter("avg_face").increment(facePPEViolation);
            meterRegistry.counter("avg_head").increment(headPPEViolation);
            meterRegistry.counter("avg_hands").increment(handPPEViolation);
            meterRegistry.counter("img_scanned").increment(images.size());
        }
        
        
        
        PPEResponse ppeResponse = new PPEResponse(bucketName, classificationResponses);
        return ResponseEntity.ok(ppeResponse);
    }
    /**
     * Detects if the image has a protective gear violation for the FACE bodypart-
     * It does so by iterating over all persons in a picture, and then again over
     * each body part of the person. If the body part is a FACE and there is no
     * protective gear on it, a violation is recorded for the picture.
     *
     * @param result
     * @return
     */
    private static boolean isViolation(DetectProtectiveEquipmentResult result) {
        return result.getPersons().stream()
                .flatMap(p -> p.getBodyParts().stream())
                .anyMatch(bodyPart -> bodyPart.getName().equals("FACE")
                        && bodyPart.getEquipmentDetections().isEmpty());
    }
    
    //#####################################################################################################
    
    private static int isViolationFace(DetectProtectiveEquipmentResult result) {
        boolean b = result.getPersons().stream()
                .flatMap(p -> p.getBodyParts().stream())
                .anyMatch(bodyPart -> bodyPart.getName().equals("FACE")
                        && bodyPart.getEquipmentDetections().isEmpty());
        int i = 0;
        if(b){
            i = 1;
        }
        return i;
    }
    private static int isViolationHead(DetectProtectiveEquipmentResult result) {
        boolean b = result.getPersons().stream()
                .flatMap(p -> p.getBodyParts().stream())
                .anyMatch(bodyPart -> bodyPart.getName().equals("HEAD")
                        && bodyPart.getEquipmentDetections().isEmpty());
        int i = 0;
        if(b){
            i = 1;
        }
        return i; 
    }
    private static int isViolationHands(DetectProtectiveEquipmentResult result) {
        boolean b = result.getPersons().stream()
                .flatMap(p -> p.getBodyParts().stream())
                .anyMatch(bodyPart -> bodyPart.getName().equals("LEFT_HAND")
                        && bodyPart.getEquipmentDetections().isEmpty() || bodyPart.getName().equals("RIGHT_HAND")
                        && bodyPart.getEquipmentDetections().isEmpty());
        int i = 0;
        if(b){
            i = 1;
        }
        return i; 
    }
    
    

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
    }
}
