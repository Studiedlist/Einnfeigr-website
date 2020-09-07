package main.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import main.page.PageTemplateData;
import main.template.Template;
import main.template.TemplateFactory;

@ControllerAdvice
public class ExceptionHandlerController extends ResponseEntityExceptionHandler {

	@ExceptionHandler(value = {
			ControllerException.class,
			IllegalArgumentException.class,
			IllegalStateException.class })
	public ResponseEntity<Object> handleConflict(RuntimeException ex,
			WebRequest request) {
		try {
			Template template;
			String bodyOfResponse;
			String responseTemplatePath;
			PageTemplateData pageData = new PageTemplateData();
			if(ex instanceof ControllerException) {
				String textPath;
				if(ex instanceof NotFoundException) {
					textPath = "static/text/ru/error/notFound";
				} else {
					textPath = "static/text/ru/error/error";
				}
				template = TemplateFactory.buildTemplate(textPath);
				pageData.setText(template.compile());
			}
			if(request.getParameter("path") == null) {
				responseTemplatePath = "templates/index";
			} else {
				responseTemplatePath = "templates/placeholder";
			}
			String pagePath = "templates/pages/error/error";
			template = TemplateFactory.buildTemplate(pageData, pagePath);
			PageTemplateData responseData = new PageTemplateData();
			responseData.setTitle("Ошибка");
			responseData.setPage(template.compile());
			template = TemplateFactory.buildTemplate(responseData,
					responseTemplatePath);
			bodyOfResponse = template.compile();
		    return handleExceptionInternal(ex, bodyOfResponse, 
		  	      new HttpHeaders(), HttpStatus.OK, request);
		} catch (Exception e) {
			e.printStackTrace();
			return handleExceptionInternal(ex, "что-то пошло не так", 
			  	      new HttpHeaders(), HttpStatus.OK, request);
		}
	}
}