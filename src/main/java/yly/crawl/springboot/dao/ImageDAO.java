package yly.crawl.springboot.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import yly.crawl.springboot.pojo.Image;

public interface ImageDAO extends JpaRepository<Image, Integer> {

}
