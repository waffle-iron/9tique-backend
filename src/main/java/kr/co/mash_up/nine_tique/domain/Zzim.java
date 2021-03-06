package kr.co.mash_up.nine_tique.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

/**
 * 찜(좋아요)
 */
@Entity
@Table(name = "zzim")
@Getter
@Setter
@ToString(exclude = {"user"})
@NoArgsConstructor  // JPA는 default constructor 필요
public class Zzim extends AbstractEntity<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
//    @MapsId  //식별관계 매핑(FK가 PK에 포함될 때). FK와 매핑한 연관관계를 기본키에도 매핑하겠다는 뜻
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "zzim", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ZzimProduct> zzimProducts;

    public Zzim(User user) {
        this.user = user;
    }

//    /**
//     * 찜되어 있는지 확인
//     *
//     * @param product 확인할 상품
//     * @return 결과
//     */
//    public boolean checkProductZzim(Product product) {
//        for (ZzimProduct zzimProduct : zzimProducts) {
//            if (zzimProduct.isEnabled() && product.equals(zzimProduct.getProduct())) {
//                return true;
//            }
//        }
//        return false;
//    }

    public void addZzimProduct(ZzimProduct zzimProduct) {
        int position = searchProductZzimIndex(zzimProduct);
        if (position > -1) {
            zzimProduct = zzimProducts.get(position);
            zzimProduct.enable();
        } else {
            zzimProducts.add(zzimProduct);
        }
    }

    public void removeZzimProduct(ZzimProduct zzimProduct) {
        int deleteItemPosition = searchProductZzimIndex(zzimProduct);
        zzimProducts.get(deleteItemPosition).disable();
    }

    /**
     * 찜된 목록에서 삭제할 상품 위치 찾기
     *
     * @param zzimProduct 찾을 상품
     * @return 리스트의 상품 위치
     */
    private int searchProductZzimIndex(ZzimProduct zzimProduct) {
        for (int idx = 0; idx < zzimProducts.size(); idx++) {
            ZzimProduct.Id currentZzimProductId = zzimProducts.get(idx).getId();
            if (Objects.equals(zzimProduct.getId().getProductId(), currentZzimProductId.getProductId())
                    && Objects.equals(zzimProduct.getId().getZzimId(), currentZzimProductId.getZzimId())) {
                return idx;
            }
        }
        return -1;
    }
}
