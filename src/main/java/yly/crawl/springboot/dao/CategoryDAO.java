package yly.crawl.springboot.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import yly.crawl.springboot.pojo.Category;

//面向接口编程
/**
 * 父接口实现了CRUD，分页等
 * 其中泛型 Category表示对这个类的DAO
 * Integer 表示主键类型
 * @author yly
 *
 */
public interface CategoryDAO extends JpaRepository<Category, Integer>{
	
}
