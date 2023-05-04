package by.kaminsky.repository;

import by.kaminsky.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaterialsRepository extends JpaRepository<Material, Long> {

    List<Material> findAllByName(String name);

    Optional<Material> findByNameAndSpecific(String name, String specific);

}
