package ProjectBlogOJT.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import net.minidev.json.annotate.JsonIgnore;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "Product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "productID")
    private int productID;
    @Column(name = "productName")
    private String productName;
    @Column(name = "productAuthor")
    private String productAuthor;
    @Column(name = "productPrice")
    private int productPrice;
    @Column(name = "ProductImage")
    private String productImage;
    @Column(name = "productDescription")
    private String productDescription;
    @Column(name = "productCreateDate")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date productCreateDate;
    @Column(name = "productStatus")
    private boolean productStatus;
    @ManyToOne
    @JoinColumn(name = "User")
    private User user;
    @OneToMany(mappedBy = "product")
    @JsonIgnore
    private List<Exhibition> exhibitionList = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name= "Product_Tag", joinColumns = @JoinColumn(name = "productID"),
            inverseJoinColumns = @JoinColumn(name="tagID"))
    private Set<Tag> listTag = new HashSet<>();
    @OneToMany(mappedBy = "product")
    private List<History> listHistory;


}
