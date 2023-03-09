package ProjectBlogOJT.model.repository;

import ProjectBlogOJT.model.entity.Product;
import com.sun.tools.javac.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product,Integer> {
    List<Product> findProductByProductNameContaining(String productName);

}
