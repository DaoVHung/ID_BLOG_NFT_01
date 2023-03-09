package ProjectBlogOJT.model.service;


import ProjectBlogOJT.model.entity.Product;

import java.util.List;

public interface ProductSevice {
    List<Product> findAll();
    Product findByID(int productID);
    Product save(Product product);
    Product saveOrUpdate(Product product);
    void delete(int productID);
    List<Product> searchByName(String productName);


}
