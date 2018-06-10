package yly.crawl.springboot.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import yly.crawl.springboot.pojo.Image;

public interface ImageDAO extends JpaRepository<Image, String> {
	@Query("select image from Image image where image.galleryId=?1 and image.serialNum>=?2 and image.serialNum<=?3")
	List<Image> inImage(String galleryId, Integer beginSerialNum, Integer endSerialNum);
}
