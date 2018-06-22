package yly.crawl.springboot.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import yly.crawl.springboot.pojo.Gallery;

public interface GalleryDAO extends JpaRepository<Gallery, Long>{
	@Query("select gallery from Gallery gallery where gallery.serialId=?1")
	List<Gallery> findBySerialId(String serialId);
	
}
