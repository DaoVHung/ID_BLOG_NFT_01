package ProjectBlogOJT.model.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "Exhibition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Exhibition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exhibitionID")
    private int exhibitionID;
    @Column(name = "exhibitionTitle")
    private String exhibitionTitle;
    @Column(name = "exhibitionDescription")
    private String exhibitionDescription;
    @Column(name = "exhibitionCreatedDate")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date exhibitionCreatedDate;
    @Column(name = "exhibitionExpiredDate")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private Date exhibitionExpiredDate;
    @Column(name = "exhibitionStatus")
    private Boolean exhibitionStatus;
    @ManyToOne
    @JoinColumn(name = "Product")
    private Product product;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name= "Exhibition_Tag", joinColumns = @JoinColumn(name = "exhibitionID"),
            inverseJoinColumns = @JoinColumn(name="tagID"))
    private Set<Tag> listTag = new HashSet<>();
}
