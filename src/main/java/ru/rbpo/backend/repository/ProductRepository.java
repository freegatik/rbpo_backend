package ru.rbpo.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.rbpo.backend.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
}
