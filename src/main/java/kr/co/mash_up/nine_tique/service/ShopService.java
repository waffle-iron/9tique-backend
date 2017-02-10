package kr.co.mash_up.nine_tique.service;

import kr.co.mash_up.nine_tique.domain.Seller;
import kr.co.mash_up.nine_tique.domain.Shop;
import kr.co.mash_up.nine_tique.dto.ShopDto;
import kr.co.mash_up.nine_tique.exception.AlreadyExistException;
import kr.co.mash_up.nine_tique.exception.IdNotFoundException;
import kr.co.mash_up.nine_tique.exception.UserIdNotMatchedException;
import kr.co.mash_up.nine_tique.repository.SellerRepository;
import kr.co.mash_up.nine_tique.repository.ShopRepository;
import kr.co.mash_up.nine_tique.util.CodeGeneratorUtil;
import kr.co.mash_up.nine_tique.vo.DataListRequestVO;
import kr.co.mash_up.nine_tique.vo.ShopRequestVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Shop과 관련된 비즈니스 로직 처리
 */
@Service(value = "shopService")
@Slf4j
public class ShopService {

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private SellerRepository sellerRepository;

    public Shop create(ShopRequestVO requestVO) {
        Shop newShop = requestVO.toShopEntitiy();
        Shop oldShop = shopRepository.findByNameAndPhone(newShop.getName(), newShop.getPhone());

        if (oldShop == null) {  // oldShop 1번도 등록 안된경우 -> 인증코드 생성 후 등록
            newShop.setAuthentiCode(CodeGeneratorUtil.generateAuthentiCode());
            return shopRepository.save(newShop);
        }
        if (oldShop.isEnabled()) {
            throw new AlreadyExistException("shop create -> shop already exist");
        }

        oldShop.enable();
        return shopRepository.save(newShop);
    }

    public Page<ShopDto> list(DataListRequestVO requestVO) {
        Pageable pageable = requestVO.getPageable();

        Page<Shop> shopPage = shopRepository.findShops(pageable);

        List<ShopDto> shopDtos = shopPage.getContent().stream()
                .map(shop -> new ShopDto.Builder()
                        .withName(shop.getName())
                        .withInfo(shop.getInfo())
                        .withPhone(shop.getPhone())
                        .withAuthentiCode(shop.getAuthentiCode())
                        .build()
                )
                .collect(Collectors.toList());

        Pageable resultPageable = new PageRequest(shopPage.getNumber(), shopPage.getSize(),
                new Sort(Sort.Direction.DESC, "createdAt"));

        return new PageImpl<ShopDto>(shopDtos, resultPageable, shopPage.getTotalElements());
    }

    public ShopDto findOne(Long shopId) {
        Shop shop = shopRepository.findOne(shopId);

        Optional.ofNullable(shop).orElseThrow(() -> new IdNotFoundException("shop find by id -> shop not found"));
        if (!shop.isEnabled()) {
            throw new IdNotFoundException("shop find by id -> shop not found");
        }

        return new ShopDto.Builder()
                .withName(shop.getName())
                .withInfo(shop.getInfo())
                .withPhone(shop.getPhone())
                .withAuthentiCode(shop.getAuthentiCode())
                .build();
    }

    public Shop update(Long userId, Long shopId, ShopRequestVO requestVO) {
        Shop oldShop = shopRepository.findOne(shopId);
        Optional.ofNullable(oldShop).orElseThrow(() -> new IdNotFoundException("shop update -> shop not found"));
        if (!oldShop.isEnabled()) {
            throw new IdNotFoundException("shop update -> shop not found");
        }

        Seller seller = sellerRepository.findByUserId(userId);
        if (!seller.matchShop(oldShop)) {  // 판매자가 해당 shop에 속해있나
            throw new UserIdNotMatchedException("shop update -> forbbiden access");
        }

        oldShop.update(requestVO.toShopEntitiy());
        return shopRepository.save(oldShop);
    }

    public void delete(Long shopId) {
        Shop oldShop = shopRepository.findOne(shopId);
        Optional.ofNullable(oldShop).orElseThrow(() -> new IdNotFoundException("shop find by id -> shop not found"));
        if (!oldShop.isEnabled()) {
            throw new IdNotFoundException("shop find by id -> shop not found");
        }

        oldShop.disable();
        shopRepository.save(oldShop);
    }
}