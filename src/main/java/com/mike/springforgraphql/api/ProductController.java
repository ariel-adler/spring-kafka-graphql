package com.mike.springforgraphql.api;

import com.mike.springforgraphql.api.input.ProductInput;
import com.mike.springforgraphql.api.input.ProductSearchCriteriaInput;
import com.mike.springforgraphql.api.response.Product;
import com.mike.springforgraphql.api.response.ProductPriceHistory;
import com.mike.springforgraphql.config.ReactiveReceiver;
import com.mike.springforgraphql.db.entity.ProductEntity;
import com.mike.springforgraphql.db.entity.ProductPriceHistoryEntity;
import com.mike.springforgraphql.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Controller
@Slf4j
public class ProductController {

    @Autowired
    private ReactiveReceiver reactiveReceiver;
    private final ProductService productService;

    private final Random rn = new Random();

    private final Logger logger = LoggerFactory.getLogger(ProductController.class);

    public ProductController(ProductService productService) {

        this.productService = productService;

    }

    @QueryMapping("getProduct") // value (i.e. "getProduct") must match GraphQL schema operation
    public Product findProduct(@Argument Long id) {

        logger.debug("Find Product for id {}", id);

        var productEntity = productService.findProduct(id);

        if (productEntity == null) {

            logger.debug("No Product found for id {}", id);

            return null;

        }

        var product = convertProductEntityToProduct(productEntity);

        logger.debug("Found Product {} for id {}", product, id);

        return product;
    }

    @QueryMapping("allProducts") // value (i.e. "allProducts") must match GraphQL schema operation
    public List<Product> findAllProducts() {

        logger.debug("Find All Products");

        var productEntities = productService.findAllProducts();

        var products = convertProductEntityListToProductList(productEntities);

        logger.debug("Found All Product {}", products);

        return products;

    }

    @QueryMapping("searchProducts") // value (i.e. "searchProducts") must match GraphQL schema
    // operation
    public List<Product> searchProducts(@Argument ProductSearchCriteriaInput productSearchCriteriaInput) {

        logger.debug("Search for Products using criteria {}", productSearchCriteriaInput);

        List<ProductEntity> productEntities;

        productEntities = productService.searchProducts(productSearchCriteriaInput);

        if (productEntities == null) {
            logger.debug("No Products found for search criteria {}", productSearchCriteriaInput);

            return new ArrayList<>();
        }

        var products = convertProductEntityListToProductList(productEntities);

        logger.debug("Found {} Products using criteria {}", products, productSearchCriteriaInput);

        return products;

    }

    @MutationMapping("saveProduct") // value (i.e. "saveProduct") must match GraphQL schema
    // operation
    public Product saveProduct(@Argument ProductInput productInput) {

        if (productInput.id() == null) {

            logger.debug("Insert Product for ProductInput {}", productInput);

        } else {

            logger.debug("Update Product for ProductInput {}", productInput);

        }

        var savedProduct = productService.saveProduct(productInput);

        var apiProduct = convertProductEntityToProduct(savedProduct);

        logger.debug("Created Product {}", apiProduct);

        return apiProduct;

    }

    @MutationMapping("deleteProduct") // value (i.e. "deleteProduct") must match GraphQL schema
    // operation
    public Long deleteProduct(@Argument Long id) {

        logger.debug("Delete Product for Id {}", id);

        var deletedId = productService.deleteProduct(id);

        if (deletedId == null) {
            logger.debug("Product for id {} did not exist so could not be deleted", id);

        } else {
            logger.debug("Product for id {} deleted", id);

        }

        return deletedId;

    }

    private Product convertProductEntityToProduct(ProductEntity productEntity) {

        var product = new Product(productEntity.getId(), productEntity.getTitle(),
                productEntity.getDescription(), productEntity.getPrice(), new ArrayList<>());

        for (ProductPriceHistoryEntity productPriceHistoryEntity : productEntity.getProductPriceHistories()) {

            product.productPriceHistories().add(
                    new ProductPriceHistory(
                            productPriceHistoryEntity.getId(), productPriceHistoryEntity.getStartDate(),
                            productPriceHistoryEntity.getPrice()));

        }

        return product;
    }

    private List<Product> convertProductEntityListToProductList(List<ProductEntity> productEntities) {

        List<Product> apiProducts = new ArrayList<>();

        for (ProductEntity productEntity : productEntities) {

            Product apiProduct = convertProductEntityToProduct(productEntity);
            apiProducts.add(apiProduct);

        }

        return apiProducts;
    }

    @SubscriptionMapping("notifyProductPriceChange")
    public Flux<ProductPriceHistory> notifyProductPriceChange(@Argument Long productId) {
        return reactiveReceiver.getReceiver();
    }
}
