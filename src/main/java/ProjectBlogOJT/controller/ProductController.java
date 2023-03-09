package ProjectBlogOJT.controller;

import ProjectBlogOJT.model.entity.Product;
import ProjectBlogOJT.model.service.ProductSevice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:8080")
@RestController
@RequestMapping("/api/v1/product")
public class ProductController {
    @Autowired
    ProductSevice productSevice;

    @GetMapping()
    public List<Product> productList() {
        List<Product> productList = productSevice.findAll();
        return productList;
    }

    @GetMapping("/searchProduct/{productName}")
    public List<Product> searchByProductName(@PathVariable("productName") String productName) {
        List<Product> productCreat = productSevice.searchByName(productName);
        return productCreat;
    }

    @PostMapping("/delete/{productID}")
    public Product delete(@PathVariable("productID") int productID) {
        Product product = productSevice.findByID(productID);
        if (product.isProductStatus() == true) {
            product.setProductStatus(false);
        } else {
            product.setProductStatus(true);
        }
        return productSevice.saveOrUpdate(product);
    }

    @PostMapping
    public Product creatProduct(@RequestBody Product productCreata) {
        Product product = productSevice.save(productCreata);
        return product;
    }

    @PutMapping("/update/{productID}")
    public Product updateProdutc(@PathVariable("productID") int productID, @RequestBody Product product) {
        Product productUpdate = productSevice.findByID(productID);
        productUpdate.setProductName(product.getProductName());
        productUpdate.setProductAuthor(product.getProductAuthor());
        productUpdate.setProductDescription(product.getProductDescription());
        productUpdate.setProductCreateDate(product.getProductCreateDate());
        productUpdate.setProductPrice(product.getProductPrice());
        productUpdate.setProductImage(product.getProductImage());
        return productSevice.saveOrUpdate(productUpdate);
    }


}
