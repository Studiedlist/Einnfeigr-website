package main.template.data;

public class SectionTemplateData implements TemplateData {

	String name;
	String images;
	String id;


	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getImages() {
		return images;
	}

	public void setImages(String images) {
		this.images = images;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
}
