package com.luvbrite.config;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import com.luvbrite.utils.Exceptions;

@ControllerAdvice
public class GlobalExceptionHandler {
	
	private static Logger logger = Logger.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String maxSizeExceeded(HttpServletRequest request, Exception ex){        
        return "misc/upload-size-error";
    }
    
    @ExceptionHandler
    public String handleException(HttpServletRequest request, Exception ex){
        logger.error(Exceptions.giveStackTrace(ex));
        
        return "generic-error";
    }
}
