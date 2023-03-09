package ProjectBlogOJT.model.serviceImp;

import ProjectBlogOJT.model.entity.Product;
import ProjectBlogOJT.model.repository.ProductRepository;
import ProjectBlogOJT.model.service.ProductSevice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImp implements ProductSevice {
    @Autowired
    ProductRepository productRepository;

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Product findByID(int productID) {
        return productRepository.findById(productID).get();
    }

    @Override
    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Product saveOrUpdate(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void delete(int productID) {
        productRepository.deleteById(productID);
    }

    @Override
    public List<Product> searchByName(String productName) {
        return productRepository.findProductByProductNameContaining(productName);
    }
}