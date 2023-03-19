package com.mike.springforgraphql;

import com.mike.springforgraphql.api.ProductController;
import com.mike.springforgraphql.api.input.ProductInput;
import com.mike.springforgraphql.api.input.ProductSearchCriteriaInput;
import com.mike.springforgraphql.api.response.Product;
import com.mike.springforgraphql.api.response.ProductPriceHistory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class ProductControllerTests {

    @Autowired
    ProductController productController;

    @Test
    void testFindAllProducts() {

        List<Product> products = productController.findAllProducts();

        assertThat(products).isNotNull().hasSize(13);

    }

    @Test
    void testFindOneProductUsingIdThatExists() {

        Product product = productController.findProduct(1L);

        assertThat(product).isNotNull();
        assertThat(product.id()).isEqualTo(1L);

    }

    @Test
    void testFindOneProductUsingIdThatDoesNotExist() {

        Product product = productController.findProduct(99999999L);

        assertThat(product).isNull();

    }

    @Test
    void testFindUsingProductSearchCriteriaForBetweenLowerPriceAndUpperPrice() {

        ProductSearchCriteriaInput productSearchCriteriaInput = new ProductSearchCriteriaInput(null, null, 1, 500);

        List<Product> products = productController.searchProducts(productSearchCriteriaInput);

        assertThat(products).hasSize(3);

    }

    @Test
    void testFindUsingProductSearchCriteriaForTitle() {

        ProductSearchCriteriaInput productSearchCriteriaInput = new ProductSearchCriteriaInput("Phone", null, null,
                null);

        List<Product> products = productController.searchProducts(productSearchCriteriaInput);

        assertThat(products).hasSize(1);

    }

    @Test
    void testFindUsingProductSearchCriteriaForProductThatDoesNotExist() {

        ProductSearchCriteriaInput productSearchCriteriaInput = new ProductSearchCriteriaInput("ProductThatDoesNotExist", null,
                null,
                null);

        List<Product> products = productController.searchProducts(productSearchCriteriaInput);

        assertThat(products).isEmpty();

    }

    @Test
    void testSaveProductUsingIdThatDoesExist() {

        Long productId = 1L;
        ProductInput productInput = new ProductInput(productId, "testTitle", "testDescription", 9999,
                new ArrayList<>());

        Product product = productController.saveProduct(productInput);

        assertThat(product.id()).isNotNull();
        assertThat(product.id()).isEqualTo(productId);

    }

    @Test
    void testSaveProductThatDoesNotExist() {

        ProductInput productInput = new ProductInput(null, "testTitle", "testDescription", 9999, new ArrayList<>());

        Product product = productController.saveProduct(productInput);

        assertThat(product.id()).isNotNull();

    }

    @Test
    void testDeleteProductThatExists() {

        Long idToDelete = 2L;

        Long deletedId = productController.deleteProduct(idToDelete);

        assertThat(deletedId).isEqualTo(idToDelete);

    }

    @Test
    void testDeleteProductThatDoesNotExist() {

        Long deletedId = productController.deleteProduct(99999999L);

        assertThat(deletedId).isNull();

    }

    @Test
    void testNotifyProductPriceChang() {

        Flux<ProductPriceHistory> productPriceHistoryStream = productController.notifyProductPriceChange(1L);

        Mono<ProductPriceHistory> productPriceHistory = productPriceHistoryStream.next();

        assertThat(productPriceHistory).isNotNull();

        System.out.println(productPriceHistory);

    }

}
