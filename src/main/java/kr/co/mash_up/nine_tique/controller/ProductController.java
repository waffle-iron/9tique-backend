package kr.co.mash_up.nine_tique.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import kr.co.mash_up.nine_tique.domain.Product;
import kr.co.mash_up.nine_tique.dto.ProductDto;
import kr.co.mash_up.nine_tique.security.SecurityUtil;
import kr.co.mash_up.nine_tique.service.ProductService;
import kr.co.mash_up.nine_tique.util.ParameterUtil;
import kr.co.mash_up.nine_tique.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import static kr.co.mash_up.nine_tique.util.Constant.RestEndpoint.API_PRODUCT;

@RestController
@RequestMapping(value = API_PRODUCT)
@Slf4j
@Api(description = "상품", tags = {"product"})
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 상품 생성
     *
     * @param requestVO 생성할 상품 정보
     * @return 생성 결과
     */
    @ApiOperation(value = "상품 생성")
    @RequestMapping(method = RequestMethod.POST)
    public ResponseVO add(@RequestBody ProductRequestVO requestVO) {
        ParameterUtil.checkParameterEmpty(requestVO.getName(), requestVO.getBrandName(), requestVO.getSize(),
                requestVO.getPrice(), requestVO.getDescription(), requestVO.getMainCategory(), requestVO.getProductImages());

        Long userId = SecurityUtil.getCurrentUser().getId();
        log.info(userId + " ");
        requestVO.setStatus(Product.Status.SELL.name());

        Product product = productService.create(userId, requestVO);

        if (product != null) {  // 생성 성공
            return ResponseVO.ok();
        }
        //Todo: 생성 실패시 예외처리 -> 어떤 경우가 있을까..?
        return ResponseVO.ok();
    }

//    @RequestMapping(method = RequestMethod.POST)
//    public ResponseVO add(@RequestParam(name = "name") String name,
//                          @RequestParam(name = "brand_name") String brandName,
//                          @RequestParam(name = "size") String size,
//                          @RequestParam(name = "price") int price,
//                          @RequestParam(name = "description") String description,
//                          @RequestParam(name = "main_category") String mainCategory,
//                          @RequestParam(name = "sub_category") String subCategory,
//                          @RequestParam(name = "files") List<String> imageUrls) {
//        ParameterUtil.checkParameterEmpty(name, brandName, size, price, description, mainCategory, imageUrls);
//
//        Long userId = SecurityUtil.getCurrentUser().getId();
//        log.info(userId + " ");
//
//        ProductRequestVO requestVO = new ProductRequestVO();
//        requestVO.setName(name);
//        requestVO.setBrandName(brandName);
//        requestVO.setSize(size);
//        requestVO.setPrice(price);
//        requestVO.setDescription(description);
//        requestVO.setMainCategory(mainCategory);
//        requestVO.setSubCategory(subCategory);
//        requestVO.setStatus(Product.Status.SELL.name());
//        requestVO.setFiles(files);
//
//        Product product = productService.create(userId, requestVO);
//
//        if (product != null) {
//            return ResponseVO.ok();
//        }
//        //Todo: 생성 실패시 예외처리 -> 어떤 경우가 있을까..?
//        return ResponseVO.ok();
//    }

    /**
     * 상품 정보 수정 (상태변경 - 판매중/완료)
     *
     * @param productId 수정할 상품 id
     * @param requestVO 수정할 상품 정보
     * @return 수정 결과
     */
    @ApiOperation(value = "상품 정보 수정")
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseVO update(@PathVariable("id") Long productId,
                             @RequestBody ProductRequestVO requestVO) {
        //Todo: status만 올때랑 name, brandName 등등이 올때를 구분해야 한다.. 좀더 개선 해보자
//        ParameterUtil.checkParameterEmpty(requestVO.getName(), requestVO.getBrandName(), requestVO.getSize(),
//                requestVO.getPrice(), requestVO.getDescription(), requestVO.getStatus(), requestVO.getMainCategory(),
//                requestVO.getProductImages());

        Long userId = SecurityUtil.getCurrentUser().getId();

        Product product;
        if (requestVO.getStatus() != null) {
            product = productService.updateStatus(userId, productId, requestVO);
        } else {
            product = productService.update(userId, productId, requestVO);
        }

        if (product != null) {
            return ResponseVO.ok();
        }

        //Todo: 수정 실패시 예외처리 -> 어떤 경우가 있을까..?
        return ResponseVO.ok();
    }

    /**
     * 상품 상세정보 조회
     *
     * @param productId 상세조회할 상품 id
     * @return 조회한 상품 정보
     */
    @ApiOperation(value = "상품 정보 조회")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public DataResponseVO<ProductDto> detail(@PathVariable("id") Long productId) {
        Long userId = SecurityUtil.getCurrentUser().getId();
        ProductDto productDto = productService.findOne(userId, productId);
        return new DataResponseVO<ProductDto>(productDto);
    }

    /**
     * 카테고리별 상품 리스트 조회
     *
     * @param requestVO request parameter가 담긴 VO
     * @return 카테고리별 상품 리스트
     */
    @ApiOperation(value = "카테고리별 상품 리스트 조회")
    @RequestMapping(method = RequestMethod.GET)
    public DataListResponseVO<ProductDto> list(ProductListRequestVO requestVO) {
        ParameterUtil.checkParameterEmpty(requestVO.getMainCategory());
        Long userId = SecurityUtil.getCurrentUser().getId();

        Page<ProductDto> page = productService.findProductsByCategory(userId, requestVO);

        log.debug(requestVO.getPageNo() + " " + requestVO.getPageSize() + " " + requestVO.getPageable() +
                " " + requestVO.getMainCategory() + " " + requestVO.getSubCategory());

        return new DataListResponseVO<ProductDto>(page);
    }

    /**
     * 상품 정보 삭제
     *
     * @param productId 삭제할 상품 id
     * @return 삭제 결과
     */
    @ApiOperation(value = "상품 정보 삭제")
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseVO delete(@PathVariable("id") Long productId) {
        Long userId = SecurityUtil.getCurrentUser().getId();
        productService.delete(userId, productId);
        return ResponseVO.ok();
    }
}
