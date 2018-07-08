package yly.crawl.springboot.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import yly.crawl.springboot.pojo.Image;

public interface ImageDAO extends JpaRepository<Image, Long> {
	@Query("select image from Image image where image.galleryId=?1 and image.serialNum>=?2 and image.serialNum<=?3 order by image.serialNum")
	List<Image> inImage(String galleryId, Integer beginSerialNum, Integer endSerialNum);
	
	@Query("select i from Image i where i.galleryId=?1 and i.serialNum=?2")
	Optional<Image> findByUkImage(String galleryId, Integer SerialNum);
}
