package main.page;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import main.album.Album;
import main.album.AlbumController;
import main.exception.ControllerException;
import main.exception.NotFoundException;
import main.exception.TemplateException;
import main.img.ImageDataController;
import main.misc.Util;
import main.template.AlbumListTemplate;
import main.template.AlbumPageTemplateData;
import main.template.AlbumTemplate;
import main.template.Template;
import main.template.TemplateFactory;

@RestController
public class PageController {
	
	private final static Logger logger = 
			LoggerFactory.getLogger(PageController.class);
	
	@Autowired
	ImageDataController dataController;
	@Autowired 
	AlbumController albumController;
	
    @RequestMapping(value= {"/{page}", "/"}, method= RequestMethod.GET,
    		produces = "application/json", headers = "target=body")
    public ResponseEntity<Page> getPageBody(Device device, 
    		@RequestParam(required=false) String ver,
    		@PathVariable(value="page",required=false) Optional<String> pageName
    				) throws TemplateException {
    	try {
    		String name;
    		if(pageName.isPresent()) {
    			name = pageName.get();
    		} else {
    			name = "main";
    		}
	    	Page page = new Page();
	    	PageTemplateData data = getPageData(name, device, ver);
	    	page.setTitle(data.getTitle());
	    	page.setContent(data.getPage());
	    	return new ResponseEntity<Page>(page, HttpStatus.OK);
    	} catch(Exception e) {
    		logger.error(Util.EXCEPTION_LOG_MESSAGE, e);
			ControllerException exception = new ControllerException(e);
			throw exception;
    	}
    }
	
    @RequestMapping(value= {"/{page}", "/"}, method= RequestMethod.GET)
    public ModelAndView getPage(Device device, 
    		@RequestParam(required=false) String ver, 
    		@PathVariable(required=false) Optional<String> page) 
    				throws ControllerException {
    	try {
    		String name;
    		if(page.isPresent()) {
    			name = page.get();
    		} else {
    			name = "main";
    		}
	  		ModelAndView mav = new ModelAndView("index");
      		PageTemplateData data = getPageData(name, device, ver);
	        mav.getModel().put("title", data.getTitle());
			mav.getModel().put("page", data.getPage());
			mav.getModel().put("isMobile", data.isMobile());
	    	return mav;
    	} catch(NotFoundException e) {
    		throw e;
		} catch(Exception e) {
			logger.error(Util.EXCEPTION_LOG_MESSAGE, e);
			ControllerException exception = new ControllerException(e);
			throw exception;
		}
    }
    
    private PageTemplateData getPageData(String page, Device device, String ver) 
    		throws IOException {
    	PageTemplateData data = new PageTemplateData();
    	data.setMobile(isMobile(data, device, ver));
    	switch(page) {
			case("main"):
	        	data = compileMain(data);
				data.setTitle("Главная");	
				break;
			case("portfolio"):
				Template template = TemplateFactory.buildTemplate(
						albumController.getRootAlbums());
				data.setText(template.compile());
	        	data = loadPage(data, null, 
						"templates/pages/portfolio");
				data.setTitle("Портфолио");
				break;
			case("retouch"):
				data = loadPage(new PageTemplateData(), 
						"static/text/ru/retouch", 
						"templates/pages/retouch");
				data.setTitle("Ретушь");
				break;
			case("about"):
				data = loadPage(data, "static/text/ru/about", 
						"templates/pages/about");
				data.setTitle("Обо всем");
				break;
			case("contacts"):
				data = loadPage(data, "static/text/ru/contacts", 
						"templates/pages/contacts");
				data.setTitle("Контакты");
				break;
			default: 
				logger.error("Invalid page address: "+page);
				throw new NotFoundException("Invalid page address: "+page);
    	} 	
    	return data;
    }
    
    @RequestMapping(value= "/portfolio/albums/{id}",
    		method = RequestMethod.GET, produces = "application/json",
    		headers = "target=body")
    public ResponseEntity<Page> getAlbumBody(Device device, 
    		@PathVariable() String id,
    		@RequestParam(required=false) String ver) throws TemplateException {
    	try {
	    	Page page = new Page();
	    	Album album = albumController.getAlbum(id);
	    	Template template = new AlbumTemplate(album);	    	
	    	page.setTitle(Util.toUpperCase(album.getName()));
	    	page.setContent(template.compile());
	    	return new ResponseEntity<Page>(page, HttpStatus.OK);
    	} catch(Exception e) {
    		logger.error(Util.EXCEPTION_LOG_MESSAGE, e);
			ControllerException exception = new ControllerException(e);
			throw exception;
    	}
    }
    
    @RequestMapping(value= "/portfolio/albums/{id}", 
    		method= RequestMethod.GET)
    public ModelAndView getAlbum(Device device,
    		@PathVariable() String id,
    		@RequestParam(required=false) String ver) throws TemplateException {
    	ModelAndView mav = null;
    	PageTemplateData data = new AlbumPageTemplateData();
      	try {
      		mav = new ModelAndView("index");
      		data.setMobile(isMobile(data, device, ver));
	    	Album album = albumController.getAlbum(id);
	    	Template template = new AlbumTemplate(album);
	    	data.setPage(template.compile());
	    	data.setTitle(Util.toUpperCase(album.getName()));
	    	mav.getModel().put("name", album.getName());
	        mav.getModel().put("title", data.getTitle());
			mav.getModel().put("page", data.getPage());
			return mav;
    	} catch (Exception e) {
    		logger.error(Util.EXCEPTION_LOG_MESSAGE, e);
			ControllerException exception = new ControllerException(e);
			throw exception;
		}
    }
    
    private PageTemplateData compileMain(PageTemplateData data) {
    	Template template;
		try {
    		template = TemplateFactory.buildTemplate(
    				dataController.getLatestImages());
    		Map<String, String> textData = new HashMap<>();
    		Map<String, String> pageData = new HashMap<>();
        	textData.put("latest", template.compile());
    		template = TemplateFactory.buildTemplate(textData,
    				"static/text/ru/main");
    		pageData.put("text", template.compile());
    		template = TemplateFactory.buildTemplate(pageData,
    				"templates/pages/main");
    		data.setPage(template.compile());
    	} catch(IOException | IllegalArgumentException e) {
    		logger.error(Util.EXCEPTION_LOG_MESSAGE, e);
    	}
    	return data;
    }
    
    private PageTemplateData loadPage(PageTemplateData data, String textPath, 
    		String pagePath) throws IOException {
    	Template template;
    	if(data == null || pagePath == null) {
        	throw new NullPointerException("Null passed as argument");
        }
    	if(textPath != null) {
    		if(data.getTextData() == null) {
    			data.setTextData(new HashMap<>());
    		}
    		template = TemplateFactory.buildTemplate(data.getTextData(),
    				textPath);
    		data.setText(template.compile());
    	}
    	template = TemplateFactory.buildTemplate(data, pagePath);
    	data.setPage(template.compile());
        return data;
    }
    
    private boolean isMobile(PageTemplateData data, Device device, String ver) {
    	if(ver == null) {
    		ver = "";
    	}
    	if(device.isNormal() && ver.toString().equals("") 
    			|| ver.equals("desktop")) {
    		return false;
	  	} else if((device.isMobile() && ver.toString().equals(""))
	  			|| (device.isTablet() && ver.toString().equals(""))
    			|| ver.toString().equals("mobile")) {
    		return true;
	  	} else {
	  		logger.warn("Cannot specify device! ver:"+ver);
	  		return true;
	  	}
    }
    
}