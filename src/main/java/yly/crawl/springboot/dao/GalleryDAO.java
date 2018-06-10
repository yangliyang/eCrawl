package yly.crawl.springboot.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import yly.crawl.springboot.pojo.Gallery;

public interface GalleryDAO extends JpaRepository<Gallery, String>{
	
	
}
