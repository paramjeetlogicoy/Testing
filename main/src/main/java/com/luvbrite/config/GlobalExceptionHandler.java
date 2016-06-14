package com.luvbrite.config;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String maxSizeExceeded(HttpServletRequest request, Exception ex){        
        return "misc/upload-size-error";
    }
    
    @ExceptionHandler
    public String handleException(HttpServletRequest request, Exception ex){
        System.out.println("GlobalExceptionHandler - ");
        ex.printStackTrace();
        
        return "generic-error";
    }
}
